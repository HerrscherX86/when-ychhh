package id.my.f1zz.whenyah.domain.plan

import java.time.LocalDate

data class SavingsPlanProjection(
    val remainingIdr: Long,
    val requiredContributions: Long,
    val completionDate: LocalDate?,
    val isGoalReached: Boolean,
)
