package jxa.com.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jxa.com.bean.FileInfo;
import jxa.com.service.DownloadService;
import jxa.com.utlis.NotificationUtil;

public class MainActivity extends AppCompatActivity {

    private ListView lvdownload;
    private List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
    private FileListAdapter adapter;
    private NotificationUtil notificationUtil = null;
    public Messenger mServiceMessenger = null;//Service中的Messenger


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initEvent() {

      /*  IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISH);
        filter.addAction(DownloadService.ACTION_START);
        registerReceiver(mReceiver, filter);*/
        //初始化通知工具类
        notificationUtil = new NotificationUtil(this);
        //第三步，绑定Service
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);

    }

    private void initView() {
        lvdownload = (ListView) findViewById(R.id.lvdownload);
        FileInfo fileInfo = new FileInfo(0, "http://sw.bos.baidu.com/sw-search-sp/software/0d1a67fbc10/YoudaoDict_6.3.69.5012.exe", 0, 0, "YoudaoDict_6.3.69.5012.exe");
        FileInfo fileInfo1 = new FileInfo(1, "http://img.mukewang.com/down/54bf5e0f0001687600000000.rar", 0, 0, "54bf5e0f0001687600000000.rar");
        FileInfo fileInfo2 = new FileInfo(2, "http://sw.bos.baidu.com/sw-search-sp/software/f78ef1b586cb7c9/BaiduYunGuanjia_5.4.6.2.exe", 0, 0, "BaiduYunGuanjia_5.4.6.2.exe");
        FileInfo fileInfo3 = new FileInfo(3, "http://img.mukewang.com/down/57086882000192e800000000.zip", 0, 0, "57086882000192e800000000.zip");
        fileInfoList.add(fileInfo);
        fileInfoList.add(fileInfo1);
        fileInfoList.add(fileInfo2);
        fileInfoList.add(fileInfo3);
        adapter = new FileListAdapter(this, fileInfoList);
        lvdownload.setAdapter(adapter);

    }

   /* BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                //更新下载进度
                int finish = intent.getIntExtra("finish", 0);
                int id = intent.getIntExtra("id", 0);
                adapter.updateProgress(id, finish);
                notificationUtil.updateNofication(id, finish);
            } else if (DownloadService.ACTION_FINISH.equals(intent.getAction())) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("finish");
                adapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this, fileInfoList.get(fileInfo.getId()).getFilename() + "下载完毕", Toast.LENGTH_SHORT).show();
                notificationUtil.cancelNotification(fileInfo.getId());
            } else if (DownloadService.ACTION_START.equals(intent.getAction())) {
                //显示通知
                notificationUtil.showNotification((FileInfo) intent.getSerializableExtra("fileInfo"));
            }
        }
    };*/

  /*  @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

    }*/

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //获得Service中的Messenger
            mServiceMessenger = new Messenger(iBinder);
            //设置适配器中的Messenger
            adapter.setMessenger(mServiceMessenger);
            //第四步，创建Activity中的Messenger
            Messenger mActivityMessenger = new Messenger(mHandler);
            //第五步，创建一个Message
            Message message = new Message();
            message.what = DownloadService.MSG_BIND;
            message.replyTo = mActivityMessenger;
            //使用Service的Messenger发送Activity中的Messenger
            try {
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadService.MSG_START:
                    //显示通知
                    notificationUtil.showNotification((FileInfo) msg.obj);
                    break;
                case DownloadService.MSG_FINISH:
                    FileInfo fileInfo = (FileInfo)msg.obj;
                    adapter.updateProgress(fileInfo.getId(), 0);
                    Toast.makeText(MainActivity.this, fileInfoList.get(fileInfo.getId()).getFilename() + "下载完毕", Toast.LENGTH_SHORT).show();
                    notificationUtil.cancelNotification(fileInfo.getId());
                    break;
                case DownloadService.MSG_UPDATE:
                    //更新下载进度
                    int finish = msg.arg1;
                    int id = msg.arg2;
                    adapter.updateProgress(id, finish);
                    notificationUtil.updateNofication(id, finish);
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
