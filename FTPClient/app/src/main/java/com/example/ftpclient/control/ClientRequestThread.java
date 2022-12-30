package com.example.ftpclient.control;

import com.example.ftpclient.exception.ServerNotFound;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientRequestThread extends Thread{
    private Socket clientSocket = null;// 和本线程相关的Socket
    private BufferedWriter clientWriter;
    private BufferedReader clientReader;
    private final MyClient myClient;

    public ClientRequestThread(String address,int port,MyClient myClient) throws ServerNotFound {
        this.myClient = myClient;
        try {
            this.clientSocket = new Socket(address, port);
            clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            clientWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }catch (IOException e){
            throw new ServerNotFound("无法连接到服务器:"+address+":"+port);
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true){
            String response = null;
            try {
                response = clientReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //如果这里是null，就抛出IO异常。
                if (response == null) {
                    try {
                        throw new IOException();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    if(response.startsWith("201")){
                        myClient.setConnect(true);
                    }
                    System.out.println(response);
                }
        }
    }

    public void writerCommand(String command){
        try {
            System.out.println("Writer:" + command);
            clientWriter.write(command);
            clientWriter.write("\r\n");
            clientWriter.flush();
           // String response = clientReader.readLine();
            //System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
