package jxa.com.db;

import java.util.List;

import jxa.com.bean.ThreadInfo;

/**
 * Created by jxa on 2016/7/4.
 */
public interface ThreadDAO {
    //插入线程信息
    public void insertThread(ThreadInfo threadInfo);
    //删除线程
    public void deleteThread(String url);
    //更新线程下载进度
    public void updateThread(String url, int thread_id,int finished);
    //查询文件的线程信息
    public List<ThreadInfo> getThreads(String url);
    //线程信息是否已经存在
    public boolean isExists(String url,int thread_id);
}
