package com.zhengsr.shareclient;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhengsr.shareclient.frament.SearchFragment;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "MainActivity";
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, SearchFragment.newInstance())
                .commit();
    }



}
