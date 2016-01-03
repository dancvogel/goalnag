package com.allegedvino.goalnag;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.allegedvino.goalnag.adapters.GoalArrayAdapter;
import com.allegedvino.goalnag.data.Goal;
import com.allegedvino.goalnag.data.Nag;
import com.allegedvino.goalnag.data.RequestCodes;
import com.allegedvino.goalnag.receivers.AlarmReceiver;
import com.allegedvino.goalnag.services.AlarmService;

import org.apache.pig.impl.util.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {
    public static final String BROADCAST_REFRESH = "com.allegedvino.goalnag.refresh";
    public static final String BROADCAST_SAVE = "com.allegedvino.goalnag.save";
    private ArrayList<Goal> _goalsList;
    private GoalArrayAdapter _adapter;
    private AlarmManager _alarmMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        _goalsList = new ArrayList<>();
        _adapter = new GoalArrayAdapter(this, _goalsList);

        // Assign adapter to ListView
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(_adapter);

        registerForContextMenu(listView);

        // Make sure the alarms are set.
        contactService(AlarmService.RECREATE, null);

        refreshList(getIntent());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddGoalActivity.class);
                startActivityForResult(intent, RequestCodes.ADD);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId()==R.id.list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(_goalsList.get(info.position).getText());
            String[] menuItems = getResources().getStringArray(R.array.menu_items);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        if(menuItemIndex == 1){
            removeGoal(info.position);
        } else {
            Intent intent = new Intent(HomeActivity.this, AddGoalActivity.class);
            intent.putExtra(AddGoalActivity.EXTRA_GOAL, _goalsList.get(info.position));
            startActivityForResult(intent, RequestCodes.EDIT);
        }

        return true;
    }

    /* Called when the second activity's finished */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case RequestCodes.ADD:
                if (resultCode == RESULT_OK) {
                    refreshList(data);

                    Goal goal = (Goal) data.getSerializableExtra(AddGoalActivity.EXTRA_GOAL);
                    if(goal == null) {
                        return;
                    }

                    for(int x = 0; x < goal.getNagCount(); x++) {
                        Nag n = goal.getNagAt(x);
                        contactService(AlarmService.CREATE, n);
                    }
                }
                break;
            case RequestCodes.EDIT:
                if (resultCode == RESULT_OK) {
                    Goal goal = (Goal) data.getSerializableExtra(AddGoalActivity.EXTRA_GOAL);
                    if(goal == null) {
                        return;
                    }

                    for(int i = 0; i < _goalsList.size(); i++) {
                        if(_goalsList.get(i).getId().equals(goal.getId())) {
                            Goal oldGoal = _goalsList.get(i);

                            // remove all nag alarms
                            for(int x = 0; x < oldGoal.getNagCount(); x++) {
                                Nag n = oldGoal.getNagAt(x);
                                if (goal.getNag(n.getId()) == null) {
                                    contactService(AlarmService.CANCEL, n);
                                }
                            }

                            for(int x = 0; x < goal.getNagCount(); x++) {
                                Nag n = goal.getNagAt(x);
                                contactService(AlarmService.CREATE, n);
                            }
                            _goalsList.set(i, goal);
                            break;
                        }
                    }
                    saveList();
                    _adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    private void contactService(String action, Nag nag){
        Intent service = new Intent(this, AlarmService.class);
        service.putExtra(Nag.EXTRA_NAG, nag);
        service.setAction(action);
        startService(service);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BROADCAST_REFRESH:
                    refreshList(intent);
                    break;
                case BROADCAST_SAVE:
                    saveList();
                    break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        saveList();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_REFRESH);
        filter.addAction(BROADCAST_SAVE);
        registerReceiver(broadcastReceiver, filter);
    }

    /** Called when the user clicks the delete button */
    public void removeGoal(int index) {
        _goalsList.remove(index);

        saveList();
        _adapter.notifyDataSetChanged();
    }

    private void refreshList(Intent intent) {
        // load tasks from preference
        SharedPreferences prefs = getSharedPreferences(getString(R.string.goal_file_key), Context.MODE_PRIVATE);
        String key = getString(R.string.goals_key);

        _goalsList.clear();
        try {
            String defaultValue = ObjectSerializer.serialize(new ArrayList<Goal>());
            ArrayList<Goal> list = (ArrayList<Goal>) ObjectSerializer.deserialize(prefs.getString(key, defaultValue));
            _goalsList.addAll(list);

            if(intent != null) {
                Goal newGoal = (Goal) intent.getSerializableExtra(AddGoalActivity.EXTRA_GOAL);

                if (newGoal != null) {
                    _goalsList.add(newGoal);
                }
            }

            _adapter.notifyDataSetChanged();

        } catch (NullPointerException e){
            e.printStackTrace();
            /* Then there was no */
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void saveList() {
        //save the list to preference
        SharedPreferences prefs = getSharedPreferences(getString(R.string.goal_file_key), Context.MODE_PRIVATE);
        String key = getString(R.string.goals_key);

        SharedPreferences.Editor editor = prefs.edit();
        try {
            editor.putString(key, ObjectSerializer.serialize(_goalsList));
        } catch (IOException e) {
            e.printStackTrace();
        }
        editor.commit();
    }
}
