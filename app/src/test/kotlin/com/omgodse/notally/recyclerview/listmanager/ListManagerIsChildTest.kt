package com.omgodse.notally.recyclerview.listmanager

import com.omgodse.notally.changehistory.ListIsChildChange
import com.omgodse.notally.preferences.ListItemSorting
import com.omgodse.notally.test.assert
import com.omgodse.notally.test.assertOrder
import org.junit.Test

class ListManagerIsChildTest : ListManagerTestBase() {

    // region changeIsChild

    @Test
    fun `changeIsChild changes parent to child and pushes change`() {
        setSorting(ListItemSorting.noAutoSort)
        listManager.changeIsChild(1, isChild = true)

        items.assertOrder("A", "B")
        "A".assertChildren("B")
        (changeHistory.lookUp() as ListIsChildChange).assert(true, 1)
    }

    @Test
    fun `changeIsChild changes child to parent`() {
        setSorting(ListItemSorting.noAutoSort)
        listManager.changeIsChild(1, isChild = true)

        listManager.changeIsChild(1, isChild = false)

        "A".assertIsParent()
        "B".assertIsParent()
        (changeHistory.lookUp() as ListIsChildChange).assert(false, 1)
    }

    @Test
    fun `changeIsChild adds all child items when item becomes a parent`() {
        setSorting(ListItemSorting.noAutoSort)
        listManager.changeIsChild(1, true)
        listManager.changeIsChild(2, true)

        listManager.changeIsChild(1, isChild = false)

        "A".assertChildren()
        "B".assertChildren("C")
        (changeHistory.lookUp() as ListIsChildChange).assert(false, 1)
    }

    // endregion

}
