package com.wodongx123.butterknifedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.wodongx123.butterknife_annotation.BindView;
import com.wodongx123.butterknife_annotation.OnClick;
import com.wodongx123.butterknife_reflection.ButterKnife;


public class MainActivity extends AppCompatActivity {


    @BindView(R.id.view)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        tv.setText("aaaaaa");
    }

    @OnClick(R.id.view)
    public void aaa(View view){
    }
}