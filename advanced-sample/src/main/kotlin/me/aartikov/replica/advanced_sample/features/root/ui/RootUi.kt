package me.aartikov.replica.advanced_sample.features.root.ui

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import com.arkivanov.decompose.extensions.compose.stack.Children
import me.aartikov.replica.advanced_sample.core.message.ui.MessageUi
import me.aartikov.replica.advanced_sample.core.theme.AppTheme
import me.aartikov.replica.advanced_sample.core.theme.RawColors
import me.aartikov.replica.advanced_sample.features.dudes.ui.DudesUi
import me.aartikov.replica.advanced_sample.features.fruits.ui.FruitsUi
import me.aartikov.replica.advanced_sample.features.menu.ui.MenuUi
import me.aartikov.replica.advanced_sample.features.pokemons.ui.PokemonsUi
import me.aartikov.replica.advanced_sample.features.project.ui.ProjectUi
import android.graphics.Color as AndroidColor

@Composable
fun RootUi(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val childStack by component.childStack.collectAsState()

    Children(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        stack = childStack
    ) { child ->
        when (val instance = child.instance) {
            is RootComponent.Child.Menu -> MenuUi(instance.component)
            is RootComponent.Child.Project -> ProjectUi(instance.component)
            is RootComponent.Child.Pokemons -> PokemonsUi(instance.component)
            is RootComponent.Child.Fruits -> FruitsUi(instance.component)
            is RootComponent.Child.Dudes -> DudesUi(instance.component)
        }
    }

    MessageUi(
        component = component.messageComponent,
        modifier = modifier,
        bottomPadding = 16.dp
    )

    ConfigureSystemBars()
}

@Composable
private fun ConfigureSystemBars() {
    val isDarkTheme = isSystemInDarkTheme()

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
        Spacer(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .drawBehind {
                    when {
                        isGestureNavigation -> Color.Transparent

                        isDarkTheme || Build.VERSION.SDK_INT < Build.VERSION_CODES.O ->
                            RawColors.darkGray.copy(alpha = 0.6f)

                        else -> RawColors.white.copy(alpha = 0.9f)
                    }.run(::drawRect)
                }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RootUiPreview() {
    AppTheme {
        RootUi(FakeRootComponent())
    }
}
