package com.omgodse.notally.sorting

import androidx.recyclerview.widget.RecyclerView
import com.omgodse.notally.test.assertOrder
import com.omgodse.notally.test.createListItem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

open class ListItemSortedListTest {

    private lateinit var items: ListItemSortedList
    private lateinit var adapter: RecyclerView.Adapter<*>

    @Before
    fun setup() {
        adapter = mock(RecyclerView.Adapter::class.java)
        val callback = ListItemSortedByCheckedCallback(adapter)
        items = ListItemSortedList(callback)
        callback.setList(items)
    }

    @Test
    fun `init fixes null order values`() {
        items.addAll(
            createListItem("A", id = 0, order = null),
            createListItem("B", id = 1, order = null),
            createListItem("C", id = 2, order = 2),
            createListItem("D", id = 3, order = null),
            createListItem("E", id = 4, order = null),
            createListItem("F", id = 5, order = null),
        )

        items.assertOrder("A", "B", "C", "D", "E", "F")
        for (i in items.indices) {
            assertEquals(i, items[i].order)
        }
    }

    @Test
    fun `init fixes duplicate order values`() {
        items.addAll(
            createListItem("A", id = 0, order = 0),
            createListItem("B", id = 1, order = 1),
            createListItem("C", id = 2, order = 1),
            createListItem("D", id = 3, order = 2),
            createListItem("E", id = 4, order = 3),
            createListItem("F", id = 5, order = 3),
        )

        items.assertOrder("A", "B", "C", "D", "E", "F")
        assertOrders(0, 1, 2, 3, 4, 5)
    }

    @Test
    fun `init with unordered input`() {
        items.addAll(
            createListItem("A", id = 0, order = 0),
            createListItem("F", id = 5, order = 5),
            createListItem("D", id = 3, order = 3),
            createListItem("E", id = 4, order = 4),
            createListItem("C", id = 2, order = 2),
            createListItem("B", id = 1, order = 1),
        )

        items.assertOrder("A", "B", "C", "D", "E", "F")
        assertOrders(0, 1, 2, 3, 4, 5)
    }

    private fun assertOrders(vararg orders: Int) {
        for (idx in 0 until orders.size) {
            assertEquals(orders[idx], items[idx].order)
        }
    }
}
