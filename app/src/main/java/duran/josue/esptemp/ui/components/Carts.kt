package duran.josue.esptemp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import duran.josue.esptemp.data.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun ConnectionCard(
    deviceName: String,
    ip: String,
    time: String,
    isConnected: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_indicator")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColorTarjetaControl)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "WiFi",
                    tint = when {
                        isLoading -> Color.Gray
                        isConnected -> Color(0xFF4CAF50)
                        else -> Color.Red
                    },
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = when {
                            isLoading -> "Cargando..."
                            isConnected -> "Conectado"
                            else -> "Esperando datos..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isLoading -> Color.Gray
                            isConnected -> Color.Black
                            else -> Color.Red
                        }
                    )
                    Text(
                        text = "$deviceName $ip",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}


val backgroundColorTarjeta = Color(0xFFE8B39C)

@Composable
fun DataCard(
    icon: ImageVector,
    value: String,
    unit: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColorTarjeta)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = MaterialTheme.typography.displaySmall)
                Text(text = unit, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}


@Composable
fun GraphCardMPChart(history: List<HistoryEntry>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp).background(Color.White)) {
            Text("Historial de Datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            AndroidView(
                modifier = Modifier.fillMaxWidth().background(Color.White).height(250.dp),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    if (history.isEmpty()) return@AndroidView
                    val tempValues = history.mapIndexed { index, entry -> Entry(index.toFloat(), entry.temperature) }
                    val humValues = history.mapIndexed { index, entry -> Entry(index.toFloat(), entry.humidity) }
                    val tempSet = LineDataSet(tempValues, "Temp (°C)").apply {
                        color = android.graphics.Color.RED
                        lineWidth = 2f
                    }
                    val humSet = LineDataSet(humValues, "Humedad (%)").apply {
                        color = android.graphics.Color.BLUE
                        lineWidth = 2f
                    }
                    chart.data = LineData(tempSet, humSet)
                    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            if (index !in history.indices) return ""
                            return dateFormat.format(Date(history[index].timestamp * 1000))
                        }
                    }
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.invalidate()
                }
            )
        }
    }
}


// control con modos
val backgroundColorTarjetaControl = Color(0xFFE1E0E7)

@Composable
fun ControlCard(
    isManualMode: Boolean,
    ledIsOn: Boolean,
    onModeChange: (Boolean) -> Unit,
    onLedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColorTarjetaControl)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Panel de Control", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            // fila para el modo auto y manual
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Modo Manual", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isManualMode, onCheckedChange = onModeChange)
            }

            Spacer(Modifier.height(8.dp))

            // fila para el control del led
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Activar LED",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isManualMode) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Switch(checked = ledIsOn, onCheckedChange = onLedChange, enabled = isManualMode)
            }
        }
    }
}