package com.example.apphandroll

// NUEVO: splash intro - pantalla de presentación animada para "Arma Tu Handroll".
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// NUEVO: splash intro - composable principal con animaciones configurables.
@Composable
fun SplashIntroScreen(
    enableMotion: Boolean = true,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    // NUEVO: splash intro - detectar escala global de animaciones para respetar accesibilidad.
    val animatorScale = remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
        }.getOrDefault(1f)
    }
    val motionEnabled = enableMotion && animatorScale != 0f

    var animateIn by remember { mutableStateOf(!motionEnabled) }
    var fadeOut by remember { mutableStateOf(false) }

    // NUEVO: splash intro - orquestar la secuencia temporal (aprox. 2.2 s).
    LaunchedEffect(motionEnabled) {
        animateIn = true
        if (motionEnabled) {
            delay(1800)
            fadeOut = true
            delay(400)
        } else {
            delay(2200)
        }
        onFinished()
    }

    // NUEVO: splash intro - animaciones de entrada/salida del contenido.
    val logoScale by animateFloatAsState(
        targetValue = when {
            !motionEnabled -> 1f
            animateIn -> 1f
            else -> 0.85f
        },
        animationSpec = if (motionEnabled) {
            tween(durationMillis = 600, easing = FastOutSlowInEasing)
        } else {
            snap()
        },
        label = "splashLogoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = when {
            !motionEnabled -> 1f
            animateIn -> 1f
            else -> 0f
        },
        animationSpec = if (motionEnabled) {
            tween(durationMillis = 600, easing = LinearEasing)
        } else {
            snap()
        },
        label = "splashLogoAlpha"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = when {
            !motionEnabled -> 1f
            fadeOut -> 0f
            else -> 1f
        },
        animationSpec = if (motionEnabled) {
            tween(durationMillis = 400, easing = LinearEasing)
        } else {
            snap()
        },
        label = "splashContentAlpha"
    )

    // NUEVO: splash intro - transiciones infinitas para letras y emojis.
    val infiniteTransition = rememberInfiniteTransition(label = "splashInfiniteTransition")
    val sushiScale = animatedValue(
        motionEnabled = motionEnabled,
        defaultValue = 1f
    ) {
        infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splashSushiScale"
        ).value
    }
    val chopsticksRotation = animatedValue(
        motionEnabled = motionEnabled,
        defaultValue = 0f
    ) {
        infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splashChopsticksRotation"
        ).value
    }
    val sparkleAlpha = animatedValue(
        motionEnabled = motionEnabled,
        defaultValue = 1f
    ) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splashSparkleAlpha"
        ).value
    }
    val sparkleShift = animatedValue(
        motionEnabled = motionEnabled,
        defaultValue = 0f
    ) {
        infiniteTransition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splashSparkleShift"
        ).value
    }
    val waveShift = animatedValue(
        motionEnabled = motionEnabled,
        defaultValue = 0f
    ) {
        infiniteTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "splashWaveShift"
        ).value
    }

    // NUEVO: splash intro - superficie usando la paleta actual.
    val density = LocalDensity.current
    val sparkleShiftPx = with(density) { sparkleShift.dp.toPx() }
    val waveShiftPx = with(density) { waveShift.dp.toPx() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .graphicsLayer(alpha = contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // NUEVO: splash intro - logo opcional con fade + scale.
            val logoId = remember {
                context.resources.getIdentifier("ath", "mipmap", context.packageName)
            }
            if (logoId != 0) {
                Image(
                    painter = painterResource(id = logoId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .graphicsLayer(
                            alpha = logoAlpha,
                            scaleX = logoScale,
                            scaleY = logoScale
                        )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // NUEVO: splash intro - bloque central con título y emojis animados.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                AnimatedSplashTitle(
                    text = "Arma Tu Handroll",
                    motionEnabled = motionEnabled
                )
                Text(
                    text = "🍣",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-28).dp, y = (-36).dp)
                        .graphicsLayer(
                            scaleX = sushiScale,
                            scaleY = sushiScale
                        )
                )
                Text(
                    text = "🥢",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 28.dp, y = (-24).dp)
                        .graphicsLayer(
                            rotationZ = chopsticksRotation
                        )
                )
                Text(
                    text = "✨",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 16.dp, y = 24.dp)
                        .graphicsLayer(
                            alpha = sparkleAlpha,
                            translationY = sparkleShiftPx
                        )
                )
                Text(
                    text = "🌊",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-32).dp, y = 28.dp)
                        .graphicsLayer(
                            translationX = waveShiftPx
                        )
                )
            }

        }
    }
}

// NUEVO: splash intro - helper para animaciones opcionales según accesibilidad.
private fun <T> animatedValue(
    motionEnabled: Boolean,
    defaultValue: T,
    calculate: () -> T
): T {
    return if (motionEnabled) calculate() else defaultValue
}

// NUEVO: splash intro - título animado letra por letra.
@Composable
private fun AnimatedSplashTitle(
    text: String,
    motionEnabled: Boolean
) {
    val characters = remember(text) { text.toCharArray().toList() }
    val infiniteTransition = rememberInfiniteTransition(label = "splashTitleTransition")
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        characters.forEachIndexed { index, char ->
            if (char == ' ') {
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                val offset = animatedValue(
                    motionEnabled = motionEnabled,
                    defaultValue = 0f
                ) {
                    infiniteTransition.animateFloat(
                        initialValue = -6f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = 1000,
                                delayMillis = index * 70,
                                easing = FastOutSlowInEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "splashTitleOffset$index"
                    ).value
                }
                Text(
                    text = char.toString(),
                    color = onSurface,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.offset(y = offset.dp)
                )
            }
        }
    }
}

