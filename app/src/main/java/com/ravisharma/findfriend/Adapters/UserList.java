package com.ravisharma.findfriend.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ravisharma.findfriend.Models.UserInfo;
import com.ravisharma.findfriend.R;

import java.util.List;

public class UserList extends BaseAdapter{

    Context c;
    List<UserInfo> userList;

    public UserList(Context c, List<UserInfo> userlist) {
        this.c=c;
        this.userList=userlist;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = li.inflate(R.layout.adapter_userlist, null);

        TextView userName, userPhone;

        userName = convertView.findViewById(R.id.userName);
        userPhone= convertView.findViewById(R.id.userPhone);

        userName.setText(userList.get(position).getName());
        userPhone.setText(userList.get(position).getPhone());

        return convertView;
    }
}
