package id.my.f1zz.whenyah.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

object AppMotion {
    const val FastMillis = 150
    const val MediumMillis = 250
    const val SlowMillis = 400

    /** Fade-through timing: old content leaves first, then new content follows. */
    const val FadeOutMillis = 180
    const val FadeInMillis = 420

    /** Top-level tab fade-through, slightly slower than detail navigation. */
    const val TabFadeOutMillis = 150
    const val TabFadeInMillis = 350

    val StandardEasing = FastOutSlowInEasing
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val FadeOutEasing = FastOutLinearInEasing
    val FadeInEasing = LinearOutSlowInEasing
}
