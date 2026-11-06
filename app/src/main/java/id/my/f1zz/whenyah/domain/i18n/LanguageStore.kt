package id.my.f1zz.whenyah.domain.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageStore @Inject constructor() {
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    fun update(language: AppLanguage) {
        _language.value = language
    }

    fun strings(): AppStrings = AppStringsFactory.forLanguage(_language.value)
}
