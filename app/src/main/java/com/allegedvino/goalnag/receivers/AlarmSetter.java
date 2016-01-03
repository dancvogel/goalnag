package com.allegedvino.goalnag.receivers;

import com.allegedvino.goalnag.services.AlarmService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AlarmService.class);
        service.setAction(AlarmService.RECREATE);
        context.startService(service);
    }
}