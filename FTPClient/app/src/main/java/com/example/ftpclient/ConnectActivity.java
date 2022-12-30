package com.example.ftpclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ftpclient.control.FtpUtil;
import com.example.ftpclient.control.MyClient;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;

public class ConnectActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private CheckBox box;
    private String address,password,user;
    private int port=0;
    private EditText ip1,ip2,ip3,ip4,portInput,userInput,passwordInput;
    private boolean anonymous = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        box = (CheckBox) findViewById(R.id.anonymous);

        InitMenu();

        //还原存储的数据
        SharedPreferences pref =getSharedPreferences("connectData",MODE_PRIVATE);
        address=pref.getString("address","X");
        password=pref.getString("password","X");
        user=pref.getString("user","X");
        port=pref.getInt("port",0);
        anonymous=pref.getBoolean("anonymous", false);

        ip1=(EditText)findViewById(R.id.ip_1);
        ip2=(EditText)findViewById(R.id.ip_2);
        ip3=(EditText)findViewById(R.id.ip_3);
        ip4=(EditText)findViewById(R.id.ip_4);
        portInput=(EditText)findViewById(R.id.port);
        userInput=(EditText)findViewById(R.id.user);
        passwordInput=(EditText)findViewById(R.id.password);

        if(address!=null && !address.equals("X")){
            String[] ip = address.split("\\.");
            if (ip.length == 4){
                ip1.setText(ip[0]);
                ip2.setText(ip[1]);
                ip3.setText(ip[2]);
                ip4.setText(ip[3]);
            }
        }
        if(password!=null && !password.equals("X")){
            passwordInput.setText(password);
        }
        if(user!=null && !user.equals("X")){
            userInput.setText(user);
        }
        if(port!=0){
            portInput.setText(""+port);
        }
        box.setChecked(anonymous);

        //连接
        Button connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    address = ip1.getText().toString()+"."+ip2.getText().toString()+"."+ip3.getText().toString()+"."+ip4.getText().toString();
                    port = Integer.parseInt(portInput.getText().toString().trim());
                    user = userInput.getText().toString();
                    password = passwordInput.getText().toString();
                    judgeAnonymous();
                    attemptLogin();
                }catch (NumberFormatException e){
                    port=0;
                    Toast.makeText(ConnectActivity.this,"请把信息填写完整",Toast.LENGTH_SHORT).show();
                }

                //信息保存
                SharedPreferences.Editor editor=getSharedPreferences("connectData",MODE_PRIVATE).edit();
                editor.putString("address",address);
                editor.putString("user",user);
                editor.putString("password",password);
                editor.putInt("port",port);
                editor.putBoolean("anonymous", anonymous);
                editor.apply();
            }
        });
    }

    //判断是否匿名登录
    private void judgeAnonymous(){
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                anonymous = isChecked;
            }
        });
    }

    private void InitMenu() {
        ActionBar actionBar = getSupportActionBar();
        //设置左上角图标
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }

        NavigationView navigationView=(NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //回到主页
                    case R.id.nav_LocalDirectory:
                        Intent intent=new Intent(ConnectActivity.this,MainActivity.class);
                        startActivity(intent);
                        break;
                    //断开连接
                    case R.id.nav_close:
                        disconnect();
                        item.setChecked(false);
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });
    }

    //处理菜单被选中运行后的事件处理
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    //进行校验，尝试登录
    private void attemptLogin(){
        if(ip1.getText().toString().equals("")||ip2.getText().toString().equals("")||ip3.getText().toString().equals("")||ip4.getText().toString().equals("")||port==0){
            Toast.makeText(this,"请把信息填写完整",Toast.LENGTH_SHORT).show();
        }else if (user.equals("")||password.equals("")){
            if (anonymous){
                login(address,port,"anonymous","");
            }else {
                Toast.makeText(this,"请把信息填写完整",Toast.LENGTH_SHORT).show();
            }
        }else {
            login(address,port,user,password);
        }
    }

    //登录
    private void login(String address, int port, String user, String password) {
        FtpUtil.init(address,port,user,password);
        int isConnected = FtpUtil.connect();
        if (isConnected == 1){//连接成功
            Intent intent=new Intent();
            intent.setAction("com.example.connected");
            sendBroadcast(intent);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ConnectActivity.this, "连接成功,默认被动模式", Toast.LENGTH_SHORT).show();
                }
            });
        }else if (isConnected == 0){//已经在连接中
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ConnectActivity.this, "当前已连接到该地址", Toast.LENGTH_SHORT).show();
                }
            });
        }else if (isConnected == -1){//连接服务端失败
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ConnectActivity.this, "无法连接到服务器:"+address+":"+port, Toast.LENGTH_SHORT).show();
                }
            });
        }else if (isConnected == -2){//登录失败
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ConnectActivity.this, "登陆失败,账号或密码错误！", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    //断开连接
    private void disconnect(){
        if (FtpUtil.getMyClient()!=null && FtpUtil.getMyClient().isConnect()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (FtpUtil.getMyClient() != null) {
                            if (FtpUtil.getMyClient().isConnect()) {
                                FtpUtil.getMyClient().disconnect();
                                threadToast("连接已断开！");
                            } else {
                                threadToast("还未连接，请先连接！");
                            }
                        }
                    } catch (IOException e) {
                        threadToast("断开连接失败！");
                    }
                }
            }).start();
        }else {
            threadToast("还未连接，请先连接！");
        }
    }

    private void threadToast(String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConnectActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
