package id.my.f1zz.whenyah.data.repository

import id.my.f1zz.whenyah.data.local.entity.WishlistItemEntity
import id.my.f1zz.whenyah.domain.savings.SavingsSummary

data class WishlistItemWithSavings(
    val item: WishlistItemEntity,
    val savings: SavingsSummary,
)
