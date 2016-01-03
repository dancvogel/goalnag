package com.allegedvino.goalnag.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.allegedvino.goalnag.R;
import com.allegedvino.goalnag.data.Goal;
import com.allegedvino.goalnag.data.Nag;

import java.text.SimpleDateFormat;

/**
 * Created by dancvogel on 11/7/15.
 *
 * Listview adapter for displaying a Goal's list of nags.
 */
public class NagAdapter extends BaseAdapter {
    private final Context _context;
    private final Goal _value;

    static class ViewHolder {
        protected TextView text;
        protected ImageButton button;
    }

    public NagAdapter(Context context, Goal value) {
        this._context = context;
        this._value = value;
    }

    @Override
    public int getCount() {
        return _value.getNagCount();
    }

    @Override
    public Nag getItem(int position) {
        return _value.getNagAt(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if(rowView == null) {
            LayoutInflater inflater = (LayoutInflater) _context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.nag_row, parent, false);

            // configure view holder
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.label);
            viewHolder.button = (ImageButton) rowView.findViewById(R.id.btn_remove_nag);
            viewHolder.button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    _value.removeNagAt(position);

                    notifyDataSetChanged();
                }
            });
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Nag nag = _value.getNagAt(position);

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        holder.text.setText(format.format(nag.getTime().getTime()));

        return rowView;
    }
}