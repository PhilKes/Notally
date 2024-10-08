package com.omgodse.notally.recyclerview.viewholder

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.omgodse.notally.R
import com.omgodse.notally.databinding.RecyclerBaseNoteBinding
import com.omgodse.notally.miscellaneous.Operations
import com.omgodse.notally.miscellaneous.applySpans
import com.omgodse.notally.miscellaneous.displayFormattedTimestamp
import com.omgodse.notally.miscellaneous.dp
import com.omgodse.notally.model.BaseNote
import com.omgodse.notally.model.Color
import com.omgodse.notally.model.FileAttachment
import com.omgodse.notally.model.ListItem
import com.omgodse.notally.model.SpanRepresentation
import com.omgodse.notally.model.Type
import com.omgodse.notally.preferences.TextSize
import com.omgodse.notally.recyclerview.ItemListener
import java.io.File

class BaseNoteVH(
    private val binding: RecyclerBaseNoteBinding,
    private val dateFormat: String,
    private val textSize: String,
    private val maxItems: Int,
    maxLines: Int,
    maxTitle: Int,
    listener: ItemListener,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        val title = TextSize.getDisplayTitleSize(textSize)
        val body = TextSize.getDisplayBodySize(textSize)

        binding.Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, title)
        binding.Date.setTextSize(TypedValue.COMPLEX_UNIT_SP, body)
        binding.Note.setTextSize(TypedValue.COMPLEX_UNIT_SP, body)

        binding.LinearLayout.children.forEach { view ->
            view as TextView
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, body)
        }

        binding.Title.maxLines = maxTitle
        binding.Note.maxLines = maxLines

        binding.root.setOnClickListener { listener.onClick(adapterPosition) }

        binding.root.setOnLongClickListener {
            listener.onLongClick(adapterPosition)
            return@setOnLongClickListener true
        }
    }

    fun updateCheck(checked: Boolean) {
        binding.root.isChecked = checked
    }

    fun bind(baseNote: BaseNote, imageRoot: File?, fileRoot: File?, checked: Boolean) {
        updateCheck(checked)

        when (baseNote.type) {
            Type.NOTE -> bindNote(baseNote.body, baseNote.spans)
            Type.LIST -> bindList(baseNote.items)
        }

        binding.Date.displayFormattedTimestamp(baseNote.timestamp, dateFormat)
        setColor(baseNote.color)
        setImages(baseNote.images, imageRoot)
        setFiles(baseNote.files, fileRoot)

        binding.Title.text = baseNote.title
        binding.Title.isVisible = baseNote.title.isNotEmpty()

        Operations.bindLabels(binding.LabelGroup, baseNote.labels, textSize)

        if (isEmpty(baseNote)) {
            binding.Title.setText(getEmptyMessage(baseNote))
            binding.Title.visibility = View.VISIBLE
        }
    }

    private fun bindNote(body: String, spans: List<SpanRepresentation>) {
        binding.LinearLayout.visibility = View.GONE

        binding.Note.text = body.applySpans(spans)
        binding.Note.isVisible = body.isNotEmpty()
    }

    private fun bindList(items: List<ListItem>) {
        binding.Note.visibility = View.GONE

        if (items.isEmpty()) {
            binding.LinearLayout.visibility = View.GONE
        } else {
            binding.LinearLayout.visibility = View.VISIBLE

            val filteredList = items.take(maxItems)
            binding.LinearLayout.children.forEachIndexed { index, view ->
                if (view.id != R.id.ItemsRemaining) {
                    if (index < filteredList.size) {
                        val item = filteredList[index]
                        view as TextView
                        view.text = item.body
                        handleChecked(view, item.checked)
                        view.visibility = View.VISIBLE
                        if (item.isChild) {
                            val layoutParams = view.layoutParams as LinearLayout.LayoutParams
                            layoutParams.marginStart = 150.dp
                            view.layoutParams = layoutParams
                        }
                    } else view.visibility = View.GONE
                }
            }

            if (items.size > maxItems) {
                binding.ItemsRemaining.visibility = View.VISIBLE
                binding.ItemsRemaining.text = (items.size - maxItems).toString()
            } else binding.ItemsRemaining.visibility = View.GONE
        }
    }

    private fun setColor(color: Color) {
        val context = binding.root.context

        if (color == Color.DEFAULT) {
            val stroke = ContextCompat.getColorStateList(context, R.color.chip_stroke)
            binding.root.setStrokeColor(stroke)
            binding.root.setCardBackgroundColor(0)
        } else {
            binding.root.strokeColor = 0
            val colorInt = Operations.extractColor(color, context)
            binding.root.setCardBackgroundColor(colorInt)
        }
    }

    private fun setImages(images: List<FileAttachment>, mediaRoot: File?) {
        if (images.isNotEmpty()) {
            binding.ImageView.visibility = View.VISIBLE
            binding.Message.visibility = View.GONE

            val image = images[0]
            val file = if (mediaRoot != null) File(mediaRoot, image.localName) else null

            Glide.with(binding.ImageView)
                .load(file)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(
                    object : RequestListener<Drawable> {

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean,
                        ): Boolean {
                            binding.Message.visibility = View.VISIBLE
                            return false
                        }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean,
                            ): Boolean {
                                return false
                            }
                        }
                    )
                    .into(ImageView)
                if (images.size > 1) {
                    Space.visibility = View.GONE
                    ImageViewMore.apply {
                        text = getQuantityString(R.plurals.more_images, images.size - 1)
                        visibility = View.VISIBLE
                    }
                } else {
                    ImageViewMore.visibility = View.GONE
                    Space.visibility = View.VISIBLE
                }
            } else {
                Space.visibility = View.VISIBLE
                ImageView.visibility = View.GONE
                Message.visibility = View.GONE
                ImageViewMore.visibility = View.GONE
                Glide.with(ImageView).clear(ImageView)
            }
        }
    }

    private fun setFiles(files: List<FileAttachment>, mediaRoot: File?) {
        if (files.isNotEmpty()) {
            binding.FileView.visibility = View.VISIBLE

            val firstFile = files[0]
            binding.FileView.text = firstFile.originalName
            if (files.size > 1) {
                binding.FileViewMore.text =
                    binding.FileViewMore.context.resources.getQuantityString(
                        R.plurals.more_files,
                        files.size - 1,
                        files.size - 1,
                    )
                binding.FileViewMore.visibility = View.VISIBLE
            } else {
                binding.FileViewMore.visibility = View.GONE
            }
        } else {
            binding.FileView.visibility = View.GONE
            binding.FileViewMore.visibility = View.GONE
        }
    }

    private fun isEmpty(baseNote: BaseNote): Boolean {
        return when (baseNote.type) {
            Type.NOTE ->
                baseNote.title.isBlank() && baseNote.body.isBlank() && baseNote.images.isEmpty()
            Type.LIST ->
                baseNote.title.isBlank() && baseNote.items.isEmpty() && baseNote.images.isEmpty()
        }
    }

    private fun getEmptyMessage(baseNote: BaseNote): Int {
        return when (baseNote.type) {
            Type.NOTE -> R.string.empty_note
            Type.LIST -> R.string.empty_list
        }
    }

    private fun handleChecked(textView: TextView, checked: Boolean) {
        if (checked) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.checkbox_16,
                0,
                0,
                0,
            )
        } else
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.checkbox_outline_16,
                0,
                0,
                0,
            )
    }
}
