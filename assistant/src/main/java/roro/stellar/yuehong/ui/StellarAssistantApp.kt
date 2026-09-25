package roro.stellar.yuehong.ui

import android.app.Activity
import android.content.Intent
import android.system.Os
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import roro.stellar.yuehong.ghostlock.GhostLockActivity

private enum class AppPage {
    ModeSelection,
}

@Composable
fun StellarAssistantApp() {
    StellarTheme {
        val context = LocalContext.current
        BackHandler { (context as? Activity)?.moveTaskToBack(true) }

        var page by rememberSaveable { mutableStateOf(AppPage.ModeSelection) }

        val kernelRelease = remember {
            runCatching { Os.uname().release }.getOrNull().orEmpty().ifBlank {
                System.getProperty("os.version", "unknown")
            }
        }
        val ghostLockKernelAvailable = remember(kernelRelease) {
            GHOSTLOCK_KERNEL_PATTERN.containsMatchIn(kernelRelease)
        }

        fun openGhostLockMode() {
            val activity = context as? Activity ?: return
            activity.startActivity(Intent(context, GhostLockActivity::class.java))
        }

        fun openStellarMode() {
            val activity = context as? Activity ?: return
            val intent = Intent().apply {
                setClassName(
                    context.packageName,
                    "roro.stellar.manager.ui.features.manager.ManagerActivity",
                )
                putExtra("route", "workspace")
            }
            activity.startActivity(intent)
        }

        fun openVivoWiredMode() {
            val activity = context as? Activity ?: return
            val intent = Intent().apply {
                setClassName(
                    context.packageName,
                    "roro.stellar.manager.ui.features.wired.VivoWiredActivity",
                )
            }
            activity.startActivity(intent)
        }

        AnimatedContent(
            targetState = page,
            transitionSpec = {
                val forward = targetState.ordinal > initialState.ordinal
                val direction = if (forward) 1 else -1
                val enter = slideInHorizontally(
                    animationSpec = tween(480, easing = FastOutSlowInEasing),
                    initialOffsetX = { width -> direction * width / 3 },
                ) + fadeIn(tween(320)) + scaleIn(tween(420), initialScale = 0.985f)
                val exit = slideOutHorizontally(
                    animationSpec = tween(420, easing = FastOutSlowInEasing),
                    targetOffsetX = { width -> -direction * width / 4 },
                ) + fadeOut(tween(260)) + scaleOut(tween(360), targetScale = 0.99f)
                enter togetherWith exit
            },
            label = "app-page-transition",
        ) { activePage ->
            when (activePage) {
                AppPage.ModeSelection -> ModeSelectionScreen(
                    ghostLockKernelAvailable = ghostLockKernelAvailable,
                    onOpenGhostLock = ::openGhostLockMode,
                    onOpenStellar = ::openStellarMode,
                    onOpenVivoWired = ::openVivoWiredMode,
                )
            }
        }
    }
}

private val GHOSTLOCK_KERNEL_PATTERN = Regex("^6\\.")
