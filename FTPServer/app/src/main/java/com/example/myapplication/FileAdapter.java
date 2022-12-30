package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private final List<File> arraylist;
    private File CurrentFile = Environment.getExternalStorageDirectory();//当前的文件夹

    public FileAdapter(List<File> arraylist){
        this.arraylist = arraylist;
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

        //点击监听目录
        view.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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

        return holder;
    }

    //展示图标及文件名
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       File file=arraylist.get(position);
       holder.textView.setText(file.getName());
       if(file.isFile()){
         holder.imageView.setImageDrawable(ServerApplication.getContext().getDrawable(R.drawable.file));
       }else{
           holder.imageView.setImageDrawable(ServerApplication.getContext().getDrawable(R.drawable.directory));
       }
    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    public boolean isRoot(){
        if (CurrentFile.equals(Environment.getExternalStorageDirectory())){
            return true;
        }
        else {
            return false;
        }
    }

    //返回上一层
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("NotifyDataSetChanged")
    public void LastFile(){
        if (!CurrentFile.equals(Environment.getExternalStorageDirectory()) && CurrentFile!=null){
                CurrentFile = CurrentFile.getParentFile();
                arraylist.clear();
                arraylist.addAll(Arrays.asList(Objects.requireNonNull(CurrentFile.listFiles())));
                notifyDataSetChanged();
        }
    }
}
