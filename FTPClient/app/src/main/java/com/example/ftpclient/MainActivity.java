package com.example.ftpclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ftpclient.control.FtpUtil;
import com.example.ftpclient.control.MyClient;
import com.example.ftpclient.data.FTPFile;
import com.example.ftpclient.exception.DownloadException;
import com.example.ftpclient.exception.ModeFailure;
import com.example.ftpclient.exception.SocketError;
import com.example.ftpclient.exception.TypeFailure;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DrawerLayout mDrawerLayout;

    private MyClient myClient = FtpUtil.getMyClient();

    private List<File> fileList;
    private final List<FTPFile> ftpList=new ArrayList<>();

    private FileAdapter adapter;
    private FtpFileAdapter FTPAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPermissions();
    }

    public void Init(){
        boolean getData = InitData();
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        InitUI(getData);
        InitMenu();
        InitBroadCast();
    }

    private void InitUI(boolean getData) {
        ActionBar actionBar = getSupportActionBar();
        //设置左上角图标
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("正在复制...");

        if (getData){
            //如果获取到目录就显示本机文件列表
            recyclerView=(RecyclerView)findViewById(R.id.file_list);
            LinearLayoutManager layoutManager=new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            adapter=new FileAdapter(fileList);
            FTPAdapter=new FtpFileAdapter(ftpList,this);
            recyclerView.setAdapter(adapter);
        }
    }

    //创建Menu菜单的项目
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    //处理菜单被选中运行后的事件处理
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.setting:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("下载路径");
                //    通过LayoutInflater来加载一个xml的布局文件作为一个View对象
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.setting_download_directory, null);
                //    设置我们自己定义的布局文件作为弹出框的Content
                builder1.setView(view);

                EditText downloadDirectory = (EditText)view.findViewById(R.id.downloadDirectory);
                downloadDirectory.setText(myClient.getDownloadDirectory());

                builder1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String a = downloadDirectory.getText().toString().trim();
                        try {
                            myClient.setDownloadDirectory(a);
                            Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } catch (DownloadException e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
                break;
            case R.id.passive:
                final String[] items1 = {"主动模式", "被动模式"};
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("设置主动模式/被动模式");

                int checkedItem = 0;
                if (myClient.isPassive()){
                    checkedItem = 1;
                }

                builder2.setSingleChoiceItems(items1, checkedItem,new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int i){
                        if (i == 0){
                            if (myClient.isPassive()){
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            myClient.setActive();
                                            Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                        } catch (ModeFailure modeFailure) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(MainActivity.this,modeFailure.getMessage(),Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }).start();
                            }else {
                                Toast.makeText(MainActivity.this,"当前已是主动模式",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            if (myClient.isPassive()){
                                Toast.makeText(MainActivity.this,"当前已是被动模式",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setPassive();
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure modeFailure) {
                                    Toast.makeText(MainActivity.this,modeFailure.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                builder2.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog2 = builder2.create();
                dialog2.show();
                break;
            case R.id.type:
                final String[] items2 = {"ASCII", "Binary"};
                AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
                builder3.setTitle("设置TYPE");

                int checkedItem2 = 1;
                if (myClient.isAscii()){
                    checkedItem2 = 0;
                }

                builder3.setSingleChoiceItems(items2, checkedItem2,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            if (myClient.isAscii()){
                                Toast.makeText(MainActivity.this,"当前已是ASCII模式",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setType("A");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (TypeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            if (myClient.isAscii()){
                                try {
                                    myClient.setType("B");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (TypeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(MainActivity.this,"当前已是Binary模式",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                builder3.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog3 = builder3.create();
                dialog3.show();
                break;
            case R.id.mode:
                final String[] items3 = {"Stream", "Block","Compress"};
                AlertDialog.Builder builder4 = new AlertDialog.Builder(this);
                builder4.setTitle("设置MODE");

                int checkedItem3 = 0;
                if (myClient.getMode() == MyClient.Mode.Block){
                    checkedItem3 = 1;
                }else if (myClient.getMode() == MyClient.Mode.Compressed){
                    checkedItem3 = 2;
                }

                builder4.setSingleChoiceItems(items3, checkedItem3,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            if (myClient.getMode() == MyClient.Mode.Stream){
                                Toast.makeText(MainActivity.this,"当前已是Stream模式",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setTransferMode("S");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else if (i==1){
                            if (myClient.getMode() == MyClient.Mode.Block){
                                Toast.makeText(MainActivity.this,"当前已是Block模式",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setTransferMode("B");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            if (myClient.getMode() == MyClient.Mode.Compressed){
                                Toast.makeText(MainActivity.this,"当前已是Compressed模式",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setTransferMode("C");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                builder4.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog4 = builder4.create();
                dialog4.show();
                break;
            case R.id.structure:
                final String[] items4 = {"File", "Record","Page"};
                AlertDialog.Builder builder5 = new AlertDialog.Builder(this);
                builder5.setTitle("设置MODE");

                int checkedItem4 = 0;
                if (myClient.getStructure() == MyClient.Structure.Record){
                    checkedItem4 = 1;
                }else if (myClient.getStructure() == MyClient.Structure.Page){
                    checkedItem4 = 2;
                }

                builder5.setSingleChoiceItems(items4, checkedItem4, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            if (myClient.getStructure() == MyClient.Structure.File){
                                Toast.makeText(MainActivity.this,"当前已是File结构",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setStructure("F");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else if (i==1){
                            if (myClient.getStructure() == MyClient.Structure.Record){
                                Toast.makeText(MainActivity.this,"当前已是Record结构",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setStructure("R");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            if (myClient.getStructure() == MyClient.Structure.Page){
                                Toast.makeText(MainActivity.this,"当前已是页结构",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setStructure("P");
                                    Toast.makeText(MainActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                builder5.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog5 = builder5.create();
                dialog5.show();
                break;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    //初始化菜单栏
    private void InitMenu() {
        NavigationView navigationView=(NavigationView) findViewById(R.id.nav_view);
        MenuItem checkedItem = navigationView.getCheckedItem();
        if (checkedItem!=null && checkedItem.getItemId() == R.id.nav_close){
            checkedItem.setChecked(false);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //连接服务端
                    case R.id.nav_connecting:
                        Intent intent=new Intent(MainActivity.this,ConnectActivity.class);
                        startActivity(intent);
                        break;
                    //查看服务器文件
                    case R.id.nav_FtpServerDirectory:
                        if (myClient!=null && myClient.isConnect()){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        List<FTPFile> files = myClient.list("/");
                                        for (FTPFile file : files) {
                                            file.setPath("/"+file.getFilename());
                                            boolean check = false;
                                            for (FTPFile ftpFile : ftpList) {
                                                if (ftpFile.getFilename().equals(file.getFilename())) {
                                                    check = true;
                                                    break;
                                                }
                                            }
                                            if (!check)
                                                ftpList.add(file);
                                        }
                                        FTPAdapter.setFtpList(ftpList);
                                        FTPAdapter.notifyDataSetChanged();
                                    } catch (SocketError | DownloadException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        item.setChecked(false);
                                    }
                                }
                            }).start();
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(ftpList.isEmpty())
                                        Toast.makeText(MainActivity.this,"还未连接服务器，请连接服务器后再查看！",Toast.LENGTH_SHORT).show();
                                }
                            });
                            item.setChecked(false);
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (recyclerView!=null){
                                    recyclerView.setAdapter(FTPAdapter);
                                    FTPAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        break;
                    //查看本机文件
                    case R.id.nav_LocalDirectory:
                        if (recyclerView!=null){
                            InitData();
                            adapter.setArraylist(fileList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //断开连接
                    case R.id.nav_close:
                        item.setChecked(false);
                        disconnect();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });
    }

    //回退按钮
    @Override
    public void onBackPressed(){
        if(recyclerView!=null && recyclerView.getAdapter()==adapter) {
            adapter.LastFile();
        } else if (recyclerView!=null && recyclerView.getAdapter()==FTPAdapter){
            FTPAdapter.LastFile();
        }
    }

    //获取权限
    private void getPermissions() {
        //SD卡写入
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED  || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},10086);
        }else {
            setContentView(R.layout.activity_main);
            Init();
        }
        /*
        //获取位置信息
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
         */
    }

    //获取权限回显
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10086) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContentView(R.layout.activity_main);
                Init();
            } else {
                Toast.makeText(this, "没有权限打开存储路径", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_cannot_get_file);
                Init();
            }
        }
    }

    //外部存储
    private boolean InitData() {
        File file = Environment.getExternalStorageDirectory();
        if (file.listFiles() != null){
            fileList=new ArrayList<File>( Arrays.asList(Objects.requireNonNull(file.listFiles())));
            return true;
        }else {
            fileList=new ArrayList<>();
            Toast.makeText(this, "无法获取目录", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //broadcast
    class ConnectBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.connected".equals(intent.getAction().toString())) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        myClient= FtpUtil.getMyClient();
                        FTPAdapter.setMyClient(myClient);
                    }
                }).start();
            }
        }
    }

    private void InitBroadCast() {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.example.connected");
        ConnectBroadcastReceiver connectBroadcastReceiver = new ConnectBroadcastReceiver();
        registerReceiver(connectBroadcastReceiver,intentFilter);
    }

    //断开连接
    private void disconnect(){
        if (FtpUtil.getMyClient()!=null && FtpUtil.getMyClient().isConnect()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (FtpUtil.getMyClient() != null) {
                            if (FtpUtil.getMyClient().isConnect()) {//当前正在连接
                                FtpUtil.getMyClient().disconnect();
                                ftpList.clear();
                                runOnUiThread(new Runnable() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void run() {
                                        FTPAdapter.notifyDataSetChanged();
                                    }
                                });
                                threadToast("连接已断开");
                            } else {
                                //当前没有连接
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
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}