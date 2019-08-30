package roberta.heartbeep.activities;

import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.wear.widget.BoxInsetLayout;

import roberta.heartbeep.R;
import roberta.heartbeep.Utilities.Constants;

public class NotificationActivity extends WearableActivity {

    BoxInsetLayout container;
    private Vibrator vibrator;
    private Handler alertHandler;
    private Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setAmbientEnabled();

        int notificationID = getIntent().getIntExtra(Constants.NOT_ID, -1);
        Log.e("Notification", "" + notificationID);


        int heartRate = getIntent().getIntExtra(Constants.NOT_HEART_RATE, -1);
        TextView alertMessage = findViewById(R.id.not_text);
        TextView heartRateValue = findViewById(R.id.heart_rate);
        if(heartRate != -1 && heartRate < 60){
            alertMessage.setText(getResources().getString(R.string.your_bpm_low));
            heartRateValue.setText(heartRate + " ");
        }else if( heartRate != -1){
            alertMessage.setText(getResources().getString(R.string.your_bpm_high));
            heartRateValue.setText(heartRate + " ");
        }

        container = findViewById(R.id.container);

        LinearLayout skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(alertHandler != null && r != null) {
                    alertHandler.removeCallbacks(r);
                }
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        if(notificationID == -1) {
            r = new Runnable() {
                @Override
                public void run() {
                    vibrate(900);

                    alertHandler.postDelayed(this, 1000);
                }
            };

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(alertHandler != null && r != null) {
                        alertHandler.removeCallbacks(r);
                    }
                }
            }, 10000);

            alertHandler = new Handler();
            alertHandler.post(r);
        }

    final Handler colorsHandler = new Handler();
        colorsHandler.post(new Runnable() {
            @Override
            public void run() {
                TransitionDrawable transition = (TransitionDrawable)container.getBackground();
                transition.startTransition(300);
                colorsHandler.postDelayed(this, 300);
            }
        });
    }

    private void vibrate(int intesity) {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long[] vibrationPattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
        //-1 - don't repeat
        final int indexInPatternToRepeat = -1;
        if (vibrator != null) {
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
        }
        try {
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100000);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_ANSWER,5000);
        }catch (Exception e){
            Log.e("VibrationERORR", " " + e.getMessage());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(vibrator != null) {
                    vibrator.cancel();
                }
            }
        }, intesity);
    }

}
