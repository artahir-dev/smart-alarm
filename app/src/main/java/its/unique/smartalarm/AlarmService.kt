package its.unique.smartalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.TaskStackBuilder
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat

///////////////////////////////////////////////////////////////////////////////////////////////////
class AlarmService : Service() {

    ///////////////////////////////////////////////////////////////////
    private lateinit var player: MediaPlayer
    private val channelId = "123"

    ///////////////////////////////////////////////////////////////////
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    ///////////////////////////////////////////////////////////////////
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification()
        playMusic()
        return START_STICKY
    }

    ///////////////////////////////////////////////////////////////////
    private fun showNotification() {

        val intent = Intent(this, StopAlarmActivity::class.java)
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alarm")
            .setContentText("Wake up...\nClick to stop it...")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    ///////////////////////////////////////////////////////////////////
    private fun playMusic() {

        player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI)
        player.isLooping = true
        player.start()
    }

    ///////////////////////////////////////////////////////////////////
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "My Channel 123",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    ///////////////////////////////////////////////////////////////////
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    ///////////////////////////////////////////////////////////////////
    override fun onDestroy() {
        super.onDestroy()
        player.stop()
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////
