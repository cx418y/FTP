package command;

import android.os.Build;
import android.os.Environment;

import android.os.Message;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.myapplication.GetIP;
import com.example.myapplication.ServerApplication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Response.Code;
import server.MD5Util;
import server.ServerHandleThread;

public class Execute {
    private ServerHandleThread serverHandleThread;
    String clientAddress;

    public Execute(ServerHandleThread serverHandleThread){
        this.serverHandleThread = serverHandleThread;
        InetAddress inetAddress = serverHandleThread.getClientSocket().getInetAddress();
        this.clientAddress = inetAddress.getHostAddress();
    }
    public String USERCommand(String username){
        String response;
        HashMap map = serverHandleThread.getUsernameAndPassword();
        if(!map.containsKey(username)){
            response = Code.USER_NOT_EXIST_CODE + ":该用户不存在";
        }
        else if(map.get(username).equals("")) {
            serverHandleThread.setUsername(username);
            serverHandleThread.setPassword("");
            serverHandleThread.setLogin(true);
            response = Code.LOGIN_SUCCESS_CODE + ": 匿名登录成功";
            sendMessage("客户端匿名登陆成功");
        }else {
            serverHandleThread.setUsername(username);
            serverHandleThread.setPassword((String)map.get(username));
            response = Code.USER_EXIST_CODE + ": 请输入用户密码";
        }
        return response;
    }

    public String PASSCommand(String password){
        String response;
        if(password.equals(serverHandleThread.getPassword())){
            serverHandleThread.setLogin(true);
            response = Code.LOGIN_SUCCESS_CODE + ": 登录成功";

            sendMessage("客户端登陆成功，用户名：test");

        }else{
            response = Code.PASSWORD_ERROR_CODE+": 密码错误";
        }
        return response;
    }

    public String NOOPCommand(){
        return Code.NOOP_TEST_SUCCESS+": 连接中";
    }

    public String QUITCommand(){
        serverHandleThread.quit();

        sendMessage("客户端断开连接");
        return "";
    }

    public String TYPECommand(String type) {
        String response;
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        if (type.equals("A")) {
            serverHandleThread.setTransferType(ServerHandleThread.TransferType.ASCII);
            sendMessage("收到客户端请求，更改模式为：ASCII");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：ASCII";
            return response;
        }
        if (type.equals("B")) {
            serverHandleThread.setTransferType(ServerHandleThread.TransferType.Binary);
            sendMessage("收到客户端请求，更改模式为：Binary");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：Binary";
            return response;
        }

        response = Code.PARAMETER_ERROR + ": 参数错误";
        return response;
    }


