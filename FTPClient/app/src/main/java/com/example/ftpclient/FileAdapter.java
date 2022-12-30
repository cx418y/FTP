package com.example.ftpclient;

import android.annotation.SuppressLint;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private List<File> arraylist;
    private File CurrentFile=Environment.getExternalStorageDirectory();//当前的文件夹

    public FileAdapter(List<File> arraylist){
        this.arraylist=arraylist;
    }

    public File getCurrentFile() {
        return CurrentFile;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        public ViewHolder(View v){
            super(v);
            imageView=(ImageView)v.findViewById(R.id.file_image);
            textView=(TextView)v.findViewById(R.id.file_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item,parent,false);
        final ViewHolder holder =new ViewHolder(view);

        //点击监听
        view.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                File file=arraylist.get(holder.getAdapterPosition());
                if(file!=null && file.isDirectory()) {
                    CurrentFile=file;
                    arraylist.clear();
                    arraylist.addAll(Arrays.asList(Objects.requireNonNull(file.listFiles())));
                    notifyDataSetChanged();
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
                inflater.inflate(R.menu.operation, popup.getMenu());//填充菜单
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint({"NotifyDataSetChanged", "NonConstantResourceId"})
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        File file=arraylist.get(holder.getAdapterPosition());
                        if (item.getItemId() == R.id.upload) {
                            //TODO
                            Toast.makeText(MyApplication.getContext(),"upload successfully",Toast.LENGTH_SHORT).show();
                        }
                        notifyDataSetChanged();
                        return true;
                    }
                });
                popup.show();
                return true;
            }
        });
        return holder;
    }

    //展示图标及文件名
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {
       File file=arraylist.get(position);
       holder.textView.setText(file.getName());
       if(file.isFile()){
           holder.imageView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.file));
       }else{
           holder.imageView.setImageDrawable(MyApplication.getContext().getDrawable(R.drawable.directory));
       }
    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    //返回上一层
    @SuppressLint("NotifyDataSetChanged")
    public void LastFile(){
        if (!CurrentFile.equals(Environment.getExternalStorageDirectory()) && CurrentFile!=null){
            CurrentFile=CurrentFile.getParentFile();
            arraylist.clear();
            arraylist.addAll(Arrays.asList(Objects.requireNonNull(CurrentFile.listFiles())));
            notifyDataSetChanged();
        }
    }

    public void setArraylist(List<File> arraylist) {
        this.arraylist.clear();
        this.arraylist.addAll(arraylist);
    }
}
