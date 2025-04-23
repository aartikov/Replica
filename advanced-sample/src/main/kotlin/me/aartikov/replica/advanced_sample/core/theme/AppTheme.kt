package me.aartikov.replica.advanced_sample.core.theme

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.view.WindowInsetsControllerCompat
import android.graphics.Color as AndroidColor

private val LightColors = lightColorScheme(
    primary = RawColors.mint,
    onPrimary = RawColors.white,
    primaryContainer = RawColors.darkMint,
    onPrimaryContainer = RawColors.white,
    secondary = RawColors.mint,
    onSecondary = RawColors.white,
    secondaryContainer = RawColors.mint,
    onSecondaryContainer = RawColors.white,
    error = RawColors.red,
    onError = RawColors.white,
    background = RawColors.white,
    onBackground = RawColors.black,
    surface = RawColors.lightGray,
    onSurface = RawColors.black
)

private val DarkColors = darkColorScheme(
    primary = RawColors.desaturatedMint,
    onPrimary = RawColors.black,
    primaryContainer = RawColors.desaturatedMint,
    onPrimaryContainer = RawColors.black,
    secondary = RawColors.desaturatedMint,
    onSecondary = RawColors.black,
    secondaryContainer = RawColors.desaturatedMint,
    onSecondaryContainer = RawColors.black,
    error = RawColors.desaturatedRed,
    onError = RawColors.black,
    background = RawColors.darkGray,
    onBackground = RawColors.white,
    surface = RawColors.darkGray,
    onSurface = RawColors.white
)

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val window = LocalActivity.current?.window

    val systemGestures = WindowInsets.systemGestures
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val isGestureNavigation by remember {
        derivedStateOf {
            systemGestures.run {
                getLeft(density, layoutDirection) > 0 && getRight(density, layoutDirection) > 0
            }
        }
    }

    SideEffect {
        window ?: return@SideEffect

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        } else {
            window.navigationBarColor = AndroidColor.TRANSPARENT
            window.statusBarColor = AndroidColor.TRANSPARENT
        }
    }

    LaunchedEffect(isDarkTheme) {
        window ?: return@LaunchedEffect

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    Box(Modifier.fillMaxSize()) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )

        Spacer(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .drawBehind {
                    when {
                        isGestureNavigation -> Color.Transparent

                        isDarkTheme || Build.VERSION.SDK_INT < Build.VERSION_CODES.O ->
                            DarkColors.background.copy(alpha = 0.6f)

                        else -> LightColors.background.copy(alpha = 0.9f)
                    }.run(::drawRect)
                }
        )
    }
}