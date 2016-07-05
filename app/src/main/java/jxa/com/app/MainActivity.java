package jxa.com.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jxa.com.bean.FileInfo;
import jxa.com.service.DownloadService;

public class MainActivity extends AppCompatActivity {

    private ListView lvdownload;
    List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
    private FileListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initEvent() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver, filter);
    }

    private void initView() {
        lvdownload = (ListView) findViewById(R.id.lvdownload);
        FileInfo fileInfo = new FileInfo(0, "http://img.mukewang.com/down/574cf68700013a9f00000000.rar", 0, 0, "574cf68700013a9f00000000.rar");
        FileInfo fileInfo1 = new FileInfo(1, "http://img.mukewang.com/down/54bf5e0f0001687600000000.rar", 0, 0, "54bf5e0f0001687600000000.rar");
        FileInfo fileInfo2 = new FileInfo(2, "http://crab.baidu.com/Public/pub/CrabSDK5.0.7.zip", 0, 0, "CrabSDK5.0.7.zip");
        FileInfo fileInfo3 = new FileInfo(3, "http://img.mukewang.com/down/57086882000192e800000000.zip", 0, 0, "57086882000192e800000000.zip");
        fileInfoList.add(fileInfo);
        fileInfoList.add(fileInfo1);
        fileInfoList.add(fileInfo2);
        fileInfoList.add(fileInfo3);
        adapter = new FileListAdapter(this, fileInfoList);
        lvdownload.setAdapter(adapter);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                //更新下载进度
                int finish = intent.getIntExtra("finish", 0);
                int id = intent.getIntExtra("id", 0);
                adapter.updateProgress(id, finish);
            } else if (DownloadService.ACTION_FINISH.equals(intent.getAction())) {
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                adapter.updateProgress(fileInfo.getId(), 0);
                Toast.makeText(MainActivity.this, fileInfoList.get(fileInfo.getId()).getFilename(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

    }
}
