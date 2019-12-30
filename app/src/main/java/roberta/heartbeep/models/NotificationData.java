package roberta.heartbeep.models;

import com.google.gson.annotations.SerializedName;

public class NotificationData {
    @SerializedName("extra_information")
    private String extraInformation;

    public NotificationData(){

    }

    public NotificationData(String extraInformation) {
        this.extraInformation = extraInformation;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }
}
