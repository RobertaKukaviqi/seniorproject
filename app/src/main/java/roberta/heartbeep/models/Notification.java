package roberta.heartbeep.models;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("title")
    private String title;
    @SerializedName("text")
    private String text;

    public Notification(){

    }

    public Notification(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
