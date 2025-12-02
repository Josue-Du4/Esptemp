package duran.josue.esptemp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import duran.josue.esptemp.data.SensorViewModel
import duran.josue.esptemp.ui.components.ConnectionCard
import duran.josue.esptemp.ui.components.ControlCard
import duran.josue.esptemp.ui.components.DataCard
import duran.josue.esptemp.ui.components.GraphCardMPChart

@Preview
@Composable
fun MainScreen() {
    val viewModel: SensorViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        ConnectionCard(
            deviceName = "Firebase",
            ip = "Realtime DB",
            time = uiState.time,
            isConnected = uiState.isConnected,
            isLoading = uiState.isLoading,

        )

        Spacer(Modifier.height(16.dp))

        if (uiState.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = " ${uiState.error}",
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFFD32F2F)
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        DataCard(
            icon = Icons.Default.Thermostat,
            value = if (uiState.isLoading) "..." else String.format("%.1f", uiState.temperature),
            unit = "°C",
            description = "Temperatura"
        )

        Spacer(Modifier.height(16.dp))

        DataCard(
            icon = Icons.Default.WaterDrop,
            value = if (uiState.isLoading) "..." else String.format("%.1f", uiState.humidity),
            unit = "%",
            description = "Humedad"

        )
        Spacer(Modifier.height(16.dp))

        // panel de control
        ControlCard(
            isManualMode = uiState.controlMode == "manual",
            ledIsOn = uiState.ledIsOn,
            onModeChange = { isManual -> viewModel.setControlMode(isManual) },
            onLedChange = { isOn -> viewModel.setLedStatus(isOn) }
        )

        Spacer(Modifier.height(16.dp))


        GraphCardMPChart(history = uiState.history)
    }
}