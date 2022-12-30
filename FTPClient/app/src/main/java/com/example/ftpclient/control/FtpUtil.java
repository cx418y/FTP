package com.example.ftpclient.control;

import com.example.ftpclient.data.FTPFile;
import com.example.ftpclient.exception.DownloadException;
import com.example.ftpclient.exception.ModeFailure;
import com.example.ftpclient.exception.ServerNotFound;
import com.example.ftpclient.exception.SocketError;
import com.example.ftpclient.exception.TypeFailure;

import java.util.ArrayList;
import java.util.List;

public class FtpUtil {
    private static MyClient myClient = new MyClient();
    private static String address;
    private static int port;
    private static String username;
    private static String password;
    private static int checkConnect;

    public static MyClient getMyClient() {
        return myClient;
    }

    //初始化myClient
    public static void init(String my_address, int my_port, String my_username, String my_password){
        address = my_address;
        port = my_port;
        username = my_username;
        password = my_password;
        if (myClient == null){
            myClient = new MyClient();
        }
    }

    /**
     * 连接服务端
     *
     * @return 1 连接成功;0 正在连接中；-1 连接服务端失败；-2 登录失败
     */
    public static int connect(){
        checkConnect = -1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (myClient.isConnect()){
                    checkConnect = 0;
                }else {
                    try {
                        myClient.connect(address,port);
                        boolean login = myClient.login(username,password);
                        if (login){//登陆成功
                            checkConnect = 1;
                            //相关设置
                            myClient.setPassive();
                            myClient.setType("B");
                            myClient.setTransferMode("S");
                            myClient.setStructure("F");
                            //TODO
                        }else {//登陆失败
                            checkConnect = -2;
                        }
                    } catch (ServerNotFound e) {//连接失败
                        checkConnect = -1;
                    } catch (ModeFailure | TypeFailure e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return checkConnect;
    }

    //上传文件到服务端
    //TODO

    //上传文件夹到服务端
    //TODO

    //从服务端下载文件
    //TODO

    //从服务端下载文件夹
    //TODO
}
