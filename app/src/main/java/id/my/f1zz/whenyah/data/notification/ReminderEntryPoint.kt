package id.my.f1zz.whenyah.data.notification

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderEntryPoint {
    fun reminderCoordinator(): ReminderCoordinator
}
