package com.example.tcp_server2.dbManagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ValuesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION=1;
    static final String DATABASE_NAME="valuesdatabase.db";


    private static ValuesDbHelper mInstance=null;

    public static ValuesDbHelper getInstance(Context context){
        if(mInstance==null){
            mInstance= new ValuesDbHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    public ValuesDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public int GetDatabase_Version() {
        return DATABASE_VERSION;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        //Create the table values
        sqLiteDatabase.execSQL(ValuesDbContract.ValuesEntry.SQL_CREATE_TABLE_VALUES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(ValuesDbContract.ValuesEntry.SQL_DROP_TABLE_VALUES);
        onCreate(sqLiteDatabase);
    }

}
