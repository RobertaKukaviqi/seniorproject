package roberta.heartbeep.models;

import android.graphics.drawable.Drawable;

public class DrawerItem {

    private String title;
    private Drawable drawable;

    public DrawerItem(String title, Drawable drawable) {
        this.title = title;
        this.drawable = drawable;
    }

    public DrawerItem(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
