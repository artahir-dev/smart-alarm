package its.unique.smartalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar

//////////////////////////////////////////////////////////////////////////////////////////////////
class MainActivity : AppCompatActivity() {

    //////////////////////////////////////////////////////////////
    private lateinit var alarmManager: AlarmManager
    private lateinit var calendar: Calendar
    private lateinit var timePicker: MaterialTimePicker
    private var code: Int = 0

    //////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {

        /////////////////////////////////
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        calendar = Calendar.getInstance()
        initializeTimePicker()

        /////////////////////////////////
        findViewById<ImageButton>(R.id.set).setOnClickListener {
            showTimePicker()
        }

        /////////////////////////////////

    }

    //////////////////////////////////////////////////////////////
    private fun initializeTimePicker() {

        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText("Select alarm time")
            .build()

        timePicker.addOnPositiveButtonClickListener {

            calendar[Calendar.HOUR_OF_DAY] = timePicker.hour
            calendar[Calendar.MINUTE] = timePicker.minute
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0

            setAlarm()
            Toast.makeText(this, "Alarm set...", Toast.LENGTH_SHORT).show()
        }
    }

    //////////////////////////////////////////////////////////////
    private fun showTimePicker() {

        timePicker.show(supportFragmentManager, "foxandroid")
    }

    //////////////////////////////////////////////////////////////
    private fun setAlarm() {

        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, code++, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
//////////////////////////////////////////////////////////////////////////////////////////////////
