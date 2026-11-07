package id.my.f1zz.whenyah.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing

object AppMotion {
    const val FastMillis = 150
    const val MediumMillis = 250
    const val SlowMillis = 400

    val StandardEasing = FastOutSlowInEasing
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
}
