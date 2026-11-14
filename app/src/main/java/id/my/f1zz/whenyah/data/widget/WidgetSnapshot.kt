package id.my.f1zz.whenyah.data.widget

import java.util.UUID

data class WidgetGoal(
    val itemId: UUID,
    val name: String,
    val savedIdr: Long,
    val targetIdr: Long,
    val remainingIdr: Long,
    val progress: Float,
)

data class WidgetSnapshot(
    val activeItemCount: Int = 0,
    val totalSavedIdr: Long = 0,
    val totalTargetIdr: Long = 0,
    val overallProgress: Float = 0f,
    val fullyFundedCount: Int = 0,
    val closestGoal: WidgetGoal? = null,
    val readyToBuy: List<WidgetGoal> = emptyList(),
) {
    val isEmpty: Boolean get() = activeItemCount == 0
}
