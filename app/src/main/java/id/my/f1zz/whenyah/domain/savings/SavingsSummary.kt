package id.my.f1zz.whenyah.domain.savings

data class SavingsSummary(
    val availableIdr: Long,
    val targetIdr: Long,
    val remainingIdr: Long,
    val surplusIdr: Long,
    val progress: Float,
    val isGoalReached: Boolean,
) {
    companion object {
        val Empty = SavingsSummary(
            availableIdr = 0,
            targetIdr = 0,
            remainingIdr = 0,
            surplusIdr = 0,
            progress = 1f,
            isGoalReached = true,
        )
    }
}
