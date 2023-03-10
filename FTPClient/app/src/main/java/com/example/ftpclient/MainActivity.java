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
        //?????????????????????
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.menu);
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("????????????...");

        if (getData){
            //????????????????????????????????????????????????
            recyclerView=(RecyclerView)findViewById(R.id.file_list);
            LinearLayoutManager layoutManager=new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            adapter=new FileAdapter(fileList);
            FTPAdapter=new FtpFileAdapter(ftpList,this);
            recyclerView.setAdapter(adapter);
        }
    }

    //??????Menu???????????????
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    //?????????????????????????????????????????????
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.setting:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setTitle("????????????");
                //    ??????LayoutInflater???????????????xml???????????????????????????View??????
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.setting_download_directory, null);
                //    ?????????????????????????????????????????????????????????Content
                builder1.setView(view);

                EditText downloadDirectory = (EditText)view.findViewById(R.id.downloadDirectory);
                downloadDirectory.setText(myClient.getDownloadDirectory());

                builder1.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String a = downloadDirectory.getText().toString().trim();
                        try {
                            myClient.setDownloadDirectory(a);
                            Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } catch (DownloadException e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder1.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog1 = builder1.create();
                dialog1.show();
                break;
            case R.id.passive:
                final String[] items1 = {"????????????", "????????????"};
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("??????????????????/????????????");

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
                                            Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(MainActivity.this,"????????????????????????",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            if (myClient.isPassive()){
                                Toast.makeText(MainActivity.this,"????????????????????????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setPassive();
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure modeFailure) {
                                    Toast.makeText(MainActivity.this,modeFailure.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                builder2.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                builder3.setTitle("??????TYPE");

                int checkedItem2 = 1;
                if (myClient.isAscii()){
                    checkedItem2 = 0;
                }

                builder3.setSingleChoiceItems(items2, checkedItem2,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            if (myClient.isAscii()){
                                Toast.makeText(MainActivity.this,"????????????ASCII??????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setType("A");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (TypeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            if (myClient.isAscii()){
                                try {
                                    myClient.setType("B");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (TypeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(MainActivity.this,"????????????Binary??????",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                builder3.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                builder4.setTitle("??????MODE");

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
                                Toast.makeText(MainActivity.this,"????????????Stream??????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setTransferMode("S");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else if (i==1){
                            if (myClient.getMode() == MyClient.Mode.Block){
                                Toast.makeText(MainActivity.this,"????????????Block??????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setTransferMode("B");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            if (myClient.getMode() == MyClient.Mode.Compressed){
                                Toast.makeText(MainActivity.this,"????????????Compressed??????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setTransferMode("C");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                builder4.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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
                builder5.setTitle("??????MODE");

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
                                Toast.makeText(MainActivity.this,"????????????File??????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setStructure("F");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else if (i==1){
                            if (myClient.getStructure() == MyClient.Structure.Record){
                                Toast.makeText(MainActivity.this,"????????????Record??????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setStructure("R");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else {
                            if (myClient.getStructure() == MyClient.Structure.Page){
                                Toast.makeText(MainActivity.this,"?????????????????????",Toast.LENGTH_SHORT).show();
                            }else {
                                try {
                                    myClient.setStructure("P");
                                    Toast.makeText(MainActivity.this,"????????????",Toast.LENGTH_SHORT).show();
                                } catch (ModeFailure e) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });

                builder5.setPositiveButton("??????", new DialogInterface.OnClickListener() {
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

    //??????????????????
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
                    //???????????????
                    case R.id.nav_connecting:
                        Intent intent=new Intent(MainActivity.this,ConnectActivity.class);
                        startActivity(intent);
                        break;
                    //?????????????????????
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
                                        Toast.makeText(MainActivity.this,"?????????????????????????????????????????????????????????",Toast.LENGTH_SHORT).show();
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
                    //??????????????????
                    case R.id.nav_LocalDirectory:
                        if (recyclerView!=null){
                            InitData();
                            adapter.setArraylist(fileList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //????????????
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

    //????????????
    @Override
    public void onBackPressed(){
        if(recyclerView!=null && recyclerView.getAdapter()==adapter) {
            adapter.LastFile();
        } else if (recyclerView!=null && recyclerView.getAdapter()==FTPAdapter){
            FTPAdapter.LastFile();
        }
    }

    //????????????
    private void getPermissions() {
        //SD?????????
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED  || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},10086);
        }else {
            setContentView(R.layout.activity_main);
            Init();
        }
        /*
        //??????????????????
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
         */
    }

    //??????????????????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10086) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContentView(R.layout.activity_main);
                Init();
            } else {
                Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                setContentView(R.layout.activity_cannot_get_file);
                Init();
            }
        }
    }

    //????????????
    private boolean InitData() {
        File file = Environment.getExternalStorageDirectory();
        if (file.listFiles() != null){
            fileList=new ArrayList<File>( Arrays.asList(Objects.requireNonNull(file.listFiles())));
            return true;
        }else {
            fileList=new ArrayList<>();
            Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
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

    //????????????
    private void disconnect(){
        if (FtpUtil.getMyClient()!=null && FtpUtil.getMyClient().isConnect()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (FtpUtil.getMyClient() != null) {
                            if (FtpUtil.getMyClient().isConnect()) {//??????????????????
                                FtpUtil.getMyClient().disconnect();
                                ftpList.clear();
                                runOnUiThread(new Runnable() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void run() {
                                        FTPAdapter.notifyDataSetChanged();
                                    }
                                });
                                threadToast("???????????????");
                            } else {
                                //??????????????????
                                threadToast("??????????????????????????????");
                            }
                        }
                    } catch (IOException e) {
                        threadToast("?????????????????????");
                    }
                }
            }).start();
        }else {
            threadToast("??????????????????????????????");
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