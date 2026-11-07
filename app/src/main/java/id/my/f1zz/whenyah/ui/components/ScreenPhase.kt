package id.my.f1zz.whenyah.ui.components

/**
 * The mutually exclusive display states a data-backed screen can be in.
 * Used as the target state for crossfading between loading, empty, and content.
 */
enum class ScreenPhase {
    Loading,
    Empty,
    Content,
}
