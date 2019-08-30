package roberta.heartbeep.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
public class Helper {

    public static Helper getInstance(){
        return new Helper();
    }

    private SharedPreferences getSharedPres(Context context){
        return context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public void setHeartRateValue(Context context, int value){
        getSharedPres(context).edit().putInt(Constants.HEART_RATE_VALUE, value).apply();
    }

    public int getHeartRateValue(Context context){
        return getSharedPres(context).getInt(Constants.HEART_RATE_VALUE, 0);
    }

    public void startButton(Context context, boolean start){
        getSharedPres(context).edit().putBoolean(Constants.START_BUTTON, start).apply();
    }

    public boolean isStartButton(Context context){
        return getSharedPres(context).getBoolean(Constants.START_BUTTON, false);
    }

    public void stopMeasuring(Context context, boolean stop){
        getSharedPres(context).edit().putBoolean(Constants.STOP_MEASURE, stop).apply();
    }

    public boolean shouldStopMeasuring(Context context){
        return getSharedPres(context).getBoolean(Constants.STOP_MEASURE, false);
    }

    public void saveUserToken(Context context, String token){
        getSharedPres(context).edit().putString(Constants.USER_TOKEN, token).apply();
    }

    public String getUserToken(Context context){
        return getSharedPres(context).getString(Constants.USER_TOKEN, "");
    }

    public void saveUserName(Context context, String token){
        getSharedPres(context).edit().putString(Constants.USER_NAME, token).apply();
    }

    public String getUserName(Context context){
        return getSharedPres(context).getString(Constants.USER_NAME, "");
    }

}
