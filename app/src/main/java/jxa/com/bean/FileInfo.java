package jxa.com.bean;

import java.io.Serializable;

/**
 * 文件信息
 * Created by jxa on 2016/7/4.
 */
public class FileInfo implements Serializable{
    private int id;
    private String url;
    private int length;
    private int finished;
    private String filename;

    public FileInfo() {
        super();
    }

    public FileInfo(int id, String url, int length, int finished, String filename) {
        this.id = id;
        this.url = url;
        this.length = length;
        this.finished = finished;
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", length=" + length +
                ", finished=" + finished +
                ", filename='" + filename + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
