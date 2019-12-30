package roberta.heartbeep.models;

import com.google.gson.annotations.SerializedName;

public class NotificationRequest {

    @SerializedName("to")
    private String to;
    @SerializedName("data")
    private NotificationData data;
    @SerializedName("notification")
    private Notification notification;

    public NotificationRequest(){

    }

    public NotificationRequest(String to, NotificationData data, Notification notification) {
        this.to = to;
        this.data = data;
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
