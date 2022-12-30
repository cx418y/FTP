package server;

import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import Response.Code;
import command.Command;

public class ServerHandleThread extends Thread {

    private Handler mHandler;

    //连接控制
    private Socket clientSocket;// 和客户端的控制连接
    private  BufferedReader serverReader;//在控制连接上读取的字符流
    private  BufferedWriter serverWriter;//在控制连接上写入的字符流

    //数据控制
    private Socket dataSocket;
    private Socket secondDataSocket;  // 第二个数据连接

    //主动模式
    private String clientAddress;//用于在主动模式中记录客户端的ip地址
    private int clientPort;   //主动模式中客户端的port
    private String fastClientAddress;  //第二个数据连接
    private int fastClientPort;

    //被动模式
    private ServerSocket passiveServerSocket;//被动模式下用来监听客户端连接请求的socket
    private ServerSocket fastPassiveServerSocket;

    public enum ConnectMode {
        PASSIVE, ACTIVE
    }

    public enum TransferType{
        ASCII, Binary
    }

    public enum TransferMode{
        Stream, Block, Compressed
    }

    public enum StructureType{
        File, Record ,Page
    }
    //连接模式和传输模式
    private ConnectMode connectMode;
    private TransferType transferType;
    private TransferMode transferMode;
    private StructureType structureType;

    //登录相关
    private String username;
    private String password;
    private boolean isLogin;  //当前登录状态
    private HashMap <String,String> usernameAndPassword;  // 指定用户名和密码

    public ServerHandleThread(Socket clientSocket,Handler mHandler) throws IOException {
        super();
        this.mHandler = mHandler;
        this.clientSocket = clientSocket;
        serverReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        serverWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        password = "";
        username = "";
        isLogin = false;

        // 初始化合法用户名密码
        usernameAndPassword = new HashMap<String,String>();
        usernameAndPassword.put("test","test");
        usernameAndPassword.put("anonymous","");

        // 初始化连接模式、传输模式
        this.connectMode = ConnectMode.ACTIVE;
        this.transferMode = TransferMode.Stream;
        this.transferType = TransferType.Binary;
        this.structureType = StructureType.File;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
        while(true){
            try {
                String commandLine = serverReader.readLine();
                //System.out.println(commandLine);

                //如果没有指令，就一直循环:
                while (commandLine == null) {
                    commandLine = serverReader.readLine();
                }

                System.out.println(commandLine);
                Command command = new Command(commandLine, this);
                String response = command.execute();
                serverWriter.write(response);
                serverWriter.write("\r\n");
                serverWriter.flush();

            }catch (IOException e){

            }
        }
    }

    public void quit(){
        try{
            serverWriter.write(Code.QUIT_SUCCESS+": 成功退出");
            serverWriter.write("\r\n");
            serverWriter.flush();
            if(clientSocket!= null){
                clientSocket.close();
            }
            if(serverReader != null){
                serverReader.close();
            }
            if(serverWriter != null){
                serverWriter.close();
            }
            this.isLogin = false;
            this.username = "";
            this.password = "";
        }catch (IOException e){
            System.out.println("关闭失败");
        }
    }

    public boolean dateConnect(){
        if(connectMode == ConnectMode.ACTIVE){
            dataSocket = null;
            //主动模式
            try {
                System.out.println("主动模式数据连接");
                this.dataSocket = new Socket(clientAddress,clientPort);
                System.out.println("主动模式数据连接成功");
                return true;
            }catch (IOException e){
                System.out.println("主动模式数据连接失败");
            }
            return false;

        }else{
            //被动模式
            dataSocket = null;
            try {
                System.out.println("被动模式数据连接");
                dataSocket = passiveServerSocket.accept();
                System.out.println("被动模式数据连接成功");
                return true;
            }catch (IOException e){
                System.out.println("被动模式数据连接失败");
            }
        }

        return false;
    }

    public boolean secondDataConnect(){
        if(connectMode == ConnectMode.ACTIVE){
            secondDataSocket = null;
            //主动模式
            try {
                System.out.println("主动模式数据连接");
                this.secondDataSocket = new Socket(fastClientAddress,fastClientPort);
                System.out.println("主动模式数据连接成功");
                return true;
            }catch (IOException e){
                System.out.println("主动模式数据连接失败");
            }
            return false;

        }else{
            //被动模式
            secondDataSocket = null;
            try {
                System.out.println("被动模式数据连接");
                secondDataSocket = fastPassiveServerSocket.accept();
                System.out.println("被动模式数据连接成功");
                return true;
            }catch (IOException e){
                System.out.println("被动模式数据连接失败");
            }
        }

        return false;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public HashMap<String, String> getUsernameAndPassword() {
        return usernameAndPassword;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }

    public ServerSocket getPassiveServerSocket() {
        return passiveServerSocket;
    }

    public ConnectMode getConnectMode() {
        return connectMode;
    }

    public void setPassiveServerSocket(ServerSocket passiveServerSocket) {
        this.passiveServerSocket = passiveServerSocket;
    }

    public void setConnectMode(ConnectMode connectMode) {
        this.connectMode = connectMode;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
    }

    public void setStructureType(StructureType structureType) {
        this.structureType = structureType;
    }

    public StructureType getStructureType() {
        return structureType;
    }

    public BufferedWriter getServerWriter() {
        return serverWriter;
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public Socket getDataSocket() {
        return dataSocket;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public ServerSocket getFastPassiveServerSocket() {
        return fastPassiveServerSocket;
    }

    public void setFastClientAddress(String fastClientAddress) {
        this.fastClientAddress = fastClientAddress;
    }

    public void setFastClientPort(int fastClientPort) {
        this.fastClientPort = fastClientPort;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setFastPassiveServerSocket(ServerSocket fastPassiveServerSocket) {
        this.fastPassiveServerSocket = fastPassiveServerSocket;
    }

    public Socket getSecondDataSocket() {
        return secondDataSocket;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}
