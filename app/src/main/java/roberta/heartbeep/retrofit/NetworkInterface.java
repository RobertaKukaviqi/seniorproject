package roberta.heartbeep.retrofit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import roberta.heartbeep.Utilities.Constants;
import roberta.heartbeep.models.NotificationRequest;

public interface NetworkInterface {

    @Headers({"Content-Type: application/json", "Authorization:" + Constants.FCM_SERVER_KEY})
    @POST("fcm/send")
    Call<String> sendNotification(
            @Body NotificationRequest data
    );


}
