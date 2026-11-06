package id.my.f1zz.whenyah.domain.purchase

data class PurchaseSpendPlan(
    val savingsSpentIdr: Long,
    val shouldWriteSpendEvent: Boolean,
)

object PurchaseAccounting {
    fun plan(availableBalanceIdr: Long): PurchaseSpendPlan {
        require(availableBalanceIdr >= 0) { "Available balance cannot be negative" }
        return PurchaseSpendPlan(
            savingsSpentIdr = availableBalanceIdr,
            shouldWriteSpendEvent = availableBalanceIdr > 0,
        )
    }
}
