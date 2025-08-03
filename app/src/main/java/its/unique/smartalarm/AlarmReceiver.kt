package its.unique.smartalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        context?.startForegroundService(Intent(context, AlarmService::class.java))
    }
}