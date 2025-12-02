package duran.josue.esptemp.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// Objeto de datos que representa la estructura en Firebase
@com.google.firebase.database.IgnoreExtraProperties
data class RealtimeData(
    val temperature: Float = 0f,
    val humidity: Float = 0f
)

// Objeto para datos en el historial
data class HistoryEntry(
    val timestamp: Long = 0L,
    val temperature: Float = 0f,
    val humidity: Float = 0f
)

// Objeto de estado de la UI, ahora con modo de control
data class SensorUiState(
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val time: String = "--:--",
    val isConnected: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val history: List<HistoryEntry> = emptyList(),
    val controlMode: String = "auto",
    val ledIsOn: Boolean = false
)

class SensorViewModel : ViewModel() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val sensorDataRef = database.getReference("sensores/datos")
    private val historyDataRef = database.getReference("sensores/historial").limitToLast(20)
    //referencias para el control
    private val modeControlRef = database.getReference("sensores/control/mode")
    private val ledControlRef = database.getReference("sensores/control/led")

    private val _uiState = MutableStateFlow(SensorUiState())
    val uiState: StateFlow<SensorUiState> = _uiState

    private var currentValueListener: ValueEventListener? = null
    private var historyValueListener: ValueEventListener? = null
    private var modeListener: ValueEventListener? = null
    private var ledListener: ValueEventListener? = null

    init {
        startTimeUpdater()
        attachDatabaseReadListeners()
    }

    // Funciones para modificar el estado en Firebase
    fun setControlMode(isManual: Boolean) {
        val newMode = if (isManual) "manual" else "auto"
        modeControlRef.setValue(newMode)
    }

    fun setLedStatus(isOn: Boolean) {
        ledControlRef.setValue(isOn)
    }

    private fun startTimeUpdater() {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(time = java.time.LocalTime.now().withNano(0).toString()) }
                delay(1000)
            }
        }
    }

    private fun attachDatabaseReadListeners() {
        //para datos sin cambios
        currentValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue<RealtimeData>()?.let {
                    _uiState.update { state ->
                        state.copy(temperature = it.temperature, humidity = it.humidity, isConnected = true, isLoading = false, error = null)
                    }
                } ?: _uiState.update { it.copy(isConnected = false, isLoading = false) }
            }
            override fun onCancelled(error: DatabaseError) { _uiState.update { it.copy(error = "Error: ${error.message}") } }
        }
        sensorDataRef.addValueEventListener(currentValueListener!!)

        //para historial sin cambios
        historyValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { data ->
                    data.getValue<RealtimeData>()?.let { HistoryEntry(data.key?.toLongOrNull() ?: 0L, it.temperature, it.humidity) }
                }
                _uiState.update { it.copy(history = list) }
            }
            override fun onCancelled(error: DatabaseError) { _uiState.update { it.copy(error = "Error historial: ${error.message}") } }
        }
        historyDataRef.addValueEventListener(historyValueListener!!)

        //panel de control
        modeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val mode = snapshot.getValue<String>() ?: "auto"
                _uiState.update { it.copy(controlMode = mode) }
            }
            override fun onCancelled(error: DatabaseError) { _uiState.update { it.copy(error = "Error modo: ${error.message}") } }
        }
        modeControlRef.addValueEventListener(modeListener!!)

        ledListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = snapshot.getValue<Boolean>() ?: false
                _uiState.update { it.copy(ledIsOn = isOn) }
            }
            override fun onCancelled(error: DatabaseError) { _uiState.update { it.copy(error = "Error LED: ${error.message}") } }
        }
        ledControlRef.addValueEventListener(ledListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        currentValueListener?.let { sensorDataRef.removeEventListener(it) }
        historyValueListener?.let { historyDataRef.removeEventListener(it) }
        modeListener?.let { modeControlRef.removeEventListener(it) }
        ledListener?.let { ledControlRef.removeEventListener(it) }
    }
}
