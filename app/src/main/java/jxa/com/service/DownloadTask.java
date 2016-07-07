package jxa.com.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jxa.com.bean.FileInfo;
import jxa.com.bean.ThreadInfo;
import jxa.com.db.ThreadDAO;
import jxa.com.db.ThreadDAOImpl;

/**
 * Created by jxa on 2016/7/4.
 */
public class DownloadTask {
    private ThreadDAO threadDAO;
    private Context context;
    private FileInfo fileInfo;
    private int finished = 0;
    public boolean isPause = false;
    private int ThreadCount = 1;
    private List<downLoadThread> threadList;


    public DownloadTask(Context context, FileInfo fileInfo, int ThreadCount) {
        this.context = context;
        this.fileInfo = fileInfo;
        this.ThreadCount = ThreadCount;
        threadDAO = new ThreadDAOImpl(context);
    }

    public void download() {

        //读取数据库的线程信息
        List<ThreadInfo> infoList = threadDAO.getThreads(fileInfo.getUrl());
        if (infoList.size() == 0) {
            //获取每个线程下载的长度
            int length = fileInfo.getLength() / ThreadCount;
            for (int i = 0; i < ThreadCount; i++) {
                ThreadInfo threadInfo = new ThreadInfo(i, fileInfo.getUrl(), length * i, (i + 1) * length - 1, 0);
                if (i == ThreadCount - 1) {
                    threadInfo.setEnd(fileInfo.getLength());
                }
                //添加信息到集合中取
                infoList.add(threadInfo);
                //向数据库插入线程信息
                threadDAO.insertThread(threadInfo);
            }
        }
        threadList = new ArrayList<downLoadThread>();
        //启动多个线程进行下载
        for (ThreadInfo info : infoList) {
            downLoadThread thread = new downLoadThread(info);
            thread.start();
            threadList.add(thread);
        }
    }

    //判断线程是否全部执行完毕
    private synchronized void checkAllThreadFinished() {
        boolean allFinish = true;
        for (downLoadThread thread : threadList) {
            if (!thread.isFinished) {
                allFinish = false;
                break;
            }
        }
        if (allFinish) {
            //删除线程信息
            threadDAO.deleteThread(fileInfo.getUrl());
            Intent intent = new Intent(DownloadService.ACTION_FINISH);
            intent.putExtra("finish", fileInfo);
            context.sendBroadcast(intent);
    }
    }

    //下载线程
    class downLoadThread extends Thread {

        private InputStream inputStream;
        private RandomAccessFile raf;
        private HttpURLConnection conn;
        private ThreadInfo threadInfo;
        public Boolean isFinished = false;//线程是否执行完毕

        public downLoadThread(ThreadInfo threadInfo) {
            this.threadInfo = threadInfo;

        }

        @Override
        public void run() {


            try {
                URL url = new URL(threadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                //设置下载位置
                int start = threadInfo.getStart() + threadInfo.getFinished();
                //范围
                conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, fileInfo.getFilename());
                raf = new RandomAccessFile(file, "rwd");
                //设置下载的开始位置
                raf.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                finished += threadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() == 206) {

                    //读取数据
                    inputStream = conn.getInputStream();

                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = inputStream.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //把下载进度发送广播到activity
                        //整个文件下载的进度
                        finished += len;
                        //每个线程完成的进度
                        threadInfo.setFinished(threadInfo.getFinished() + len);
                        if (System.currentTimeMillis() - time > 1000) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finish", finished * 100 / fileInfo.getLength());
                            intent.putExtra("id", fileInfo.getId());
                            context.sendBroadcast(intent);
                            Log.e("开始下载", inputStream + "");
                        }
                        //在下载暂停时，保存下载进度
                        if (isPause) {
                            threadDAO.updateThread(threadInfo.getUrl(), threadInfo.getId(), threadInfo.getFinished());
                            return;
                        }
                    }
                    //线程执行完毕
                    isFinished = true;

                    checkAllThreadFinished();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                conn.disconnect();
                try {
                    raf.close();
                    inputStream.close();
                    Log.e("raf关闭了",raf+"");
                    Log.e("inputStream关闭了",inputStream+"");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }
    }
}
