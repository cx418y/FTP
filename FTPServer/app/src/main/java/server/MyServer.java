package server;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class MyServer{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ListeningThread listeningThread;
    private Handler mHandler;

    public MyServer(int port, Handler mhandler) {
        this.mHandler = mhandler;
        try {
            serverSocket = new ServerSocket(port);

            Message msg = Message.obtain(); // 实例化消息对象
            msg.what = 1; // 消息标识
            msg.obj = "–开启服务器，监听端口: "+port; // 消息内容存放

            mHandler.sendMessage(msg);
            System.out.println("–开启服务器，监听端口: "+port);
            listeningThread = new ListeningThread(serverSocket,this.mHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        listeningThread.start();
    }

    public void close(){
        try {
            if (serverSocket != null)
                serverSocket.close();
            listeningThread.closeSocket();
        }catch (IOException e){

        }
    }


}
