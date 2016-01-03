package com.allegedvino.goalnag.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.allegedvino.goalnag.HomeActivity;
import com.allegedvino.goalnag.R;
import com.allegedvino.goalnag.data.Goal;
import com.allegedvino.goalnag.data.Nag;

import org.apache.pig.impl.util.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dancvogel on 10/31/15.
 *
 * Alarm receiver
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Nag nag = (Nag)intent.getSerializableExtra(Nag.EXTRA_NAG);
        execute(nag, context);
    }

    private void execute(Nag nag, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.goal_file_key), Context.MODE_PRIVATE);
        String key = context.getString(R.string.goals_key);

        try {
            // Read in the goals list.
            String defaultValue = ObjectSerializer.serialize(new ArrayList<Goal>());
            ArrayList<Goal> goalsList = (ArrayList<Goal>) ObjectSerializer.deserialize(prefs.getString(key, defaultValue));

            for (int i = 0; i < goalsList.size(); i++) {
                Goal g = goalsList.get(i);

                if(nag == null) {
                    goalsList.get(i).setCompleted(false);
                } else if(g.getId().equals(nag.getGoalId()) && !g.getCompleted()) {
                    sendNag(nag, g , context);
                    break;
                }
            }

            // Write out any changes.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(key, ObjectSerializer.serialize(goalsList));
            editor.commit();

            context.sendBroadcast(new Intent(HomeActivity.BROADCAST_REFRESH));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNag(Nag nag, Goal goal, Context context) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), 0);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Stop being lazy!")
                        .setContentText("'" + goal.getText() + "' hasn't been done today!!")
                        .setContentIntent(pi)
                        .setVibrate(new long[]{0, 1000, 1000, 1000, 1000})
                        .setLights(context.getResources().getColor(R.color.colorPrimary), 3000, 3000)
                        .setSound(alarmSound);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // TODO: proper id.
        notificationManager.notify((int)nag.getTime().getTimeInMillis(), mBuilder.build());
    }
}

