package id.my.f1zz.whenyah.navigation

import java.util.UUID

object Routes {
    const val COLLECTION_DETAIL = "collection/{collectionId}"
    const val ITEM_DETAIL = "item/{itemId}"
    const val ITEM_HISTORY = "item/{itemId}/history"
    const val ITEM_PLAN = "item/{itemId}/plan"
    const val ITEM_ADD = "add-item?collectionId={collectionId}"
    const val ITEM_EDIT = "edit-item/{itemId}"

    const val ARCHIVE = "archive"
    const val ARCHIVE_DETAIL = "archive/{itemId}"
    const val ACTIVITY = "activity"

    fun collectionDetail(collectionId: UUID): String = "collection/$collectionId"

    fun itemDetail(itemId: UUID): String = "item/$itemId"

    fun itemHistory(itemId: UUID): String = "item/$itemId/history"

    fun itemPlan(itemId: UUID): String = "item/$itemId/plan"

    fun itemAdd(collectionId: UUID? = null): String =
        if (collectionId == null) "add-item" else "add-item?collectionId=$collectionId"

    fun itemEdit(itemId: UUID): String = "edit-item/$itemId"

    fun archiveDetail(itemId: UUID): String = "archive/$itemId"
}
