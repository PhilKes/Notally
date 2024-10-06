package com.omgodse.notally.activities

import android.os.Build
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.omgodse.notally.R
import com.omgodse.notally.changehistory.ChangeHistory
import com.omgodse.notally.miscellaneous.add
import com.omgodse.notally.miscellaneous.setOnNextAction
import com.omgodse.notally.model.Type
import com.omgodse.notally.preferences.ListItemSorting
import com.omgodse.notally.preferences.Preferences
import com.omgodse.notally.recyclerview.ListManager
import com.omgodse.notally.recyclerview.adapter.MakeListAdapter
import com.omgodse.notally.sorting.ListItemNoSortCallback
import com.omgodse.notally.sorting.ListItemSortedByCheckedCallback
import com.omgodse.notally.sorting.ListItemSortedList
import com.omgodse.notally.sorting.toMutableList
import com.omgodse.notally.widget.WidgetProvider

class MakeList : NotallyActivity(Type.LIST) {

    private lateinit var adapter: MakeListAdapter
    private lateinit var items: ListItemSortedList

    private lateinit var listManager: ListManager

    override suspend fun saveNote() {
        model.saveNote(items.toMutableList())
        WidgetProvider.sendBroadcast(application, longArrayOf(model.id))
    }

    override fun setupToolbar() {
        super.setupToolbar()
        binding.Toolbar.menu.add(
            1,
            R.string.delete_checked_items,
            R.drawable.delete_all,
            MenuItem.SHOW_AS_ACTION_IF_ROOM,
        ) {
            listManager.deleteCheckedItems()
        }
        binding.Toolbar.menu.add(
            1,
            R.string.check_all_items,
            R.drawable.checkbox_fill,
            MenuItem.SHOW_AS_ACTION_IF_ROOM,
        ) {
            listManager.changeCheckedForAll(true)
        }
        binding.Toolbar.menu.add(
            1,
            R.string.uncheck_all_items,
            R.drawable.checkbox,
            MenuItem.SHOW_AS_ACTION_IF_ROOM,
        ) {
            listManager.changeCheckedForAll(false)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.Toolbar.menu.setGroupDividerEnabled(true)
        }
    }

    override fun initActionManager(undo: MenuItem, redo: MenuItem) {
        changeHistory = ChangeHistory {
            undo.isEnabled = changeHistory.canUndo()
            redo.isEnabled = changeHistory.canRedo()
        }
    }

    override fun configureUI() {
        binding.EnterTitle.setOnNextAction { listManager.moveFocusToNext(-1) }

        if (model.isNewNote) {
            if (model.items.isEmpty()) {
                listManager.add(pushChange = false)
            }
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        binding.AddItem.setOnClickListener { listManager.add() }
    }

    override fun setStateFromModel() {
        super.setStateFromModel()
        val elevation = resources.displayMetrics.density * 2
        listManager =
            ListManager(
                binding.RecyclerView,
                changeHistory,
                preferences,
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager,
            )
        adapter =
            MakeListAdapter(
                model.textSize,
                elevation,
                Preferences.getInstance(application),
                listManager,
            )
        val sortCallback =
            when (preferences.listItemSorting.value) {
                ListItemSorting.autoSortByChecked -> ListItemSortedByCheckedCallback(adapter)
                else -> ListItemNoSortCallback(adapter)
            }
        items = ListItemSortedList(sortCallback)
        if (sortCallback is ListItemSortedByCheckedCallback) {
            sortCallback.setList(items)
        }
        items.init(model.items)
        adapter.setList(items)
        binding.RecyclerView.adapter = adapter
        listManager.adapter = adapter
        listManager.initList(items)
    }
}
