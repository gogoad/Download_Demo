package jxa.com.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import jxa.com.bean.ThreadInfo;

/**
 * Created by jxa on 2016/7/4.
 */
public class ThreadDAOImpl implements ThreadDAO {
    private DBHelper dbHelper;

    public ThreadDAOImpl(Context context) {
        dbHelper = DBHelper.instance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(),
                        threadInfo.getEnd(), threadInfo.getFinished()});
        database.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("delete from thread_info where url=? ",
                new Object[]{url});
        database.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("update thread_info set finished=? where url=? and thread_id=?",
                new Object[]{finished,url,thread_id});
        database.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<ThreadInfo> threadInfos = new ArrayList<ThreadInfo>();
        Cursor cursor = database.rawQuery("select * from thread_info where url=?", new String[]{url});
        while (cursor.moveToNext()) {
            ThreadInfo threadInfo = new ThreadInfo();
            threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            threadInfo.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            threadInfo.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            threadInfo.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            threadInfos.add(threadInfo);
        }
        cursor.close();
        database.close();
        return threadInfos;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from thread_info where url=? and thread_id=?", new String[]{url,thread_id+""});
        boolean exists = cursor.moveToNext();
        cursor.close();
        //database.close();
        return exists;
    }
}
