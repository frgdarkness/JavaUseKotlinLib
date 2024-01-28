package com.zeroapp.bloodpresshelp.javaapptest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zeroapp.bloodpresshelp.kotlintoastlibrary.KotlinToastHelper;
import com.zeroapp.bloodpresshelp.kotlintoastlibrary.Student;
import com.zeroapp.bloodpresshelp.zeroimageeditor.ZeroImageEditorUtils;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NewStudent hoang = new NewStudent("", "");
        Student huy = new Student("", "");
        KotlinToastHelper.INSTANCE.showShortToast(getApplicationContext(), "hello kotlin");
        ZeroImageEditorUtils.INSTANCE.showEnhancerActivity(MainActivity.this);
    }
}