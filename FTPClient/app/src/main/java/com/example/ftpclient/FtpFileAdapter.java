package com.example.ftpclient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ftpclient.control.FtpUtil;
import com.example.ftpclient.control.MyClient;
import com.example.ftpclient.data.FTPFile;
import com.example.ftpclient.exception.DownloadException;
import com.example.ftpclient.exception.SocketError;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FtpFileAdapter extends RecyclerView.Adapter<FtpFileAdapter.ViewHolder> {
    private List<FTPFile> FtpList;
    private MyClient myClient;

    static class ViewHolder extends RecyclerView.ViewHolder{
       ImageView File_imageview;
       TextView File_textView;

        public ViewHolder(View v){
           super(v);
           File_imageview=(ImageView) v.findViewById(R.id.file_image);
           File_textView=(TextView) v.findViewById(R.id.file_name);
        }
    }

    public FtpFileAdapter(List<FTPFile> list, MainActivity activity){
        FtpList=list;
        setMyClient(FtpUtil.getMyClient());
    }

    public void setFtpList(List<FTPFile> ftpList) {
        FtpList = ftpList;
    }

    public void setMyClient(MyClient client){
        this.myClient = client;
    }

    @Override
    public int getItemCount() {
        return FtpList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.directory_item,parent,false);
        final ViewHolder viewHolder=new ViewHolder(view);

        //点击监听
        view.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
               final FTPFile file = FtpList.get(viewHolder.getAdapterPosition());
               if(file!=null && file.isDirectory()) {
                   try {
                       List<FTPFile> list = myClient.list(file.getPath());
                       file.setFileList(list);
                       for (FTPFile f:list){
                           f.setPath(file.getPath()+"/"+f.getFilename());
                           f.setParentFile(file);
                       }
                       FtpList.clear();
                       FtpList.addAll(list);
                       notifyDataSetChanged();
                   } catch (SocketError | DownloadException e) {
                       Toast.makeText(MyApplication.getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                   }
               }
            }
        });

        //长摁监听
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //创建弹出式菜单对象
                PopupMenu popup = new PopupMenu(MyApplication.getContext(), v);
                final MenuInflater inflater = popup.getMenuInflater();//获取菜单填充器
                inflater.inflate(R.menu.server_operation, popup.getMenu());//填充菜单
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FTPFile file=FtpList.get(viewHolder.getAdapterPosition());
                        if (item.getItemId() == R.id.download) {
                            //TODO
                            Toast.makeText(MyApplication.getContext(),"download successfully",Toast.LENGTH_SHORT).show();
                        }
                        notifyDataSetChanged();
                        return true;
                    }
                });

                popup.show();
                return true;
            }
        });

        return viewHolder;
    }

    //展示图标及文件名
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FTPFile ftpFile=FtpList.get(position);
        holder.File_textView.setText(ftpFile.getFilename());
        if(ftpFile.isDirectory()){
            holder.File_imageview.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.directory));
        }else{
            holder.File_imageview.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.file));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void LastFile(){//切换到父目录
        FTPFile parent = FtpList.get(0).getParentFile();
        if (parent!=null){
            String parentPath = parent.getFilename();
            try {
                List<FTPFile> list = myClient.list(parentPath);
                FtpList.clear();
                FtpList.addAll(list);
                notifyDataSetChanged();
            }catch (SocketError | DownloadException e){
                Toast.makeText(MyApplication.getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
}
