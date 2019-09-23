package com.hht.sharelib.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;

/**
 * @author by  zhengshaorui on 2019/9/10
 * Describe: 创建一些缓存路径，用来存储临时文件
 */
public class Foo {
    public static Handler HANDLER = new Handler(Looper.getMainLooper());
    public static final String FILE_START = "--f_start ";
    public static final String FILE_END = "--f_end ";
    public static final byte TYPE_TRANS = 0X01;
    public static final byte TYPE_ACK = 0X02;
    public static final byte BG = 0X03;
    public static final byte IMAGE = 0X04;



    public static String createCacheDir(String name){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(path,name);
        if (!file.exists()){
            if (!file.mkdirs()){
                throw  new RuntimeException("cannot create path: "+file.getAbsolutePath());
            }
        }
        return file.getAbsolutePath();
    }

    public static File createNewFile(String folderName,String name){
        String parentPath = createCacheDir(folderName);
       // String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(parentPath,name);
        if (!dir.exists()){
            try {
                dir.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dir;
    }

    public static boolean deleteFolder(String folderName){
        File dir = new File(folderName);
        if (dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File file : files) {
                file.delete();
            }
            return dir.delete();
        }
        return false;
    }
}
