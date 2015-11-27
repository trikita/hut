package trikita.hut;

import android.app.Application;

public class App extends Application {
    public static ActionsProvider sActionsInstance = null;

    @Override
    public void onCreate() {
        sActionsInstance = new ActionsProvider(getApplicationContext());
    }

    public static ActionsProvider actions() {
        return sActionsInstance;
    }
}
