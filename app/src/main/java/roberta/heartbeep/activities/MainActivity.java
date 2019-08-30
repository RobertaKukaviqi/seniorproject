package roberta.heartbeep.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

import roberta.heartbeep.adapters.DrawerAdapter;
import roberta.heartbeep.R;
import roberta.heartbeep.Utilities.Constants;
import roberta.heartbeep.Utilities.Helper;
import roberta.heartbeep.receivers.AlarmBroadcastReceiver;
import roberta.heartbeep.services.BackgroundHeartBeatService;

public class MainActivity extends WearableActivity implements View.OnClickListener {

    private TextView mTextView;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private ImageView gif;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int heartRateValue = intent.getIntExtra("heart_rate", 0);
            mTextView.setText( heartRateValue  + " ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, 321);
            return;
        }

        initActivity();
    }

    private void initActivity() {

        Log.e("Date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("")).toString());

        WearableNavigationDrawerView wearableNavigationDrawer = findViewById(R.id.top_navigation_drawer);
        wearableNavigationDrawer.getController().peekDrawer();
        DrawerAdapter adapter = new DrawerAdapter(this);
        wearableNavigationDrawer.setAdapter(adapter);
        wearableNavigationDrawer.addOnItemSelectedListener(adapter);
        gif = findViewById(R.id.gif);
        mTextView = findViewById(R.id.heart_rate);

        LinearLayout startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(this);

        if(Helper.getInstance().isStartButton(MainActivity.this)){
            ImageButton icon = findViewById(R.id.start_icon);
            TextView text = findViewById(R.id.start_text);
            icon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.play_icon));
            text.setText("Start");

            Glide.with(this).load(R.drawable.background).centerCrop().into(gif);
        }else{
            ImageButton icon = findViewById(R.id.start_icon);
            TextView text = findViewById(R.id.start_text);
            icon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.pause_icon));
            text.setText("Stop");
            Glide.with(this).load(R.drawable.giphy).centerCrop().into(gif);
        }

        checkConnectionRequests();
    }

    private void checkConnectionRequests() {
        FirebaseDatabase.getInstance().getReference("users")
                .child("wear")
                .child(Helper.getInstance().getUserToken(this))
                .child("requests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, String> requests = (HashMap<String,String>) dataSnapshot.getValue();
                        if(requests != null && !requests.isEmpty())
                            for(Map.Entry<String, String>entry: requests.entrySet()){
                                showConnectionResuestDialog(entry.getKey(), entry.getValue());
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void showConnectionResuestDialog(final String userId, String userName) {
        final String currentUserId = Helper.getInstance().getUserToken(MainActivity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Connection request")
                .setMessage("Allow " + userName + " to retrieve your heart rate data")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child("phone")
                                .child(userId)
                                .child("wears")
                                .child(currentUserId)
                                .setValue(true);

                        FirebaseDatabase.getInstance().getReference("users")
                                .child("wear")
                                .child(currentUserId)
                                .child("requests")
                                .child(userId)
                                .removeValue();
                    }
                })
                .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child("wear")
                                .child(currentUserId)
                                .child("requests")
                                .child(userId)
                                .removeValue();
                    }
                });

        builder.create();
        builder.show();
    }

    private void startService(){
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 2121, intent, 0);
        //first start time
        long futureInMillis = SystemClock.elapsedRealtime() + 100;
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.start_button:
                startButtonClicked();
                break;
        }
    }

    private void startButtonClicked() {
        if(Helper.getInstance().isStartButton(MainActivity.this)){
            setStartServiceButton();
        }else{
            setPauseServiceButton();
        }
    }

    private void setPauseServiceButton() {
        Helper.getInstance().stopMeasuring(MainActivity.this, true);
        Helper.getInstance().setHeartRateValue(MainActivity.this, 0);
        Helper.getInstance().startButton(MainActivity.this, true);
        if(alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        //stopService(new Intent(MainActivity.this, BackgroundHeartBeatService.class));
        Intent intent = new Intent(MainActivity.this, BackgroundHeartBeatService.class);
        intent.putExtra(Constants.STOP_MEASURE_INTENT, 1);
        startService(intent);
        ImageButton icon = findViewById(R.id.start_icon);
        icon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.play_icon));
        TextView text = findViewById(R.id.start_text);
        text.setText("Start");

        Glide.with(this).load(R.drawable.background).centerCrop().into(gif);

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 10000);
        toneGen1.startTone(ToneGenerator.TONE_PROP_PROMPT,1000);
    }

    private void setStartServiceButton() {
        Helper.getInstance().stopMeasuring(MainActivity.this, false);
        Helper.getInstance().startButton(MainActivity.this, false);
        ImageButton icon = findViewById(R.id.start_icon);
        TextView text = findViewById(R.id.start_text);
        icon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.pause_icon));
        text.setText("Stop");
        startService();

        Glide.with(this).load(R.drawable.giphy).centerCrop().into(gif);

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 10000);
        toneGen1.startTone(ToneGenerator.TONE_SUP_DIAL,1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("broadcastReceiver");
        registerReceiver(broadcastReceiver, filter);

        if (mTextView != null) {
            mTextView.setText(Helper.getInstance().getHeartRateValue(MainActivity.this) + " ");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 321){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                initActivity();
            }
        }

    }

}
