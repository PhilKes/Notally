package com.omgodse.notally.recyclerview.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.omgodse.notally.databinding.ErrorBinding
import com.omgodse.notally.image.FileError

class ErrorVH(private val binding: ErrorBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(error: FileError) {
        binding.Name.text = error.name
        binding.Description.text = error.description
    }
}
