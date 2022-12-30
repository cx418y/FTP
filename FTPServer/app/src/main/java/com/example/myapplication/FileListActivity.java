package com.example.myapplication;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DrawerLayout mDrawerLayout;
    private FileAdapter adapter;
    private List<File> fileList;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermissions();
       // setContentView(R.layout.file_list);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void Init(){
        boolean getData = InitData();
        InitUI(getData);
    }


    //外部存储
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

    private void InitUI(boolean getData) {
        if (getData){
            //如果获取到目录就显示本机文件列表
            recyclerView=(RecyclerView)findViewById(R.id.file_list);
            LinearLayoutManager layoutManager=new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            adapter=new FileAdapter(fileList);
            //System.out.println("文件数量："+fileList.size());
            recyclerView.setAdapter(adapter);
        }
    }

    //获取权限
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getPermissions() {
        //SD卡写入
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED  || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},10086);
           // System.out.println("获取权限成功");
        }else {
            //System.out.println("获取权限失败");
            setContentView(R.layout.file_list);
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10086) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("获取权限成功");
                setContentView(R.layout.file_list);
                Init();
            } else {
                System.out.println("获取权限失败");
                Toast.makeText(this, "没有权限打开存储路径", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_cannot_get_file);
                Init();
            }
        }
    }

    //回退按钮
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onBackPressed(){
        if(!adapter.isRoot()) {
            adapter.LastFile();
        } else {
            Intent intent=new Intent(FileListActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }



}
