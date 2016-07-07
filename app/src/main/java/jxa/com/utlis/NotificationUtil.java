package jxa.com.utlis;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

import jxa.com.app.MainActivity;
import jxa.com.app.R;
import jxa.com.bean.FileInfo;
import jxa.com.service.DownloadService;

/**
 * Created by jxa on 2016/7/7.
 */
public class NotificationUtil {

    private NotificationManager notificationManager;
    private Map<Integer,Notification> notificationMap = null;
    private Context mContext = null;

    public NotificationUtil(Context context) {
        mContext = context;
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationMap = new HashMap<Integer, Notification>();
    }

    public void showNotification(FileInfo fileInfo) {
        //判断是否存在同一通知
        if (!notificationMap.containsKey(fileInfo.getId())) {
            Notification notification = new Notification();
            //设置滚动文字
            notification.tickerText = fileInfo.getFilename() + "正在下载";
            //设置显示时间
            notification.when = System.currentTimeMillis();
            //设置图标
            notification.icon = R.mipmap.ic_launcher;
            //设置通知特性 (自动关闭)
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            //设置通知栏操作
            Intent intent = new Intent(mContext, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            notification.contentIntent = pendingIntent;
            //自定义显示的通知 ，创建RemoteView对象
            // 1、创建一个自定义的消息布局 notification.xml,然后把RemoteViews对象传到contentView字段
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification);
            //设置开始操作按钮
            Intent intentStart = new Intent(mContext, DownloadService.class);
            intentStart.setAction(DownloadService.ACTION_START);
            intentStart.putExtra("fileInfo", fileInfo);
            PendingIntent pendingIntentStart = PendingIntent.getService(mContext, 0, intentStart, 0);
            remoteViews.setOnClickPendingIntent(R.id.startnotification, pendingIntentStart);
            //设置暂停操作按钮
            Intent intentStop = new Intent(mContext, DownloadService.class);
            intentStop.setAction(DownloadService.ACTION_STOP);
            intentStop.putExtra("fileInfo", fileInfo);
            PendingIntent pendingIntentStop = PendingIntent.getService(mContext, 0, intentStop, 0);
            remoteViews.setOnClickPendingIntent(R.id.stopnotification, pendingIntentStop);
            //设置文字
           remoteViews.setTextViewText(R.id.tv_name_notification,fileInfo.getFilename());
            //设置notification的视图
            notification.contentView =remoteViews;
            //发出通知
            notificationManager.notify(fileInfo.getId(),notification);
            //把通知加到集合中
            notificationMap.put(fileInfo.getId(),notification);
        }
    }

    public void cancelNotification(int id) {
        notificationManager.cancel(id);
        notificationMap.remove(id);
    }
    //更新进度条
    public void updateNofication(int id,int progress) {
        Notification notification = notificationMap.get(id);
        if (notification != null) {
            notification.contentView.setProgressBar(R.id.pb_download_notification,100,progress,false);
            //重新发送通知
            notificationManager.notify(id,notification);
        }
    }
}
