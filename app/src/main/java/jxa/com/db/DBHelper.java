package jxa.com.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jxa on 2016/7/4.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION=1;
    private static final String DB_NAME="download.db";
    private static final String DB_CREATE="create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer, url text,start integer, end integer,finished integer )";
    private static final String DB_DROP = "drop table if exists thread_info";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DB_DROP);
        sqLiteDatabase.execSQL(DB_CREATE);
    }
}
