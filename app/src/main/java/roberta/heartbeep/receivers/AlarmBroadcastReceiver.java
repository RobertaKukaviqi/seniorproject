package roberta.heartbeep.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import roberta.heartbeep.Utilities.Helper;
import roberta.heartbeep.services.BackgroundHeartBeatService;

public class AlarmBroadcastReceiver extends BroadcastReceiver {


    private static final String TAG = "AlarmBroadcastReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {

        if(!Helper.getInstance().shouldStopMeasuring(context)) {
            intent = new Intent(context, AlarmBroadcastReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 2121, intent, 0);

            //repeat alarm
            long futureInMillis = SystemClock.elapsedRealtime() + 180000;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, alarmIntent);

            intent = new Intent(context, BackgroundHeartBeatService.class);
            context.startService(intent);
        }else{
            Helper.getInstance().stopMeasuring(context, false);
        }
    }

}
