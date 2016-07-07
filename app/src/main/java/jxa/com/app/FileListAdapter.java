package jxa.com.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jxa.com.bean.FileInfo;
import jxa.com.service.DownloadService;

/**
 * Created by jxa on 2016/7/5.
 */
public class FileListAdapter extends BaseAdapter {

    private List<FileInfo> mFile = new ArrayList<FileInfo>();
    private Context context;
    public FileListAdapter(Context context,List<FileInfo> mFile) {
        this.mFile = mFile;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mFile.size();
    }

    @Override
    public Object getItem(int positon) {
        return mFile.get(positon);
    }

    @Override
    public long getItemId(int positon) {
        return positon;
    }

    @Override
    public View getView(int positon, View view, ViewGroup viewGroup) {
        final FileInfo fileInfo = mFile.get(positon);
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.list_item, null);
            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.pb_download = (ProgressBar) view.findViewById(R.id.pb_download);
            holder.start = (Button) view.findViewById(R.id.start);
            holder.stop = (Button) view.findViewById(R.id.stop);
            holder.tv_name.setText(fileInfo.getFilename());
            holder.pb_download.setMax(100);
            holder.start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_START);
                    intent.putExtra("fileInfo", fileInfo);
                    context.startService(intent);
                }
            });
            holder.stop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, DownloadService.class);
                    intent.setAction(DownloadService.ACTION_STOP);
                    intent.putExtra("fileInfo", fileInfo);
                    context.startService(intent);
                }
            });
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.pb_download.setProgress(fileInfo.getFinished());
        return view;
    }

    public static class ViewHolder {
        public TextView tv_name;
        public ProgressBar pb_download;
        public Button start;
        public Button stop;
    }
    //更新进度条
    public void updateProgress(int id, int progress) {
        FileInfo fileInfo = mFile.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }
}
