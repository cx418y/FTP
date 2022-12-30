package com.example.ftpclient.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FTPFile{
    private long size;
    private final String filename;
    private final boolean isDirectory;
    private FTPFile parentFile;
    private String path;
    private List<FTPFile> fileList = new ArrayList<>();

    public FTPFile(String filename, boolean isDirectory){
        this.filename = filename;
        this.isDirectory = isDirectory;
    }

    public FTPFile(String filename){
        this.filename = filename;
        this.isDirectory = false;
    }

    public boolean isDirectory(){
        return isDirectory;
    }

    public String getFilename() {
        return filename;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setFileList(List<FTPFile> fileList) {
        this.fileList = fileList;
    }

    public List<FTPFile> listFiles() {
        return fileList;
    }

    public FTPFile getParentFile() {
        return parentFile;
    }

    public void setParentFile(FTPFile parentFile) {
        this.parentFile = parentFile;
    }

    public void setPath(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
