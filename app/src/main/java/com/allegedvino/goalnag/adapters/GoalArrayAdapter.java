package com.allegedvino.goalnag.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.allegedvino.goalnag.HomeActivity;
import com.allegedvino.goalnag.R;
import com.allegedvino.goalnag.data.Goal;

import java.util.ArrayList;

/**
 * Created by dancvogel on 10/28/15.
 *
 * Array adapter for displaying the list of Goals.
 */
public class GoalArrayAdapter extends ArrayAdapter<Goal> {
    private final Context context;
    private final ArrayList<Goal> values;

    static class ViewHolder {
        protected TextView text;
        protected CheckBox checkbox;
    }

    public GoalArrayAdapter(Context context, ArrayList<Goal> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.goal_row, parent, false);

            // configure view holder
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.label);
            viewHolder.checkbox = (CheckBox) rowView.findViewById(R.id.check);
            viewHolder.checkbox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            Goal element = (Goal) viewHolder.checkbox.getTag();
                            element.setCompleted(buttonView.isChecked());
                            context.sendBroadcast(new Intent(HomeActivity.BROADCAST_SAVE));
                        }
                    });
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Goal goal = values.get(position);
        holder.text.setText(goal.getText());
        holder.checkbox.setTag(goal);
        holder.checkbox.setChecked(goal.getCompleted());

        return rowView;
    }
}