    public String PASVCommand() {
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        //获取本机的地址
        try {
            int p1;
            int p2;
            String localAddress  = GetIP.getIPAddress(ServerApplication.getContext());
//            String localAddress = "";
//            try {
//                for (Enumeration<NetworkInterface> enNetI = NetworkInterface
//                        .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
//                    NetworkInterface netI = enNetI.nextElement();
//                    for (Enumeration<InetAddress> enumIpAddr = netI
//                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                        InetAddress inetAddress = enumIpAddr.nextElement();
//                        if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
//                            localAddress = inetAddress.getHostAddress();
//                        }
//                    }
//                }
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
            System.out.println("localAddress:"+localAddress);
            ServerSocket pasiveServerSocket = null;
            while (pasiveServerSocket == null) {
                p1 = (int)(Math.random() * (255 - 4) + 4);
                p2 = (int)(Math.random() * 255);
                try {

                    int port = p1 * 256 + p2;
                    pasiveServerSocket = new ServerSocket(port);
                } catch (IOException e) {
                    continue;
                }

                if(serverHandleThread.getPassiveServerSocket() != null) {
                    serverHandleThread.getPassiveServerSocket().close();
                }

                serverHandleThread.setPassiveServerSocket(pasiveServerSocket);
                serverHandleThread.setConnectMode(ServerHandleThread.ConnectMode.PASSIVE);

                response = Code.COMMAND_EXECUTE_SUCCESS+": " + localAddress.replaceAll("\\.",",") +"," + p1+","+p2;
                System.out.println("PASV Response:"+response);

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e){
        }
        sendMessage("收到客户端请求，更改模式为：被动模式");
        return response;
    }

    public String PASVFast(){
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        //获取本机的地址
        try {
            int p1;
            int p2;
            String localAddress  = GetIP.getIPAddress(ServerApplication.getContext());
            System.out.println("localAddress:"+localAddress);
            ServerSocket fastPasiveServerSocket = null;
            while (fastPasiveServerSocket == null) {
                p1 = (int)(Math.random() * (255 - 4) + 4);
                p2 = (int)(Math.random() * 255);
                try {

                    int port = p1 * 256 + p2;
                    fastPasiveServerSocket = new ServerSocket(port);
                } catch (IOException e) {
                    continue;
                }

                if(serverHandleThread.getFastPassiveServerSocket() != null) {
                    serverHandleThread.getFastPassiveServerSocket().close();
                }

                serverHandleThread.setFastPassiveServerSocket(fastPasiveServerSocket);
                serverHandleThread.setConnectMode(ServerHandleThread.ConnectMode.PASSIVE);
                response = Code.COMMAND_EXECUTE_SUCCESS+": " + localAddress.replaceAll("\\.",",") +"," + p1+","+p2;
                System.out.println("PASV Response:"+response);

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e){
        }
        return response;
    }

    public String PORTCommand(String args){
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        //客户端地址和端口错误
        String[] temp = args.split(",");
        if(temp.length != 6){
            response = Code.PARAMETER_ERROR + ": 参数错误";
            return response;
        }
        String pattern = "[0-9]+";
        for(String ss : temp){
            if(!ss.matches(pattern)){
                response = Code.PARAMETER_ERROR + ": 参数错误";
                return response;
            }
        }
        String address = temp[0]+"."+temp[1]+"."+temp[2]+"."+temp[3];
        int port = Integer.parseInt(temp[4]) * 256 + Integer.parseInt(temp[5]);

        serverHandleThread.setClientAddress(address);
        serverHandleThread.setClientPort(port);
        serverHandleThread.setConnectMode(ServerHandleThread.ConnectMode.ACTIVE);

        sendMessage("收到客户端请求，更改模式为：主动模式");
        response = Code.COMMAND_EXECUTE_SUCCESS + ": 成功接收port";
        return response;
    }

    public String PORTFast(String args){
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        //客户端地址和端口错误
        String[] temp = args.split(",");
        if(temp.length != 6){
            response = Code.PARAMETER_ERROR + ": 参数错误";
            return response;
        }
        String pattern = "[0-9]+";
        for(String ss : temp){
            if(!ss.matches(pattern)){
                response = Code.PARAMETER_ERROR + ": 参数错误";
                return response;
            }
        }
        String address = temp[0]+"."+temp[1]+"."+temp[2]+"."+temp[3];
        int port = Integer.parseInt(temp[4]) * 256 + Integer.parseInt(temp[5]);

        serverHandleThread.setFastClientAddress(address);
        serverHandleThread.setFastClientPort(port);
        serverHandleThread.setConnectMode(ServerHandleThread.ConnectMode.ACTIVE);
        sendMessage("收到客户端1的请求，当前为主动模式");
        response = Code.COMMAND_EXECUTE_SUCCESS + ": 成功接收port";
        return response;
    }

    public String MODECommand(String mode){
        String response;
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        if (mode.equals("S")) {
            serverHandleThread.setTransferMode(ServerHandleThread.TransferMode.Stream);
            sendMessage("收到客户端请求，更改模式为：Stream");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：Stream";
            return response;
        }
        if (mode.equals("B")) {
            serverHandleThread.setTransferMode(ServerHandleThread.TransferMode.Block);
            sendMessage("收到客户端请求，更改模式为：Block");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：Block";
            return response;
        }
        if (mode.equals("C")) {
            serverHandleThread.setTransferMode(ServerHandleThread.TransferMode.Compressed);
            sendMessage("收到客户端请求，更改模式为：Compressed");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：Compressed";
            return response;
        }
        response = Code.PARAMETER_ERROR + ": 参数错误";
        return response;
    }

    public String STRUCommand(String type){
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        if (type.equals("F")) {
            serverHandleThread.setStructureType(ServerHandleThread.StructureType.File);
            sendMessage("收到客户端请求，更改模式为：File");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：File";
            return response;
        }
        if (type.equals("R")) {
            serverHandleThread.setStructureType(ServerHandleThread.StructureType.Record);
            sendMessage("收到客户端请求，更改模式为：Record");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：Record";
            return response;
        }
        if (type.equals("P")) {
            serverHandleThread.setStructureType(ServerHandleThread.StructureType.Page);
            sendMessage("收到客户端请求，更改模式为：Page");
            response = Code.COMMAND_EXECUTE_SUCCESS + ": 更改模式成功，当前模式为：Page";
            return response;
        }

        response = Code.PARAMETER_ERROR + ": 参数错误";
        return response;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String RETRCommand(String path) throws IOException {
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(rootPath + path);
        System.out.println("path:"+rootPath + path);

        //文件不存在或是目录
        if (!file.exists() || file.isDirectory()) {
            response = Code.FILE_NOT_FOUND + ": 请求的文件不存在";
            System.out.println(response);
            return response;
        }

        //返回一个文件状态ok准备打开数据连接诶
        System.out.println(Code.FILE_IS_OK + ": 文件状态没问题，准备打开数据连接。");
        serverHandleThread.getServerWriter().write(Code.FILE_IS_OK + ": 文件状态没问题，准备打开数据连接。");
        serverHandleThread.getServerWriter().write("\r\n");
        serverHandleThread.getServerWriter().flush();
        sendMessage("准备向客户端传输:"+file.getAbsolutePath());

        long start = System.nanoTime();

        //打开数据连接
        boolean dataConnect = serverHandleThread.dateConnect();
        try {
            if (!dataConnect) {
                serverHandleThread.getServerWriter().write(Code.CANNOT_OPEN_DATA_CONNECT + ": 数据连接建立失败");
            } else {
                serverHandleThread.getServerWriter().write(Code.DATA_CONNECT_IS_OPEN + ": 成功建立数据连接");
            }
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {

        }

        // 使用数据连接传输数据：
        //ASCII模式
        if (serverHandleThread.getTransferType() == ServerHandleThread.TransferType.ASCII) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(serverHandleThread.getDataSocket().getOutputStream()));
                String s;

                long start1 = System.nanoTime();
                long end1 = start1;
                while ((s = br.readLine()) != null) {
                    bw.write(s);
                    bw.write("\r\n");
                    bw.flush();
                    if((end1=System.nanoTime())-start1 > 1000000000){
                        sendMessage("正在传输中，已用时：" + (end1-start)/1000000000);
                        System.out.println("正在传输中，已用时：" + (end1-start)/1000000000);
                        start1 = end1;
                    }
                }
                br.close();
                bw.close();
                System.out.println(file.getName()+"md5:"+ MD5Util.md5HashCode(file.getAbsolutePath())+"  size"+file.length());
                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();


            } catch (IOException e) {

            } finally {
                serverHandleThread.getDataSocket().close();
            }
        }

        //Binary模式
        else {
            try {
                OutputStream os = serverHandleThread.getDataSocket().getOutputStream();

                InputStream input = new FileInputStream(file);
                byte[] buf = new byte[1024 * 1024];
                int b;
                long start1 = System.nanoTime();
                long end1 = start1;
                while ( (b = input.read(buf)) != -1) {
                    if(b == 1024 * 1024) {
                        os.write(buf);
                        os.flush();
                    }else{
                        byte[] temp = new byte[b];
                        for(int i = 0; i < b; i++){
                            temp[i] = buf[i];
                        }
                        os.write(temp);
                        os.flush();
                    }
                    if((end1=System.nanoTime())-start1 > 1000000000){
                        sendMessage("正在传输中，已用时：" + (end1-start)/1000000000);
                        System.out.println("正在传输中，已用时：" + (end1-start)/1000000000);
                        start1 = end1;
                    }
                    buf = new byte[1024 * 1024];
                }
                os.close();
                input.close();
                System.out.println("md5:"+ MD5Util.md5HashCode(file.getAbsolutePath()) + "  size"+file.length());

                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();
            } catch (IOException e) {

            } finally {
                serverHandleThread.getDataSocket().close();
                //serverHandleThread.getDataSocket() = null;
            }
        }

        long end = System.nanoTime();
        sendMessage(file.getAbsolutePath() + "传输完成,用时"+(end-start)/1000000000);
        response = Code.FILE_OPERATION_SUCCESS + ": 请求的文件操作正常进行，已完成。";
        Toast.makeText(ServerApplication.getContext(), "传输完成", Toast.LENGTH_SHORT).show();
        return response;
    }

    public String STORCommand(String fileName) throws IOException {
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/client";
        System.out.println("rootpath:" + rootPath);
        File file = new File(rootPath + fileName);
        if (!file.getParentFile().exists()) {
            System.out.println("文件父目录不存在");
            file.mkdirs();
        }

        if (file.exists() ) {
            System.out.println("文件存在");
            file.delete();
            file.createNewFile();
        }

        if (!file.exists()) {
            System.out.println("文件不存在");
            file.createNewFile();
        }
        serverHandleThread.getServerWriter().write(Code.FILE_IS_OK + ": 文件状态没问题，准备打开数据连接。");
        serverHandleThread.getServerWriter().write("\r\n");
        serverHandleThread.getServerWriter().flush();

        sendMessage("准备接收客户端上传文件：:"+file.getAbsolutePath());

        long start = System.nanoTime();
        //打开数据连接
        boolean dataConnect = serverHandleThread.dateConnect();
        try {
            if (!dataConnect) {
                serverHandleThread.getServerWriter().write(Code.CANNOT_OPEN_DATA_CONNECT + ": 数据连接建立失败");
            } else {
                serverHandleThread.getServerWriter().write(Code.DATA_CONNECT_IS_OPEN + ": 成功建立数据连接");
            }
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (serverHandleThread.getTransferType() == ServerHandleThread.TransferType.ASCII) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(serverHandleThread.getDataSocket().getInputStream()));
                //BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
                String s;
                int row = 0;

                long start1 = System.nanoTime();
                long end1 = start1;
                while ((s = br.readLine()) != null) {
                    System.out.println("读取到的行的内容"+s);
                    row ++;
                    if(row != 1) {
                        bw.write("\r\n");
                    }
                    bw.write(s);
                    bw.flush();

                    if((end1=System.nanoTime())-start1 > 1000000000){
                        sendMessage("正在传输中，已用时：" + (end1-start)/1000000000);
                        System.out.println("正在传输中，已用时：" + (end1-start)/1000000000);
                        start1 = end1;
                    }
                }
                System.out.println("获得文件行数："+row);

                br.close();
                bw.close();
                System.out.println("md5:"+ MD5Util.md5HashCode(file.getAbsolutePath())+"  size"+file.length());

                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();


            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                serverHandleThread.getDataSocket().close();
                //serverHandleThread.getDataSocket() = null;
            }

        } else {
            try {
                OutputStream os = new FileOutputStream(file);
                InputStream input = serverHandleThread.getDataSocket().getInputStream();

                byte[] buf = new byte[1024 * 1024];
                int b;
                long start1 = System.nanoTime();
                long end1 = start1;
                while ((b = input.read(buf)) != -1) {
                    if(b == 1024 * 1024) {
                        os.write(buf);
                        os.flush();
                        buf = new byte[1024 * 1024];
                    }else{
                        byte[] temp = new byte[b];
                        for(int i = 0; i < b; i++){
                            temp[i] = buf[i];
                        }
                        os.write(temp);
                        os.flush();
                    }

                    if((end1=System.nanoTime())-start1 > 1000000000){
                        sendMessage("正在传输中，已用时：" + (end1-start)/1000000000);
                        System.out.println("正在传输中，已用时：" + (end1-start)/1000000000);
                        start1 = end1;
                    }

                }

                os.close();
                input.close();
                System.out.println("md5:"+ MD5Util.md5HashCode(file.getAbsolutePath())+"  size"+file.length());
                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                serverHandleThread.getDataSocket().close();
                //serverHandleThread.getDataSocket() = null;
            }
        }

