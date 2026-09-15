package com.msterjime.zikirdua

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val KAABA_LATITUDE = 21.422487
private const val KAABA_LONGITUDE = 39.826206

private fun normalizeDegrees(value: Float): Float {
    var result = value % 360f
    if (result < 0f) result += 360f
    return result
}

fun calculateQiblaBearing(latitude: Double, longitude: Double): Float {
    val lat1 = Math.toRadians(latitude)
    val lat2 = Math.toRadians(KAABA_LATITUDE)
    val deltaLon = Math.toRadians(KAABA_LONGITUDE - longitude)

    val y = sin(deltaLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)
    return normalizeDegrees(Math.toDegrees(atan2(y, x)).toFloat())
}

@Composable
fun QiblaScreen(
    cityName: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val rotationSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    var heading by remember { mutableFloatStateOf(0f) }
    val qiblaBearing = remember(latitude, longitude) {
        calculateQiblaBearing(latitude, longitude)
    }

    DisposableEffect(rotationSensor) {
        if (rotationSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val rotationMatrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    heading = normalizeDegrees(Math.toDegrees(orientation[0].toDouble()).toFloat())
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }

            sensorManager.registerListener(
                listener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val arrowRotation = normalizeDegrees(qiblaBearing - heading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Направление киблы",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "📍 $cityName",
            color = Color(0xFF2F6B57),
            fontSize = 14.sp
        )

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF173F35))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Кибла ${qiblaBearing.toInt()}° от севера",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(Color(0xFFEAF2EE), RoundedCornerShape(95.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "↑",
                        modifier = Modifier.rotate(arrowRotation),
                        fontSize = 112.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC8A95B)
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text(
                    text = if (rotationSensor != null) {
                        "Поверните телефон так, чтобы стрелка смотрела прямо вверх."
                    } else {
                        "Компас на этом устройстве недоступен. Используйте угол киблы относительно севера."
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5D9))
        ) {
            Text(
                text = "Для точности держите телефон горизонтально и подальше от металла. Если стрелка ведёт себя нестабильно, сделайте телефоном несколько движений в форме восьмёрки для калибровки компаса.",
                modifier = Modifier.padding(14.dp),
                color = Color(0xFF6B5722),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
