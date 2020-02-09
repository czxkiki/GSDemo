package com.dji.GSDemo.GaodeMap;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

import androidx.multidex.MultiDex;

public class MApplication extends Application {

    private DJIDemoApplication uxApplication;
    private DJIDemoApplication fpvDemoApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        MultiDex.install(this);
        Helper.install(MApplication.this);
        if (uxApplication == null) {
            uxApplication = new DJIDemoApplication();
            uxApplication.setContext(this);
        }
        if (fpvDemoApplication == null) {
            fpvDemoApplication = new DJIDemoApplication();
            fpvDemoApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uxApplication.onCreate();
        fpvDemoApplication.onCreate();
    }

}
