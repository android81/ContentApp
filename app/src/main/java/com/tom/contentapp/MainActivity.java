package com.tom.contentapp;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.widget.TextView;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int permission = ActivityCompat.checkSelfPermission(this,
                READ_CONTACTS);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //未取得權限，向使用者要求允許權限
            ActivityCompat.requestPermissions(this,
                    new String[]{READ_CONTACTS, WRITE_CONTACTS},
                    REQUEST_CONTACTS);
        } else {
            //已有權限，可進行檔案存取
            readContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得聯絡人權限，進行存取
                    readContacts();
                } else {
                    //使用者拒絕權限，顯示對話框告知
                    new AlertDialog.Builder(this)
                            .setMessage("必須允許聯絡人權限才能顯示資料")
                            .setPositiveButton("OK", null)
                            .show();
                }
                return;
        }
    }

    private void readContacts() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[] { Contacts.DISPLAY_NAME,
                        Contacts.HAS_PHONE_NUMBER},
                new int[] {android.R.id.text1, android.R.id.text2},
                1){
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                TextView phone = view.findViewById(android.R.id.text2);
                if (cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))==0){
                    phone.setText("");
                }else {
                    int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
                    Cursor pCursor = getContentResolver().query(
                            Phone.CONTENT_URI,
                            null,
                            Phone.CONTACT_ID+"=?",
                            new String[]{String.valueOf(id)},
                            null);
                    if (pCursor.moveToFirst()) {
                        String number = pCursor.getString(pCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.DATA));
                        phone.setText(number);
                    }
                }
            }
        };
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}
