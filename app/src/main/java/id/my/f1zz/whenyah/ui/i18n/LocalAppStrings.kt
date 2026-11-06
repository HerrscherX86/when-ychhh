package id.my.f1zz.whenyah.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.domain.i18n.EnglishStrings

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { EnglishStrings }
