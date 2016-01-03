package com.allegedvino.goalnag.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.allegedvino.goalnag.data.Nag;
import com.allegedvino.goalnag.receivers.AlarmReceiver;

import java.util.Calendar;

/**
 * Created by dancvogel on 12/21/15.
 */
public class AlarmService extends IntentService {

    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";
    public static final String RECREATE = "RECREATE";

    private IntentFilter matcher;

    public AlarmService() {
        super("AlarmService");
        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
        matcher.addAction(RECREATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Nag nag = (Nag)intent.getSerializableExtra(Nag.EXTRA_NAG);

        if (matcher.matchAction(action)) {
            if(RECREATE.equals(action)){
                // To set the clear completed alarm.
                execute(CREATE, null);
                // TODO: And every nag.
                // foreach
                // execute (CREATE, nag);
            } else {
                execute(action, nag);
            }
        }
    }

    private void execute(String action, Nag nag) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(this, AlarmReceiver.class);

        int id = 0;
        if (nag != null) {
            i.putExtra(Nag.EXTRA_NAG_ID, nag.getId());
            i.putExtra(Nag.EXTRA_NAG, nag);
            id = (int)nag.getTime().getTimeInMillis();
        }

        PendingIntent pi = PendingIntent.getBroadcast(this, id, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.set(Calendar.HOUR_OF_DAY, 1);

        if (nag != null) {
            time = nag.getTime();
        }

        while(time.getTimeInMillis() < System.currentTimeMillis()) {
            time.add(Calendar.DATE, 1);
        }

        if (CREATE.equals(action)) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pi);
        } else if (CANCEL.equals(action)) {
            am.cancel(pi);
        }
    }
}