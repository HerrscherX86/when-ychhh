package id.my.f1zz.whenyah.data.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LaunchRequest {
    data class OpenItem(
        val itemId: UUID,
        val depositAmountIdr: Long? = null,
    ) : LaunchRequest

    data object AddItem : LaunchRequest
}

object LaunchRequestParser {
    fun parse(
        itemId: String?,
        depositAmountIdr: Long?,
        openAddItem: Boolean,
    ): LaunchRequest? {
        if (openAddItem) return LaunchRequest.AddItem

        val parsedId = itemId
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: return null
        return LaunchRequest.OpenItem(
            itemId = parsedId,
            depositAmountIdr = depositAmountIdr?.takeIf { it > 0 },
        )
    }
}

@Singleton
class AppLauncher @Inject constructor() {
    private val _request = MutableStateFlow<LaunchRequest?>(null)
    val request: StateFlow<LaunchRequest?> = _request.asStateFlow()

    fun openItem(itemId: UUID, depositAmountIdr: Long? = null) {
        _request.value = LaunchRequest.OpenItem(itemId, depositAmountIdr)
    }

    fun addItem() {
        _request.value = LaunchRequest.AddItem
    }

    fun consume() {
        _request.value = null
    }
}
