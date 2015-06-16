package com.thesaan.android.business.austria.keywest;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class AgeControlService extends Service {

    private final IBinder mBinder = new MyBinder();
    protected Activity myActivity;
    protected Database db;


    public AgeControlService() {
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        //access the database
        db = new Database(this);

        Thread ageControl = new Thread(new Runnable() {
            @Override
            public void run() {

                db.checkPersonsAgeInDatabase(myActivity);
            }
        });

        ageControl.start();


        //Does not depend on the intent
        //because this service runs anyway
        return Service.START_STICKY;
    }

    class MyBinder extends Binder {
        AgeControlService getService() {
            return AgeControlService.this;
        }
    }


}
