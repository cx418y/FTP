package com.example.ftpclient.control;

import android.os.Environment;

import com.example.ftpclient.data.FTPFile;
import com.example.ftpclient.exception.DownloadException;
import com.example.ftpclient.exception.ModeFailure;
import com.example.ftpclient.exception.ServerNotFound;
import com.example.ftpclient.exception.SocketError;
import com.example.ftpclient.exception.TypeFailure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyClient{
    private Socket controlSocket = null;// 控制连接
    private BufferedWriter clientWriter;
    private BufferedReader clientReader;

    private boolean isConnect;//判断是否已经连接到服务端并登录
    private volatile boolean passive=true;//记录主动模式还是被动模式，默认被动模式
    private volatile boolean ascii = false;//设置type为ascii还是binary，默认binary
    public enum Mode{
        Stream,Block,Compressed
    }
    private volatile Mode mode = Mode.Stream;//设置传输模式,默认stream
    public enum Structure{
        File,Record,Page
    }
    private volatile Structure structure = Structure.File;//设置文件传输格式，默认文件结构

    private volatile Socket dataSocket;//数据连接
    private volatile String serverAddress;//服务器ip地址
    private volatile int serverPort;//服务器数据连接的端口
    private volatile ServerSocket serverSocket;//主动模式需要

    private volatile String downloadDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/serverDownload";//设置文件下载目录
    private volatile String uploadDirectory = "/clientUpload";

    public MyClient(){
        isConnect = false;
    }

    /**
     * 建立控制连接
     *
     * @param address ip地址
     * @param port 端口号
     *
     * @exception ServerNotFound 无法连接到服务器
     */
    public void connect(String address, int port) throws ServerNotFound {
        try {
            this.controlSocket = new Socket(address, port);
            clientReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            clientWriter = new BufferedWriter(new OutputStreamWriter(controlSocket.getOutputStream()));
        }catch (IOException e){
            throw new ServerNotFound("无法连接到服务器:"+address+":"+port);
        }
    }

    /**
     * 建立数据连接
     *
     * @return 是否建立成功
     * @throws SocketError socket错误
     */
    public boolean dataConnect() throws SocketError {
        try {
            if (dataSocket != null){
                dataSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        try {
            if (passive){
                dataSocket = new Socket(serverAddress, serverPort);
            }else {
                dataSocket = serverSocket.accept();
            }
            return true;
        }catch (IOException e){
            ignore();
            throw new SocketError(e.getMessage());
        }
    }

    /**
     * 登录-USER、PASS
     *
     * @param username 用户名
     * @param password 密码
     * @return 是否登录成功
     */
    public synchronized boolean login(String username, String password){
        try {
            writeCommand("USER "+username);
            String response = clientReader.readLine();
            if (response.startsWith("230")){//登录不需要密码
                setConnect(true);
                return true;
            }else if (response.startsWith("201")){//用户名存在，登录需要密码
                writeCommand("PASS "+password);
                String response_pass = clientReader.readLine();
                if (response_pass.startsWith("230")){//密码正确
                    setConnect(true);
                    return true;
                }
            }
        } catch (IOException e) {
            ignore();
        }
        return false;
    }

    //在控制连接上写入命令
    public void writeCommand(String command) throws IOException {
        if (clientWriter!=null){
            clientWriter.write(command);
            clientWriter.write("\r\n");
            clientWriter.flush();
        }else {
            throw new IOException("请先连接");
        }
    }

    //若发生异常则ignore之前写入的命令
    private void ignore(){
        try {
            if (clientReader!=null){
                while (clientReader.ready()){
                    clientReader.read();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 断开连接-QUIT
     *
     * @throws IOException 断开连接失败
     */
    public void disconnect() throws IOException{
        writeCommand("QUIT");
        String response = clientReader.readLine();
        if (response.startsWith("221")){
            setConnect(false);
            controlSocket.close();
            clientReader.close();
            clientWriter.close();
        }else {
            throw new IOException();
        }
    }

    /**
     * 被动模式-PASV
     *
     * @throws ModeFailure 被动模式设置失败
     */
    public synchronized void setPassive() throws ModeFailure {
        try {
            writeCommand("PASV");
            String response = clientReader.readLine();
            if (response.startsWith("227")){
                String a = response.substring(response.indexOf("(")+1,response.indexOf(")"));
                String[] b = a.split(",");
                if (b.length == 6){
                    serverAddress = b[0]+"."+b[1]+"."+b[2]+"."+b[3];
                    serverPort = Integer.parseInt(b[4])*256 + Integer.parseInt(b[5]);
                    passive = true;
                }else {
                    ignore();
                    throw new ModeFailure("response error");
                }
            }else {
                ignore();
                throw new ModeFailure(response);
            }
        }catch (IOException e){
            ignore();
            throw new ModeFailure("set passive mode failure");
        }
    }

    /**
     *主动模式-PORT
     *
     * @throws ModeFailure 主动模式设置失败
     */
    public synchronized void setActive() throws ModeFailure{
        Random random = new Random();
        boolean open = false;
        int p1,p2,port;

        while (!open){
            //随机端口
            p1 = random.nextInt(256);
            p2 = random.nextInt(256);
            port = p1*256+p2;

            //打开serverSocket监听服务端
            ServerSocket serverSocket;
            try {
                serverSocket = new ServerSocket(port);
                open = true;

                try {
                    String localAddress = InetAddress.getLocalHost().getHostAddress();
                    String[] a = localAddress.split("\\.");
                    writeCommand("PORT "+a[0]+","+a[1]+","+a[2]+","+a[3]+","+p1+","+p2);
                    String response = clientReader.readLine();
                    if (response!=null && response.startsWith("200")){
                        if (this.serverSocket != null){
                            this.serverSocket.close();
                        }
                        passive = false;
                        this.serverSocket = serverSocket;
                    }else {
                        ignore();
                        throw new ModeFailure(response);
                    }
                }catch (IOException e){
                    ignore();
                    throw new ModeFailure("set active mode failure");
                }
            }catch (IOException e){
                open = false;
            }
        }
    }

    /**
     *设置ASCII/Binary模式-TYPE
     *
     * @param type A/B
     * @exception TypeFailure 设置type失败
     */
    public synchronized void setType(String type) throws TypeFailure {
        if (type.equals("A") || type.equals("B")){
            try{
                writeCommand("TYPE "+type);
                String response = clientReader.readLine();
                if (response!=null && response.startsWith("200")){
                    ascii= type.equals("A");
                }else {
                    ignore();
                    throw new TypeFailure(response);
                }
            }catch (IOException e){
                ignore();
                throw new TypeFailure("set type failure");
            }
        }
    }

    /**
     * 设置传输模式-MODE
     *
     * @param mode S/B/C——目前只实现流模式
     * @throws ModeFailure 设置模式失败
     */
    public synchronized void setTransferMode(String mode) throws ModeFailure {
        if (mode.equals("S") || mode.equals("B") || mode.equals("C")){
            try{
                writeCommand("MODE "+mode);
                String response = clientReader.readLine();
                if (response!=null && response.startsWith("200")){
                    switch (mode){
                        case "S":this.mode = Mode.Stream;break;
                        case "B":this.mode = Mode.Block;break;
                        case "C":this.mode = Mode.Compressed;break;
                    }
                }else {
                    ignore();
                    throw new ModeFailure(response);
                }
            }catch (IOException e){
                ignore();
                throw new ModeFailure("set transfer type failure");
            }
        }
    }

    /**
     * 设置文件传输结构-STRU
     *
     * @param structure F/R/P——目前只实现文件结构
     * @throws ModeFailure 设置文件传输结构失败
     */
    public synchronized void setStructure(String structure) throws ModeFailure {
        if (structure.equals("F") || structure.equals("R") || structure.equals("P")){
            try{
                writeCommand("STRU "+structure);
                String response = clientReader.readLine();
                if (response!=null && response.startsWith("200")){
                    switch (structure){
                        case "F":this.structure = Structure.File;break;
                        case "R":this.structure = Structure.Record;break;
                        case "P":this.structure = Structure.Page;break;
                    }
                }else {
                    ignore();
                    throw new ModeFailure(response);
                }
            }catch (IOException e){
                ignore();
                throw new ModeFailure("set structure failure");
            }
        }
    }

    /**
     * 列出服务端pathname下文件(directory+file)-SHOW
     *
     * @param pathname 服务端文件路径
     * @return pathname下所有文件信息
     * @throws SocketError socket错误
     */
    public synchronized List<FTPFile> list(String pathname) throws SocketError,DownloadException {
        List<FTPFile> fileList = new ArrayList<>();
        try {
            writeCommand("SHOW "+pathname);
            String response = clientReader.readLine();
            if (response!=null && response.startsWith("200")){
                //循环读取控制连接上传来的文件信息
                while (true) {
                    String fileInfo;
                    try {
                        fileInfo = clientReader.readLine();
                        if (fileInfo == null || fileInfo.length() == 0) {
                            break;
                        }
                        String[] a = fileInfo.split(",");
                        if (a.length == 2){
                            FTPFile ftpFile;
                            if (a[1].equals("F")){
                                ftpFile = new FTPFile(a[0],false);
                            }else {
                                ftpFile = new FTPFile(a[0],true);
                            }
                            fileList.add(ftpFile);
                        }else {
                            ignore();
                            throw new DownloadException(response);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                ignore();
                throw new SocketError(response);
            }
        }catch (IOException e){
            ignore();
            throw new SocketError(e.getMessage());
        }
        return fileList;
    }

    /**
     * 列出服务端pathname下所有文件-LIST
     *
     * @param pathname 服务端文件路径
     * @return pathname下所有文件信息
     * @throws SocketError socket错误
     */
    public synchronized List<String> listFiles(String pathname) throws SocketError {
        List<String> fileList = new ArrayList<>();
        try {
            writeCommand("LIST "+pathname);
            String response = clientReader.readLine();
            if (response!=null && response.startsWith("200")){
                //循环读取控制连接上传来的文件信息
                while (true) {
                    String fileInfo;
                    try {
                        fileInfo = clientReader.readLine();
                        if (fileInfo == null || fileInfo.length() == 0) {
                            break;
                        }
                        fileList.add(fileInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                ignore();
                throw new SocketError(response);
            }
        }catch (IOException e){
            ignore();
            throw new SocketError(e.getMessage());
        }
        return fileList;
    }

    //返回是否已登录
    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    //设置下载目录
    public void setDownloadDirectory(String downloadDirectory) throws DownloadException{
        File file = new File(downloadDirectory);
        if (!file.exists()){
            boolean f = file.mkdir();
            if (!f){
                throw new DownloadException("创建目录失败，请重新选择目录");
            }
        }
        if (!file.isDirectory()){
            throw new DownloadException("当前路径不是一个目录");
        }
        this.downloadDirectory = downloadDirectory;
    }

    public String getDownloadDirectory(){
        return downloadDirectory;
    }

    //设置上传目录
    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public boolean isPassive() {
        return passive;
    }

    public boolean isAscii() {
        return ascii;
    }

    public Mode getMode() {
        return mode;
    }

    public Structure getStructure() {
        return structure;
    }
}
