package roberta.heartbeep.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.WearableExtender;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;

import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roberta.heartbeep.R;
import roberta.heartbeep.Utilities.Constants;
import roberta.heartbeep.Utilities.Helper;
import roberta.heartbeep.Utilities.Utils;
import roberta.heartbeep.activities.NotificationActivity;
import roberta.heartbeep.models.Notification;
import roberta.heartbeep.models.NotificationData;
import roberta.heartbeep.models.NotificationRequest;
import roberta.heartbeep.retrofit.NetworkInterface;
import roberta.heartbeep.retrofit.RetrofitHelper;

public class BackgroundHeartBeatService extends Service implements SensorEventListener{

    IBinder mBinder = new LocalBinder();
    private SensorManager mSensorManager;
    private  Sensor mHeartRateSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        startMeasure();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopMeasure();
                stopSelf();
            }
        }, 8000);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            int stopMeasuring = intent.getIntExtra(Constants.STOP_MEASURE_INTENT, 0);
            Log.e("StopMeasuring", " " + stopMeasuring);

            if (stopMeasuring == 1) {
                stopMeasure();
                stopSelf();
            }

        }catch (Exception e){

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.e("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));

        Intent i = new Intent("AlarmBroadcastReceiver").putExtra("measure_start", true);
        sendBroadcast(i);
        //vibrate();
    }


    private void stopMeasure() {
        if(mSensorManager != null){
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        }
        mSensorManager.unregisterListener(this, mHeartRateSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float mHeartRateFloat = sensorEvent.values[0];
        int mHeartRate = Math.round(mHeartRateFloat);

        Helper.getInstance().setHeartRateValue(this, mHeartRate);
        if(mHeartRate != 0 && mHeartRate < 50){
            //handle low blood pressure
            startAlertActivity(mHeartRate);
            createNotification(true, mHeartRate);
            broadcastNotification(true);
            stopMeasure();
            stopSelf();
        }else if(mHeartRate > 140){
            //handle high blood pressure
            startAlertActivity(mHeartRate);
            createNotification(false, mHeartRate);
            broadcastNotification(false);
            stopMeasure();
            stopSelf();
        }

        sendDataToCloud(mHeartRate);

        Intent i = new Intent("broadcastReceiver").putExtra("heart_rate", mHeartRate);
        sendBroadcast(i);
    }

    private void sendDataToCloud(int mHeartRate) {
        FirebaseDatabase.getInstance().getReference("users")
                .child("wear")
                .child(Helper.getInstance().getUserToken(this))
                .child("data")
                .child(Utils.getCurrentWeekStart().toString())
                .child(LocalDate.now().toString())
                .child(Utils.timeToString(LocalDateTime.now()))
                .setValue(mHeartRate);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void broadcastNotification(boolean lowBlood){
        String notificationText;

        String userName = Helper.getInstance().getUserName(this);
        if(lowBlood)
            notificationText = Utils.getCurrentTime() + " - " + userName + getResources().getString(R.string.low_blood);
        else
            notificationText = Utils.getCurrentTime() + " - " + userName + getResources().getString(R.string.high_blood);

        NotificationRequest notificationRequest = new NotificationRequest(
                "/topics/" + Helper.getInstance().getUserToken(this),
                new NotificationData("Notification"),
                new Notification("HeartBeep", notificationText)
        );
        NetworkInterface networkInterface = RetrofitHelper.getInstance().create(NetworkInterface.class);
        Call<String> call = networkInterface.sendNotification(notificationRequest);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("NotificationBroadcast", "sent");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("NotificationBroadcast", "failure: " + t.getMessage());
            }
        });
    }

    private void createNotification(boolean lowBlood, int heartRate){
        int notificationId = new Random().nextInt(10000);
        String id = "heartbeep_channel";
        Intent viewIntent = new Intent(this, NotificationActivity.class);
        viewIntent.putExtra(Constants.NOT_ID, notificationId);
        viewIntent.putExtra(Constants.NOT_HEART_RATE, heartRate);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, id)
                        .setSmallIcon(R.drawable.heart_icon)
                        .setContentTitle("HeartBeep")
                        .setContentIntent(viewPendingIntent);

        if(lowBlood){
            notificationBuilder.setContentText(Utils.getCurrentTime() + " - " + getResources().getString(R.string.your_bpm_low));
        }
        else{
            notificationBuilder.setContentText(Utils.getCurrentTime() + " - " + getResources().getString(R.string.your_bpm_high));
        }

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    id,
                    "HeartBeep",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder.setChannelId(id);
        }

        notificationManager.notify(notificationId, notificationBuilder.build());
    }



    public class LocalBinder extends Binder {
        public BackgroundHeartBeatService getServerInstance() {
            return BackgroundHeartBeatService.this;
        }
    }

    private void startAlertActivity(int heartRate){
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra(Constants.NOT_HEART_RATE, heartRate);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopMeasure();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}