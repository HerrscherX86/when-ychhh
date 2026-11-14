package id.my.f1zz.whenyah.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.my.f1zz.whenyah.data.repository.WishlistRepository
import id.my.f1zz.whenyah.data.settings.SettingsRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun wishlistRepository(): WishlistRepository
    fun settingsRepository(): SettingsRepository
}
