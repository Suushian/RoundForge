package com.suushian.roundforge

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private val Ink = Color(0xFF090B0D)
private val Panel = Color(0xFF15191D)
private val Accent = Color(0xFFC7FF4A)
private val Muted = Color(0xFF858D96)
private val White = Color(0xFFF4F6F2)
private val RestBlue = Color(0xFF71C8FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 11, 13)
        window.navigationBarColor = android.graphics.Color.rgb(9, 11, 13)
        setContent { RoundForgeApp() }
    }
}

private fun playCue(context: Context, finish: Boolean = false) {
    fun tone(type: Int, duration: Int) {
        val generator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
        generator.startTone(type, duration)
        Handler(Looper.getMainLooper()).postDelayed({ generator.release() }, duration.toLong() + 120L)
    }
    if (finish) {
        tone(ToneGenerator.TONE_PROP_ACK, 180)
        Handler(Looper.getMainLooper()).postDelayed({
            tone(ToneGenerator.TONE_PROP_ACK, 320)
        }, 260L)
    } else {
        tone(ToneGenerator.TONE_PROP_BEEP, 240)
    }
}

/**
 * Synthesizes a short metallic competition bell. The inharmonic partials and
 * decaying envelope give it a resonant gong-like character without bundling
 * a copyrighted recording.
 */
private fun playRoundBell() {
    Thread {
        val sampleRate = 44100
        val durationMs = 2100
        val sampleCount = sampleRate * durationMs / 1000
        val pcm = ByteArray(sampleCount * 2)
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / sampleRate
            val attack = 1.0 - exp(-t * 110.0)
            val decay = exp(-t * 2.05)
            val fundamental = sin(2.0 * PI * 920.0 * t)
            val partial2 = 0.72 * sin(2.0 * PI * 1375.0 * t + 0.2)
            val partial3 = 0.46 * sin(2.0 * PI * 1840.0 * t + 0.4)
            val partial4 = 0.24 * sin(2.0 * PI * 2470.0 * t + 0.1)
            val sample = ((fundamental + partial2 + partial3 + partial4) * attack * decay * 0.38 * 32767.0)
                .toInt().coerceIn(-32768, 32767).toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }

        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 180L)
        } catch (_: Exception) {
            // Keep audio failures from interrupting the timer.
        } finally {
            track?.release()
        }
    }.start()
}

@Composable
private fun RoundForgeApp() {
    val context = LocalContext.current
    var roundLength by remember { mutableIntStateOf(180) }
    var restLength by remember { mutableIntStateOf(60) }
    var roundsTotal by remember { mutableIntStateOf(5) }
    var roundNumber by remember { mutableIntStateOf(1) }
    var secondsLeft by remember { mutableIntStateOf(180) }
    var isRest by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(running, secondsLeft, isRest, roundNumber, finished) {
        if (!running || finished) return@LaunchedEffect
        if (secondsLeft == 3 && soundEnabled) playCue(context)
        delay(1000)
        if (secondsLeft > 1) {
            secondsLeft -= 1
        } else if (isRest) {
            isRest = false
            secondsLeft = roundLength
            roundNumber = (roundNumber + 1).coerceAtMost(roundsTotal)
            if (soundEnabled) playRoundBell()
        } else if (roundNumber >= roundsTotal) {
            secondsLeft = 0
            running = false
            finished = true
            if (soundEnabled) playCue(context, finish = true)
        } else {
            isRest = true
            secondsLeft = restLength
            if (soundEnabled) playCue(context)
        }
    }

    val phaseLength = if (isRest) restLength else roundLength
    val progress by animateFloatAsState(
        targetValue = if (phaseLength > 0) secondsLeft.toFloat() / phaseLength else 0f,
        label = "timer-progress"
    )
    val accent = if (isRest) RestBlue else Accent

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("ROUND", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Text("FORGE", color = White, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Surface(color = Panel, shape = RoundedCornerShape(50)) {
                        Text("TRAINING MODE", Modifier.padding(horizontal = 14.dp, vertical = 9.dp), color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(if (finished) "SESSION COMPLETE" else if (isRest) "RECOVERY" else "FIGHT ROUND", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(if (finished) "Well fought." else if (isRest) "Breathe. Reset. Go again." else "Stay sharp. Stay ready.", color = Muted, fontSize = 13.sp)
                    }
                    Text("${roundNumber.toString().padStart(2, '0')} / ${roundsTotal.toString().padStart(2, '0')}", color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(20.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(270.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(color = Panel, style = Stroke(width = 10.dp.toPx()))
                        drawArc(color = accent, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isRest) "REST TIME" else "TIME LEFT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("%02d:%02d".format(secondsLeft / 60, secondsLeft % 60), color = White, fontSize = 60.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                        Text(if (running) "IN PROGRESS" else if (finished) "ALL ROUNDS DONE" else "READY WHEN YOU ARE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("ROUNDS", roundsTotal.toString(), Modifier.weight(1f))
                    StatCard("ROUND", "${roundLength / 60} MIN", Modifier.weight(1f))
                    StatCard("REST", "${restLength} SEC", Modifier.weight(1f))
                }

                Spacer(Modifier.height(10.dp))
                Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        SettingRow("Rounds", roundsTotal.toString(), onMinus = { if (!running && roundsTotal > 1) { roundsTotal--; roundNumber = roundNumber.coerceAtMost(roundsTotal) } }, onPlus = { if (!running && roundsTotal < 20) roundsTotal++ })
                        HorizontalDivider(color = Color(0xFF252B30))
                        SettingRow("Round length", "${roundLength / 60} min", onMinus = { if (!running && roundLength > 60) { roundLength -= 60; if (!isRest) secondsLeft = roundLength } }, onPlus = { if (!running && roundLength < 600) { roundLength += 60; if (!isRest) secondsLeft = roundLength } })
                        HorizontalDivider(color = Color(0xFF252B30))
                        SettingRow("Rest length", "${restLength} sec", onMinus = { if (!running && restLength > 15) { restLength -= 15; if (isRest) secondsLeft = restLength } }, onPlus = { if (!running && restLength < 180) { restLength += 15; if (isRest) secondsLeft = restLength } })
                    }
                }

                Spacer(Modifier.height(14.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (finished) {
                                finished = false
                                roundNumber = 1
                                isRest = false
                                secondsLeft = roundLength
                            }
                            if (running) {
                                running = false
                                if (soundEnabled) playCue(context)
                            } else {
                                if (!isRest && soundEnabled) playRoundBell()
                                running = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)
                    ) {
                        Text(if (running) "PAUSE" else if (finished) "START AGAIN" else "START SESSION", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Button(
                        onClick = { running = false; finished = false; isRest = false; roundNumber = 1; secondsLeft = roundLength },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = White)
                    ) { Text("↺", fontSize = 24.sp) }
                }

                TextButton(onClick = { soundEnabled = !soundEnabled; if (soundEnabled) playCue(context) }) {
                    Text(if (soundEnabled) "SOUND CUES  ·  ON" else "SOUND CUES  ·  OFF", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("BUILT FOR THE WORK.", color = Color(0xFF414950), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = onMinus, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("−", color = White, fontSize = 20.sp) }
        Text(value, color = White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onPlus, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)) { Text("+", color = Accent, fontSize = 20.sp) }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(16.dp)).padding(vertical = 13.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
