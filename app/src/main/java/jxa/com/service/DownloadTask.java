package jxa.com.service;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    private boolean isPause = false;

    public DownloadTask(Context context, FileInfo fileInfo) {
        this.context = context;
        this.fileInfo = fileInfo;
        threadDAO = new ThreadDAOImpl(context);
    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> infoList = threadDAO.getThreads(fileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (infoList.size() == 0) {
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0,fileInfo.getUrl(),0,fileInfo.getLength(),0);
        }
        //创建子线程进行下载
        downLoad(threadInfo);
    }

    //下载线程
    public void downLoad(final ThreadInfo threadInfo) {
        new Thread() {

            private InputStream inputStream;
            private RandomAccessFile raf;
            private HttpURLConnection conn;

            @Override
            public void run() {
                //向数据库插入线程信息
                if (!threadDAO.isExists(threadInfo.getUrl(),threadInfo.getId())) {
                    threadDAO.insertThread(threadInfo);
                }

                try {
                    URL url = new URL(threadInfo.getUrl());
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(3000);
                    int code = conn.getResponseCode();
                    //设置下载位置
                    int start = threadInfo.getStart() + threadInfo.getFinished();
                    //范围
                    conn.setRequestProperty("Range","bytes="+start+"-"+threadInfo.getFinished());
                    //设置文件写入位置
                    File file = new File(DownloadService.DOWNLOAD_PATH,fileInfo.getFilename());
                    raf = new RandomAccessFile(file,"rwd");
                    //设置下载的开始位置
                    raf.seek(start);
                    Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                    finished += threadInfo.getFinished();
                    //开始下载
                    if (code == 200) {
                        //读取数据
                        inputStream = conn.getInputStream();
                        byte[] buffer = new byte[1024 * 4];
                        int len = -1;
                        long time = System.currentTimeMillis();
                        while ((len = inputStream.read(buffer)) != -1) {
                            //写入文件
                            raf.write(buffer,0,len);
                            //把下载进度发送广播到activity
                            finished += len;
                            if (System.currentTimeMillis() - time > 500) {
                                time = System.currentTimeMillis();
                                intent.putExtra("finish", finished * 100 / fileInfo.getLength());
                                context.sendBroadcast(intent);
                            }
                            //在下载暂停时，保存下载进度
                            if (isPause) {
                                threadDAO.updateThread(threadInfo.getUrl(),threadInfo.getId(),finished);
                                return;
                            }
                        }
                        //删除线程信息
                        threadDAO.deleteThread(threadInfo.getUrl(),threadInfo.getId());
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    conn.disconnect();
                    try {
                        raf.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                super.run();
            }
        }.start();
    }
}
