package trikita.hut;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdatePluginsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        App.actions().refresh();
    }
}
