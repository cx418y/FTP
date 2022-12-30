package server;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.example.myapplication.ServerApplication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ListeningThread extends Thread{
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Handler mHandler;

    public ListeningThread(ServerSocket serverSocket, Handler mHandler){
        this.serverSocket = serverSocket;
        this.mHandler = mHandler;
    }
    @Override
    public void run() {
        // super.run();
        Looper.prepare();
        int time = 1;
        try {
            while (true) {
                // 循环监听等待客户端的连接
                System.out.println("等待连接：");
                clientSocket = serverSocket.accept();

                Thread serverHandleThread = new Thread(new ServerHandleThread(clientSocket,this.mHandler));

                serverHandleThread.start();

                InetAddress inetAddress = clientSocket.getInetAddress();

                Message msg = Message.obtain(); // 实例化消息对象
                msg.what = 1; // 消息标识
                msg.obj = "连接到客户端："+inetAddress.getHostAddress(); // 消息内容存放
                mHandler.sendMessage(msg);

                System.out.println("当前客户端的IP地址是：" + inetAddress.getHostAddress());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket(){

        if(serverSocket != null){
            try {
                serverSocket.close();
                this.interrupt();
                System.out.println("服务端关闭");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
