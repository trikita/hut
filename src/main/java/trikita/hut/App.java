package trikita.hut;

import android.app.Application;

public class App extends Application {
    private static ActionsProvider sActionsInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sActionsInstance = new ActionsProvider(getApplicationContext());
    }

    public static ActionsProvider actions() {
        return sActionsInstance;
    }
}
