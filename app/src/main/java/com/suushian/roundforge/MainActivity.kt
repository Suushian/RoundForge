package com.suushian.roundforge

import android.os.Bundle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val Ink = Color(0xFF090B0D)
private val Panel = Color(0xFF15191D)
private val Accent = Color(0xFFC7FF4A)
private val Muted = Color(0xFF858D96)
private val White = Color(0xFFF4F6F2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 11, 13)
        window.navigationBarColor = android.graphics.Color.rgb(9, 11, 13)
        setContent { RoundForgeApp() }
    }
}

@Composable
private fun RoundForgeApp() {
    var roundLength by remember { mutableIntStateOf(180) }
    var restLength by remember { mutableIntStateOf(60) }
    var roundsTotal by remember { mutableIntStateOf(5) }
    var roundNumber by remember { mutableIntStateOf(1) }
    var secondsLeft by remember { mutableIntStateOf(roundLength) }
    var isRest by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(running, secondsLeft, isRest, roundNumber) {
        if (running && !finished) {
            delay(1000)
            if (secondsLeft > 1) {
                secondsLeft -= 1
            } else if (isRest) {
                isRest = false
                secondsLeft = roundLength
            } else if (roundNumber >= roundsTotal) {
                secondsLeft = 0
                running = false
                finished = true
            } else {
                isRest = true
                secondsLeft = restLength
                roundNumber += 1
            }
        }
    }

    val fullLength = if (isRest) restLength else roundLength
    val progress by animateFloatAsState(
        targetValue = if (fullLength > 0) secondsLeft.toFloat() / fullLength else 0f,
        label = "timer-progress"
    )

    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("ROUND", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Text("FORGE", color = White, fontSize = 21.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Box(Modifier.background(Panel, RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 9.dp)) {
                        Text("TRAINING MODE", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(30.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(if (finished) "SESSION COMPLETE" else if (isRest) "RECOVERY" else "FIGHT ROUND", color = if (isRest) Color(0xFF71C8FF) else Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(if (finished) "Well fought." else if (isRest) "Catch your breath." else "Stay sharp. Stay ready.", color = Muted, fontSize = 13.sp)
                    }
                    Text("${roundNumber.toString().padStart(2, '0')} / ${roundsTotal.toString().padStart(2, '0')}", color = White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(26.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(286.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(color = Panel, style = Stroke(width = 10.dp.toPx()))
                        drawArc(color = if (isRest) Color(0xFF71C8FF) else Accent, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (isRest) "REST TIME" else "TIME LEFT", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("%02d:%02d".format(secondsLeft / 60, secondsLeft % 60), color = White, fontSize = 62.sp, fontWeight = FontWeight.Light, letterSpacing = (-2).sp)
                        Text(if (running) "IN PROGRESS" else if (finished) "ALL ROUNDS DONE" else "READY WHEN YOU ARE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("ROUNDS", roundsTotal.toString(), Modifier.weight(1f))
                    StatCard("ROUND TIME", "${roundLength / 60} MIN", Modifier.weight(1f))
                    StatCard("REST", "${restLength} SEC", Modifier.weight(1f))
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            if (finished) {
                                finished = false
                                roundNumber = 1
                                isRest = false
                                secondsLeft = roundLength
                            }
                            running = !running
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)
                    ) {
                        Text(if (running) "PAUSE" else if (finished) "START AGAIN" else "START SESSION", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                    Button(
                        onClick = { running = false; finished = false; isRest = false; roundNumber = 1; secondsLeft = roundLength },
                        modifier = Modifier.size(58.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Panel, contentColor = White)
                    ) { Text("↺", fontSize = 24.sp) }
                }
                Spacer(Modifier.height(14.dp))
                TextButton(onClick = {
                    if (!running) {
                        roundLength = if (roundLength == 180) 120 else if (roundLength == 120) 300 else 180
                        secondsLeft = if (isRest) restLength else roundLength
                    }
                }) {
                    Text("ROUND PRESET  ·  ${roundLength / 60} MIN   /   TAP TO CHANGE", color = Muted, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                Spacer(Modifier.weight(1f))
                Text("BUILT FOR THE WORK.", color = Color(0xFF414950), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(16.dp)).padding(vertical = 16.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
