package id.my.f1zz.whenyah.domain.plan

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

/**
 * Emits the device-local date immediately and revisits it once a minute so
 * projections roll over when the calendar day changes.
 */
fun observeToday(): Flow<LocalDate> = flow {
    while (true) {
        emit(LocalDate.now())
        delay(60_000L)
    }
}.distinctUntilChanged()
