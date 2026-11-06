package id.my.f1zz.whenyah.domain.savings

import id.my.f1zz.whenyah.domain.model.SavingsEventType

interface SavingsLedgerEntry {
    val type: SavingsEventType
    val amountIdr: Long
}
