package com.allegedvino.goalnag;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.allegedvino.goalnag.adapters.NagAdapter;
import com.allegedvino.goalnag.data.Goal;
import com.allegedvino.goalnag.data.Nag;

import org.apache.pig.impl.util.ObjectSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class AddGoalActivity extends AppCompatActivity {
    public final static String EXTRA_GOAL = "com.allegedvino.goalnag.GOAL";
    private Goal _data;
    private LinearLayout _newNag;
    private TimePicker _addTime;
    private ListView _nagList;
    private NagAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        _newNag = (LinearLayout) findViewById(R.id.add_nag_group);
        _addTime = (TimePicker) findViewById(R.id.new_nag_time);
        _nagList = (ListView) findViewById(R.id.list_nags);

        _newNag.setVisibility(View.GONE);

        Intent intent = getIntent();
        _data = (Goal)intent.getSerializableExtra(EXTRA_GOAL);
        if(_data == null){
            _data = new Goal();
        }

        _adapter = new NagAdapter(this, _data);
        _nagList.setAdapter(_adapter);

        EditText editText = (EditText) findViewById(R.id.edit_goal);
        editText.setText(_data.getText());
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    _newNag.setVisibility(View.GONE);
                }
            }
        });
    }

    /** Called when the user clicks the Send button */
    public void saveGoal(View view) {
        Intent intent = new Intent();
        EditText editText = (EditText) findViewById(R.id.edit_goal);
        String goalText = editText.getText().toString();

        if(goalText.length() == 0) {
            Toast.makeText(this, "Goal text cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }
        _data.setText(goalText);

        intent.putExtra(EXTRA_GOAL, _data);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void onAddNagClick(View view) {
        int hour = _addTime.getCurrentHour();
        int minute = _addTime.getCurrentMinute();

        Nag nag = new Nag();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        nag.setTime(calendar);
        _data.addNag(nag);
        _adapter.notifyDataSetChanged();
        _newNag.setVisibility(View.GONE);
    }

    private int getNextNagId(){
        int id = 0;

        // load tasks from preference
        SharedPreferences prefs = getSharedPreferences(getString(R.string.goal_file_key), Context.MODE_PRIVATE);
        String key = getString(R.string.goals_key);

        try {
            String defaultValue = ObjectSerializer.serialize(new ArrayList<Goal>());
            ArrayList<Goal> list = (ArrayList<Goal>) ObjectSerializer.deserialize(prefs.getString(key, defaultValue));

            List<Integer> ids = new ArrayList<Integer>();

            for(int i = 0; i < list.size(); i++) {
                Goal goal = list.get(i);

                for (int x = 0; x < goal.getNagCount(); x++) {
                    Nag n = goal.getNagAt(x);
                   // ids.add(n.getId());
                }
            }

            Collections.sort(ids);

            for (int x = 0; x < ids.size(); x++) {
                int n = ids.get(x);
                if(id != n) {
                    break;
                }
                id++;
            }

        } catch (NullPointerException e){
            e.printStackTrace();
            /* Then there was no */
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return id;
    }

    /** Called when the user clicks the new nag button */
    public void onShowAddNag(View view) {
        // hiding the input keyboard that was open for the nag title input.
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        _newNag.setVisibility(View.VISIBLE);
        _addTime.requestFocus();
    }

    /** Called when the user clicks the cancel button */
    public void onCancelNewNagClick(View view) {
        _newNag.setVisibility(View.GONE);
    }
}
