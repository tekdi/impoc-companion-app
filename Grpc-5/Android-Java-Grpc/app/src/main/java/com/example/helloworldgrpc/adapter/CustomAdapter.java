package com.example.helloworldgrpc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.helloworldgrpc.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    List<String> names;
    List<Integer> status;
    LayoutInflater inflter;

    public CustomAdapter(Context context, List<String> nameList, List<Integer> nameStatus) {
        this.context = context;
        this.names = nameList;
        this.status = nameStatus;
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.row_upload_file, null);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText(names.get(i));
        ImageView icon = (ImageView) view.findViewById(R.id.imageView);
        if (status.get(i) == 0) {
            icon.setImageDrawable(context.getDrawable(R.drawable.ic_unchecked));
        } else {
            icon.setImageDrawable(context.getDrawable(R.drawable.ic_checked));
        }
        return view;
    }
}