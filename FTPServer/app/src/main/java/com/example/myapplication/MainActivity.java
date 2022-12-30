package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;


import server.MyServer;

public class MainActivity extends AppCompatActivity {
    public ServerSocket server = null;
    public MyServer myServer;
    private static Toast toast = null;

    class mHandler extends Handler {
        // 通过复写handlerMessage() 从而确定更新UI的操作
        @Override
        public void handleMessage(Message msg) {
         // 执行UI操作
            switch(msg.what){
                case 1:
                    if(toast == null) {
                        toast = Toast.makeText(ServerApplication.getContext(), msg.obj.toString(), Toast.LENGTH_SHORT);
                    }else{
                        toast.cancel();
                        toast = Toast.makeText(ServerApplication.getContext(), msg.obj.toString(), Toast.LENGTH_SHORT);
                    }
                    toast.show();
            }
        }
    }
    // 步骤2：在主线程中创建Handler实例
    private Handler mhandler = new mHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Switch ftpSwitch = findViewById(R.id.ftpSwitch);
        final EditText text = (EditText) findViewById(R.id.port);
        EditText ip = (EditText) findViewById(R.id.server_ip_content);

        String localAddress  = "";
//        try {
//            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
//                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
//                NetworkInterface netI = enNetI.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = netI
//                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
//                        localAddress = inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
        localAddress = GetIP.getIPAddress(this);
        ip.setText(localAddress);

        ftpSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(toast != null){
                     toast.cancel();
                }
                if(buttonView.isChecked()) {
                    String port = text.getText().toString();
                    String pattern = "[0-9]+";
                    if(port.matches(pattern) || port.equals("")) {
                        port = port.equals("") ?"3333":port;
                        myServer = new MyServer(Integer.parseInt(port),mhandler);
                        //选中时 do some thing
                        System.out.println("服务器开启，端口："+port);
                        toast = Toast.makeText(getApplicationContext(), "等待来自客户端的连接请求", Toast.LENGTH_SHORT);
                        toast.show();
                        //Toast.makeText(getApplicationContext(), "等待来自客户端的连接请求", Toast.LENGTH_SHORT).show();

                        myServer.start();
                    }else{
                        toast = Toast.makeText(getApplicationContext(), "请输入正确的端口号", Toast.LENGTH_SHORT);
                        toast.show();
                        //Toast.makeText(getApplicationContext(), "请输入正确的端口号", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //非选中时 do some thing
                    toast = Toast.makeText(getApplicationContext(),"连接关闭",Toast.LENGTH_SHORT);
                    toast.show();
                    myServer.close();
                }
            }
        });
    }




    //菜单栏显示
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //菜单栏点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.fileScan) {
            Intent intent=new Intent(MainActivity.this,FileListActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
