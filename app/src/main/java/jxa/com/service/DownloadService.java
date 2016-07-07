package jxa.com.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import jxa.com.bean.FileInfo;

/**
 * Created by jxa on 2016/7/4.
 */
public class DownloadService extends Service {

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    public static final String ACTION_FINISH = "ACTION_FINISH";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/mydownloads/";
    public static final int MSG_INIT = 0;
    private Map<Integer, DownloadTask> taskMap = new LinkedHashMap<Integer, DownloadTask>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.e("获取到的下载信息", fileInfo.toString());
                    //开始下载
                    DownloadTask downloadTask = new DownloadTask(DownloadService.this, fileInfo, 3);
                    downloadTask.download();
                    taskMap.put(fileInfo.getId(), downloadTask);
                    break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得activity传递过来的参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.e("fileInfo", "开始" + fileInfo.toString());
            initDownload initDownloadThread = new initDownload(fileInfo);
            DownloadTask.executorService.execute(initDownloadThread);
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            DownloadTask downloadTask = taskMap.get(fileInfo.getId());
            if (downloadTask != null) {
                downloadTask.isPause = true;
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    class initDownload extends Thread {

        private RandomAccessFile raf;
        private HttpURLConnection conn;
        private FileInfo mFlieInfo;

        public initDownload(FileInfo mFlieInfo) {
            this.mFlieInfo = mFlieInfo;
        }

        @Override
        public void run() {
            try {
                //连接网络文件
                URL url = new URL(mFlieInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int code = conn.getResponseCode();
                int length = -1;
                if (code == 200) {
                    //获取文件长度
                    length = conn.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //在本地创建文件
                File file = new File(dir, mFlieInfo.getFilename());
                raf = new RandomAccessFile(file, "rwd");
                //设置文件长度
                raf.setLength(length);
                mFlieInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFlieInfo).sendToTarget();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                conn.disconnect();
            }
        }
    }
}

