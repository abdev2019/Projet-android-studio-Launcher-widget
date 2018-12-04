package com.abdev.fastballlauncher;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class MyDbAdapter
{
    myDbHelper myhelper;
    ArrayList<String> apps = new ArrayList();

    public MyDbAdapter(Context context)
    {
        myhelper = new myDbHelper(context);
        getData();
    }

    public long insertData(String name)
    {
        if(contains(name)) return 0;
        SQLiteDatabase dbb = myhelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(myDbHelper.APP, name);
        long id = dbb.insert(myDbHelper.TABLE_NAME, null , contentValues);
        apps.add(name);
        return id;
    }


    public boolean contains(String app){
        return apps.contains(app);
    }

    public String getData()
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] columns = {myDbHelper.UID, myDbHelper.APP};
        Cursor cursor =db.query(myDbHelper.TABLE_NAME,columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            int cid =cursor.getInt(cursor.getColumnIndex(myDbHelper.UID));
            String name =cursor.getString(cursor.getColumnIndex(myDbHelper.APP));
            apps.add(name);
            buffer.append(cid+ "   " + name + " \n");
        }
        return buffer.toString();
    }

    public  int delete(String uname)
    {
        SQLiteDatabase db = myhelper.getWritableDatabase();
        String[] whereArgs ={uname};

        int count =db.delete(myDbHelper.TABLE_NAME , myDbHelper.APP+" = ?",whereArgs);
        apps.remove(uname);
        return  count;
    }

    static class myDbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "myDatabase";    // Database Name
        private static final String TABLE_NAME = "myFavourite";   // Table Name
        private static final int DATABASE_Version = 1;    // Database Version
        private static final String UID="_id";     // Column I (Primary Key)
        private static final String APP = "App";    //Column II
        private static final String CREATE_TABLE = "CREATE TABLE "+TABLE_NAME+
                " ("+UID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+APP+" VARCHAR(255) );";
        private static final String DROP_TABLE ="DROP TABLE IF EXISTS "+TABLE_NAME;


        public myDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL(CREATE_TABLE);
            } catch (Exception e) { }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL(DROP_TABLE);
                onCreate(db);
            }catch (Exception e) { }
        }
    }
}