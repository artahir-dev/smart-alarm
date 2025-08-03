package its.unique.smartalarm

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.graphics.Color
import android.os.Handler
import android.widget.ProgressBar
import androidx.biometric.BiometricManager

/////////////////////////////////////////////////////////////////////////////////////////////////
class StopAlarmActivity : AppCompatActivity(), SensorEventListener {

    ////////////////////////////////////////////////////////////////////
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: PromptInfo
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private var fingerPrint1Scanned = false
    private var fingerPrint2Scanned = false
    private var steps = 0
    private var stepsLimit = 5
    private var stepsCompleted = false

    ////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {

        /////////////////////////////////////
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_alarm)

        /////////////////////////////////////
        initialize()

        /////////////////////////////////////
        findViewById<Button>(R.id.scan).setOnClickListener {
            scan()
        }
        /////////////////////////////////////
    }

    ////////////////////////////////////////////////////////////////////
    private fun scan() {

        biometricPrompt.authenticate(promptInfo)
    }

    ////////////////////////////////////////////////////////////////////
    private fun initialize() {

        /////////////////////////////////////
        fingerPrintAvailable()

        /////////////////////////////////////
        val executor = ContextCompat.getMainExecutor(this)

        /////////////////////////////////////
        biometricPrompt = androidx.biometric.BiometricPrompt(
            this,
            executor,
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {

                ////////////////
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)


                    if (!fingerPrint1Scanned) {
                        fingerPrint1Scanned = true
                        findViewById<ProgressBar>(R.id.f1Bar).progress = 100
                        findViewById<Button>(R.id.scan).let {
                            it.isEnabled = false
                            it.setBackgroundColor(Color.parseColor("#FF6C203A"))
                        }
                    }

                    else if (!fingerPrint2Scanned) {
                        fingerPrint2Scanned = true
                        findViewById<ProgressBar>(R.id.f2Bar).progress = 100

                        val handler = Handler()
                        handler.postDelayed(Runnable { stop() }, 800)

                    }
                }
                ////////////////
            }
        )

        /////////////////////////////////////
        promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
            .setTitle("Smart Alarm")
            .setDescription("Scan the finger")
            .setNegativeButtonText("Cancel")
            .build()

        /////////////////////////////////////
        if (!isPermissionGranted()) {
            requestPermission()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(this, "No required sensors availabe\nAlarm Stopped...", Toast.LENGTH_SHORT).show()
            stop()
        }
        /////////////////////////////////////
    }

    ////////////////////////////////////////////////////////////////////
    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {

            if (fingerPrint1Scanned && !fingerPrint2Scanned && !stepsCompleted) {
                steps++
                updateStepsBar()

                if (steps == stepsLimit) {

                    stepsCompleted = true
                    findViewById<Button>(R.id.scan).let {
                        it.isEnabled = true
                        it.setBackgroundColor(Color.parseColor("#F43B7A"))
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////
    private fun updateStepsBar() {
        findViewById<ProgressBar>(R.id.stepBar).progress = (steps / stepsLimit.toDouble() * 100).toInt()
    }

    ////////////////////////////////////////////////////////////////////
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    ////////////////////////////////////////////////////////////////////
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    ////////////////////////////////////////////////////////////////////
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    ////////////////////////////////////////////////////////////////////
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                100
            )
        }
    }

    ////////////////////////////////////////////////////////////////////
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    ////////////////////////////////////////////////////////////////////
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) { }
            else {
                // permission not granted
                Toast.makeText(this, "No required sensors availabe\nAlarm Stopped...", Toast.LENGTH_SHORT).show()
                stop()
            }
        }
    }
    ////////////////////////////////////////////////////////////////////
    private fun stop() {

        val intent = Intent(this@StopAlarmActivity, AlarmService::class.java)
        stopService(intent)

        finish()
    }

    ////////////////////////////////////////////////////////////////////

    private fun fingerPrintAvailable() {

        val biometricManager = androidx.biometric.BiometricManager.from(this)
        if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "No required sensors availabe\nAlarm Stopped...", Toast.LENGTH_SHORT).show()
            stop()
        }
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////
