package roberta.heartbeep.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

import com.google.firebase.auth.FirebaseAuth;

import roberta.heartbeep.R;
import roberta.heartbeep.Utilities.Helper;
import roberta.heartbeep.activities.LoginActivity;
import roberta.heartbeep.models.DrawerItem;

public class DrawerAdapter extends WearableNavigationDrawerView.WearableNavigationDrawerAdapter implements WearableNavigationDrawerView.OnItemSelectedListener {

    Context context;
    DrawerItem[] data;
    public DrawerAdapter(Context context){
        this.context = context;
         data = new DrawerItem[]{new DrawerItem("Logout", ContextCompat.getDrawable(context, R.drawable.ic_log_out))};
    }

    @Override
    public CharSequence getItemText(int pos) {
        return data[pos].getTitle();
    }

    @Override
    public Drawable getItemDrawable(int pos) {
        return data[pos].getDrawable();
    }

    @Override
    public int getCount() {
        return data.length;
    }


    @Override
    public void onItemSelected(int pos) {
        switch (pos){
            case 0:
                FirebaseAuth.getInstance().signOut();
                Helper.getInstance().saveUserToken(context, "");
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                ((Activity)context).finish();
                break;
        }
    }
}
