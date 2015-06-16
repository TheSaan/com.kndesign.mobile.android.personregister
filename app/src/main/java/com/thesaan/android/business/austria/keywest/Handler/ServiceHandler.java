package com.thesaan.android.business.austria.keywest.Handler;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;

/**
 * Created by Michael Knöfler on 04.03.2015.
 */
public class ServiceHandler {

    public void restartService(Activity activity,Service service){
        activity.stopService(new Intent(activity, service.getClass()));
        activity.startService(new Intent(activity, service.getClass()));
    }
}