        long end = System.nanoTime();
        sendMessage("成功接收:"+file.getAbsolutePath()+", 用时："+(end-start)/1000000000);
        response = Code.FILE_OPERATION_SUCCESS + ": 请求的文件操作正常进行，已完成。";
        return response;

    }

    public String LISTCommand(String path){
        List<String> fileList = new ArrayList<>();
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(rootPath + path);

        //文件不存在或是目录C
        if (!file.exists() || file.isFile()) {
            response = Code.FILE_NOT_FOUND + ": 请求的文件不存在";
            return response;
        }

        try {
            serverHandleThread.getServerWriter().write(Code.COMMAND_EXECUTE_SUCCESS+": 准备返回目录");
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        }catch (IOException e){

        }

        getFile(file,fileList);
        for(String name:fileList){
            String filePath = name.substring(Environment.getExternalStorageDirectory().getAbsolutePath().length());
            try {
                serverHandleThread.getServerWriter().write(filePath);
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();
            }catch (IOException e){

            }
        }

        //告诉客户端传输完成
        return "";
    }

    public void getFile(File file, List<String> fileList){
        File[] files = file.listFiles();
        if(files != null){
            for(File fileItem : files){
                if(fileItem.isDirectory()){
                    getFile(fileItem,fileList);
                }else{
                    fileList.add(fileItem.getPath());
                }
            }
        }
    }

    public String SHOWCommand(String path) {
        System.out.println(Environment.getExternalStorageDirectory().getAbsolutePath());
       // File file = new File(rootPath + path);
        List<String> fileList = new ArrayList<>();
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(rootPath + path);

        //文件不存在或是目录
        if (!file.exists() || file.isFile()) {
            response = Code.FILE_NOT_FOUND + ": 请求的文件不存在";
            return response;
        }

        try {
            serverHandleThread.getServerWriter().write(Code.COMMAND_EXECUTE_SUCCESS+ ": 命令执行成功，下面进行文件名传输");
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {

        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File fileItem : files) {
                if (fileItem.isDirectory()) {
                    fileList.add(fileItem.getPath() + ",D");
                } else {
                    fileList.add(fileItem.getPath() + ",F");
                }
            }
        }

        for (String name : fileList) {
            String filePath = name.substring(Environment.getExternalStorageDirectory().getAbsolutePath().length(), name.length());
            try {
                serverHandleThread.getServerWriter().write(filePath);
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();
            } catch (IOException e) {

            }
        }
        //告诉客户端传输完成
        return "";
    }

    public void sendMessage(String message){
        Message msg = Message.obtain();
        msg.what = 1;
        msg.obj = message;
        serverHandleThread.getmHandler().sendMessage(msg);
    }


    public String RETEFast(String path) throws IOException{
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(rootPath + path);
        System.out.println("path:"+rootPath + path);

        //文件不存在或是目录
        if (!file.exists() || file.isDirectory()) {
            response = Code.FILE_NOT_FOUND + ": 请求的文件不存在";
            System.out.println(response);
            return response;
        }

        //返回一个文件状态ok准备打开数据连接诶
        System.out.println(Code.FILE_IS_OK + ": 文件状态没问题，准备打开数据连接。");
        serverHandleThread.getServerWriter().write(Code.FILE_IS_OK + ": 文件状态没问题，准备打开数据连接。");
        serverHandleThread.getServerWriter().write("\r\n");
        serverHandleThread.getServerWriter().flush();
        sendMessage("准备向客户端传输:"+file.getAbsolutePath());

        long start = System.nanoTime();

        //打开数据连接

        //打开数据连接
        boolean dataConnect = serverHandleThread.dateConnect();
        try {
            if (!dataConnect) {
                serverHandleThread.getServerWriter().write(Code.CANNOT_OPEN_DATA_CONNECT + ": 数据连接建立失败");
            } else {
                serverHandleThread.getServerWriter().write(Code.DATA_CONNECT_IS_OPEN + ": 成功建立数据连接");
            }
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean SecondDataConnect = serverHandleThread.secondDataConnect();
        try {
            if (!SecondDataConnect) {
                serverHandleThread.getServerWriter().write(Code.CANNOT_OPEN_DATA_CONNECT + ": 数据连接建立失败");
            } else {
                serverHandleThread.getServerWriter().write(Code.DATA_CONNECT_IS_OPEN + ": 成功建立数据连接");
            }
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 使用数据连接传输数据：
        //ASCII模式
        if (serverHandleThread.getTransferType() == ServerHandleThread.TransferType.ASCII) {
            try {
                //将文件先写入两个文件内：
                long middleSize = file.length()/2;
                long currentSize = 0;
                File fileFirst = new File(rootPath + path+"First");
                File fileSecond = new File(rootPath + path+"Second");
                if(fileFirst.exists()){
                    fileFirst.delete();
                }
                if(fileSecond.exists()){
                    fileSecond.delete();
                }
                fileFirst.createNewFile();
                fileSecond.createNewFile();
                BufferedReader br0 = new BufferedReader(new FileReader(file));
                BufferedWriter bw01 = new BufferedWriter(new FileWriter(fileFirst));
                BufferedWriter bw02 = new BufferedWriter(new FileWriter(fileSecond));

                String s0;
                while ((s0 = br0.readLine()) != null) {
                    currentSize += s0.length();
                    if(currentSize <= middleSize){
                        bw01.write(s0);
                        bw01.write("\r\n");
                        bw01.flush();
                    }
                    else{
                        bw02.write(s0);
                        bw02.write("\r\n");
                        bw02.flush();
                    }
                }
                br0.close();
                bw01.close();
                bw02.close();

                //新线程传输文件第一部分
                Thread t = new Thread(()->{
                    try {
                        File fileFirst1 = new File(rootPath + path+"First");
                        BufferedReader br = new BufferedReader(new FileReader(fileFirst1));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(serverHandleThread.getDataSocket().getOutputStream()));
                        String s1;
                        while ((s1 = br.readLine()) != null) {
                            bw.write(s1);
                            bw.write("\r\n");
                            bw.flush();
                        }
                        br.close();
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
                t.start();

                //主线程传输第二部分
                BufferedReader br2 = new BufferedReader(new FileReader(fileSecond));
                BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(serverHandleThread.getSecondDataSocket().getOutputStream()));

                String s2;
                while ((s2 = br2.readLine()) != null) {
                    bw2.write(s2);
                    bw2.write("\r\n");
                    bw2.flush();
                }

                br2.close();
                bw2.close();

                t.join();
                fileFirst.delete();
                fileSecond.delete();
                System.out.println(file.getName()+"md5:"+ MD5Util.md5HashCode(file.getAbsolutePath())+"  size"+file.length());
                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                serverHandleThread.getDataSocket().close();
                serverHandleThread.getSecondDataSocket().close();
            }
        }

        //Binary模式
        else {
            try {
                long middleSize = file.length()/2;
                long currentSize = 0;
                byte[]buff = new byte[1024*1024];

                InputStream input0 = new FileInputStream(file);
                File fileFirst = new File(rootPath + path+"First");
                File fileSecond = new File(rootPath + path+"Second");
                if(fileFirst.exists()){
                    fileFirst.delete();
                }
                if(fileSecond.exists()){
                    fileSecond.delete();
                }
                fileFirst.createNewFile();
                fileSecond.createNewFile();

                OutputStream os01 = new FileOutputStream(fileFirst);
                OutputStream os02 = new FileOutputStream(fileSecond);

                if(file.length() != 0) {
                    int a = 0;
                    while ((a = input0.read(buff)) != -1) {
                        if (currentSize < middleSize) {
                            os01.write(buff);
                            os01.flush();
                        } else{
                            if (a == 1024 * 1024) {
                                os02.write(buff);
                            } else {
                                byte[] temp = new byte[a];
                                for (int i = 0; i < a; i++) {
                                    temp[i] = buff[i];
                                }
                                os02.write(temp);
                            }
                            os02.flush();
                        }
                    }
                }

                OutputStream os2 = serverHandleThread.getSecondDataSocket().getOutputStream();
                InputStream input2 = new FileInputStream(fileSecond);

                Thread t = new Thread(() ->{
                    byte[] buf = new byte[1024*1024];
                    int b;
                    try {
                        File fileFirst1 = new File(rootPath + path+"First");
                        OutputStream os = serverHandleThread.getDataSocket().getOutputStream();
                        InputStream input = new FileInputStream(fileFirst1);
                        while ( (b = input.read(buf)) != -1) {
                            if(b == 1024 * 1024) {
                                os.write(buf);
                            }else{
                                byte[] temp = new byte[b];
                                for(int i = 0; i < b; i++){
                                    temp[i] = buf[i];
                                }
                                os.write(temp);
                            }
                            os.flush();
                        }
                        os.close();
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

                byte[] buf = new byte[1024*1024];
                int b1;
                while ( (b1 = input2.read(buf)) != -1) {
                    if(b1 == 1024 * 1024) {
                        os2.write(buf);
                        os2.flush();
                    }else{
                        byte[] temp = new byte[b1];
                        for(int i = 0; i < b1; i++){
                            temp[i] = buf[i];
                        }
                        os2.write(temp);
                        os2.flush();
                    }
                }
                os2.close();
                input2.close();

                t.join();
                fileFirst.delete();
                fileSecond.delete();
                System.out.println("md5:"+ MD5Util.md5HashCode(file.getAbsolutePath()) + "  size"+file.length());
                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                serverHandleThread.getDataSocket().close();
                serverHandleThread.getSecondDataSocket().close();
                //serverHandleThread.getDataSocket() = null;
            }
        }
        long end = System.nanoTime();
        sendMessage(file.getAbsolutePath() + "传输完成,用时"+(end-start)/1000000000);
        System.out.println(file.getAbsolutePath() + "传输完成,用时"+(end-start)/1000000000);
        response = Code.FILE_OPERATION_SUCCESS + ": 请求的文件操作正常进行，已完成。";
        return response;
    }

    public String STORFast(final String fileName) throws IOException{
        String response = "";
        if (!serverHandleThread.isLogin()) {
            response = Code.USER_NOT_LOGIN + ": 未登录";
            return response;
        }

        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/client";
        System.out.println("rootpath:" + rootPath);
        File file = new File(rootPath + fileName);
        if (!file.getParentFile().exists()) {
            System.out.println("文件父目录不存在");
            file.mkdirs();
        }

        if (file.exists() ) {
            System.out.println("文件存在");
            file.delete();
            file.createNewFile();
        }

        if (!file.exists()) {
            System.out.println("文件不存在");
            file.createNewFile();
        }
        serverHandleThread.getServerWriter().write(Code.FILE_IS_OK + ": 文件状态没问题，准备打开数据连接。");
        serverHandleThread.getServerWriter().write("\r\n");
        serverHandleThread.getServerWriter().flush();

        sendMessage("准备接收客户端上传文件：:"+file.getAbsolutePath());

        long start = System.nanoTime();
        //打开数据连接
        boolean dataConnect = serverHandleThread.dateConnect();
        try {
            if (!dataConnect) {
                serverHandleThread.getServerWriter().write(Code.CANNOT_OPEN_DATA_CONNECT + ": 数据连接建立失败");
            } else {
                serverHandleThread.getServerWriter().write(Code.DATA_CONNECT_IS_OPEN + ": 成功建立数据连接");
            }
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean SecondDataConnect = serverHandleThread.secondDataConnect();
        try {
            if (!SecondDataConnect) {
                serverHandleThread.getServerWriter().write(Code.CANNOT_OPEN_DATA_CONNECT + ": 数据连接建立失败");
            } else {
                serverHandleThread.getServerWriter().write(Code.DATA_CONNECT_IS_OPEN + ": 成功建立数据连接");
            }
            serverHandleThread.getServerWriter().write("\r\n");
            serverHandleThread.getServerWriter().flush();
        } catch (IOException e) {

        }

        File fileSecond = new File(file.getAbsolutePath()+"Second");
        if(fileSecond.exists()){
            fileSecond.delete();
        }
        fileSecond.createNewFile();
        if (serverHandleThread.getTransferType() == ServerHandleThread.TransferType.ASCII) {
            try {
                //将两个连接的数据写入两个文件中：
                //新线程写第一部分内容
                Thread t = new Thread(() -> {
                    try{
                        File fileFirst = new File(rootPath+fileName+ "First");
                        if(fileFirst.exists()){
                            fileFirst.delete();
                        }
                        fileFirst.createNewFile();
                        BufferedReader br = new BufferedReader(new InputStreamReader(serverHandleThread.getDataSocket().getInputStream()));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileFirst)));
                        String s;
                        int row = 0;
                        while ((s = br.readLine()) != null) {
                            row ++;
                            if(row != 1) {
                                bw.write("\r\n");
                            }
                            bw.write(s);
                            bw.flush();
                        }
                        br.close();
                        bw.close();
                    }catch (IOException e){

                    }
                });

                t.start();

                BufferedReader br2 = new BufferedReader(new InputStreamReader(serverHandleThread.getSecondDataSocket().getInputStream()));
                BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileSecond)));

                String s;

                while ((s = br2.readLine()) != null) {
                    bw2.write("\r\n");
                    bw2.write(s);
                    bw2.flush();
                }
                t.join();

                File fileFirst = new File(fileName+"First");

                //将两个文件的内容存入一个文件
                BufferedReader br3 = new BufferedReader(new InputStreamReader(new FileInputStream(fileFirst)));
                BufferedReader br4 = new BufferedReader(new InputStreamReader(new FileInputStream(fileSecond)));
                BufferedWriter bw4 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

                while((s = br3.readLine()) != null){
                    bw4.write(s);
                    bw4.flush();
                }

                while ((s = br4.readLine())!= null){
                    bw4.write(s);
                    bw4.flush();
                }
                br2.close();
                bw2.close();
                br3.close();
                br4.close();
                bw4.close();
                fileFirst.delete();
                fileSecond.delete();

                System.out.println("md5:"+ MD5Util.md5HashCode(file.getAbsolutePath())+"  size"+file.length());
                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }finally {
                serverHandleThread.getDataSocket().close();
                serverHandleThread.getSecondDataSocket().close();
            }

        } else {
            try {
                Thread t = new Thread(()->{
                    try {
                        File fileFirst = new File(rootPath+fileName+ "First");
                        if(fileFirst.exists()){
                            fileFirst.delete();
                        }
                        fileFirst.createNewFile();
                        OutputStream os = new FileOutputStream(fileFirst);
                        InputStream input = serverHandleThread.getDataSocket().getInputStream();
                        byte[] buf = new byte[1024 * 1024];
                        int b;
                        while ((b = input.read(buf)) != -1) {
                            if(b == 1024 * 1024) {
                                os.write(buf);
                                os.flush();
                            }else{
                                byte[] temp = new byte[b];
                                for(int i = 0; i < b; i++){
                                    temp[i] = buf[i];
                                }
                                os.write(temp);
                                os.flush();
                            }
                        }
                        os.close();
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                t.start();

                OutputStream os2 = new FileOutputStream(fileSecond);
                InputStream input2 = serverHandleThread.getSecondDataSocket().getInputStream();

                byte[] buf = new byte[1024 * 1024];
                int b;
                while ((b = input2.read(buf)) != -1) {
                    if(b == 1024 * 1024) {
                        os2.write(buf);
                        os2.flush();
                    }else{
                        byte[] temp = new byte[b];
                        for(int i = 0; i < b; i++){
                            temp[i] = buf[i];
                        }
                        os2.write(temp);
                        os2.flush();
                    }
                }
                os2.close();
                input2.close();

                t.join();

                File fileFirst = new File(file.getAbsolutePath()+"First");
                InputStream input3 = new FileInputStream(fileFirst);
                InputStream input4 = new FileInputStream(fileSecond);
                OutputStream output = new FileOutputStream(file);

                while ((b = input3.read(buf)) != -1) {
                    if(b == 1024 * 1024) {
                        output.write(buf);
                        output.flush();
                        buf = new byte[1024 * 1024];
                    }else{
                        byte[] temp = new byte[b];
                        for(int i = 0; i < b; i++){
                            temp[i] = buf[i];
                        }
                        output.write(temp);
                        output.flush();
                    }
                }

                while ((b = input4.read(buf)) != -1) {
                    if(b == 1024 * 1024) {
                        output.write(buf);
                        output.flush();
                        buf = new byte[1024 * 1024];
                    }else{
                        byte[] temp = new byte[b];
                        for(int i = 0; i < b; i++){
                            temp[i] = buf[i];
                        }
                        output.write(temp);
                        output.flush();
                    }
                }

                input3.close();
                input4.close();
                output.close();

                System.out.println("md5:"+ MD5Util.md5HashCode(file.getAbsolutePath())+"  size"+file.length());
                serverHandleThread.getServerWriter().write(Code.FILE_TRANSFER_SUCCESS + ": 关闭数据连接，请求的文件操作已成功。");
                serverHandleThread.getServerWriter().write("\r\n");
                serverHandleThread.getServerWriter().flush();
            } catch (IOException | InterruptedException e) {

            } finally {
                serverHandleThread.getDataSocket().close();
                serverHandleThread.getSecondDataSocket().close();
            }
        }

        long end = System.nanoTime();
        sendMessage("成功接收:"+file.getAbsolutePath()+", 用时："+(end-start)/1000000000);
        System.out.println("成功接收:"+file.getAbsolutePath()+", 用时："+(end-start)/1000000000);
        response = Code.FILE_OPERATION_SUCCESS + ": 请求的文件操作正常进行，已完成。";
        return response;
    }

}
