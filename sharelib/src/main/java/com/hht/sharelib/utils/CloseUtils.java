package com.hht.sharelib.utils;

import android.graphics.Color;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtils  {
    public static void close(Closeable... closeables){
        if (closeables == null){
            return;
        }
        if (closeables != null) {
            for (Closeable closeable : closeables) {
                try {
                    if (closeable != null) {
                        closeable.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
