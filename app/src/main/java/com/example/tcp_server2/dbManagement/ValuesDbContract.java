package com.example.tcp_server2.dbManagement;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ValuesDbContract {


    public static final String CONTENT_AUTHORITY = "com.example.tcp_server2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_USER = "Values";


    private ValuesDbContract(){}

    public static class ValuesEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_USER).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE+"/"+CONTENT_AUTHORITY+"/"+PATH_USER;

        //Name of the table
        public static final String TABLE_NAME="TempValues";

        //Columns of the Values table
        public static final String COLUMN_Value="Value";
        public static final String COLUMN_Time="Time";


        public static final String SQL_CREATE_TABLE_VALUES="CREATE TABLE "+ ValuesEntry.TABLE_NAME+ " ("+
                ValuesEntry._ID+" INTEGER PRIMARY KEY, "+
                ValuesEntry.COLUMN_Value+" TEXT , "+
                ValuesEntry.COLUMN_Time+" TEXT "+
                " ); ";

        public static final String SQL_DROP_TABLE_VALUES = "DROP TABLE IF EXISTS " + ValuesEntry.TABLE_NAME;

        public static Uri buildUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }
    }
}
