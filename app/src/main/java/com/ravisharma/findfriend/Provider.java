package com.ravisharma.findfriend;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.ravisharma.findfriend.Models.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class Provider {

    public static List<UserInfo> contacts;
    public Context c;

    public Provider(Context c) {
        this.c = c;
        contacts = new ArrayList<>();
        Cursor cu = c.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        cu.moveToFirst();
        do{
            String name = cu.getString(cu.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String number = cu.getString(cu.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            UserInfo cl = new UserInfo(name, number);

            contacts.add(cl);
        }
        while (cu.moveToNext());
    }

}
