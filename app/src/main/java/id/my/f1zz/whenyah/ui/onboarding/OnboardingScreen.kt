package id.my.f1zz.whenyah.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import id.my.f1zz.whenyah.domain.i18n.AppLanguage
import id.my.f1zz.whenyah.domain.i18n.AppStrings
import id.my.f1zz.whenyah.ui.i18n.LocalAppStrings
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

@Composable
fun OnboardingScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val pages = remember(strings) {
        listOf(
            OnboardingPage(
                icon = Icons.Outlined.Savings,
                title = strings.onboardingTitle1,
                body = strings.onboardingBody1,
            ),
            OnboardingPage(
                icon = Icons.Outlined.EventRepeat,
                title = strings.onboardingTitle2,
                body = strings.onboardingBody2,
            ),
            OnboardingPage(
                icon = Icons.Outlined.Insights,
                title = strings.onboardingTitle3,
                body = strings.onboardingBody3,
            ),
        )
    }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    BackHandler(enabled = pagerState.currentPage > 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = strings.appName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                LanguageMenu(
                    currentLanguage = language,
                    strings = strings,
                    onLanguageChange = onLanguageChange,
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    pageIndex = page,
                    pagerState = pagerState,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .semantics {
                        contentDescription = strings.pageIndicator(
                            pagerState.currentPage + 1,
                            pages.size,
                        )
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.indices.forEach { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (selected) 28.dp else 8.dp,
                        label = "onboardingIndicatorWidth",
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(width)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                    ) {
                        Text(strings.onboardingBack)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isLastPage) {
                    TextButton(onClick = onFinish) {
                        Text(strings.onboardingSkip)
                    }
                }
            }

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(if (isLastPage) strings.onboardingStart else strings.onboardingNext)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isLastPage) {
                        Icons.Filled.Check
                    } else {
                        Icons.AutoMirrored.Filled.ArrowForward
                    },
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    pagerState: androidx.compose.foundation.pager.PagerState,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .graphicsLayer {
                val pageOffset = abs(
                    (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                ).coerceIn(0f, 1f)
                val scale = lerp(0.92f, 1f, 1f - pageOffset)
                scaleX = scale
                scaleY = scale
                alpha = lerp(0.55f, 1f, 1f - pageOffset)
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Surface(
                modifier = Modifier.size(168.dp),
                shape = RoundedCornerShape(48.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                    )
                }
            }

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LanguageMenu(
    currentLanguage: AppLanguage,
    strings: AppStrings,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = if (currentLanguage == AppLanguage.ENGLISH) {
        strings.languageEnglish
    } else {
        strings.languageIndonesian
    }

    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(currentLabel) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(strings.languageEnglish) },
                onClick = {
                    expanded = false
                    onLanguageChange(AppLanguage.ENGLISH)
                },
                trailingIcon = if (currentLanguage == AppLanguage.ENGLISH) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else {
                    null
                },
            )
            DropdownMenuItem(
                text = { Text(strings.languageIndonesian) },
                onClick = {
                    expanded = false
                    onLanguageChange(AppLanguage.INDONESIAN)
                },
                trailingIcon = if (currentLanguage == AppLanguage.INDONESIAN) {
                    { Icon(Icons.Filled.Check, contentDescription = null) }
                } else {
                    null
                },
            )
        }
    }
}
