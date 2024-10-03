package com.omgodse.notally.room

data class ListItem(
    var body: String,
    var checked: Boolean,
    var isChild: Boolean,
    var sortingPosition: Int?,
    var children: MutableList<ListItem>,
    var id: Int = -1,
) : Cloneable {
    //    public override fun clone(): Any {
    //        return ListItem(body, checked, isChild, sortingPosition, children.toMutableList(), id)
    //    }

    public override fun clone(): Any {
        // Deep clone!
        return ListItem(
            body,
            checked,
            isChild,
            sortingPosition,
            children.map { it.clone() as ListItem }.toMutableList(),
            id,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this == null && other == null) {
            return true
        }
        if (this != null && other == null) {
            return false
        }
        if (this == null && other != null) {
            return false
        }
        if (other !is ListItem) {
            return false
        }
        return (this.body == other.body &&
            this.checked == other.checked &&
            this.isChild == other.isChild)
    }

    val itemCount: Int
        get() = children.size + 1

    fun isChildOf(other: ListItem): Boolean {
        return !other.isChild && other.children.contains(this)
    }

    override fun toString(): String {
        return "${if (isChild) " >" else ""}${if (checked) "x" else ""} ${body}${
            if (children.isNotEmpty()) "(${
                children.map { it.body }.joinToString(",")
            })" else ""
        }"
    }
}
