package com.dji.GSDemo.GaodeMap;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
//import android.support.v7.app.AppCompatActivity;

import com.dji.GSDemo.GaodeMap.utils.DialogUtils;

import androidx.appcompat.app.AppCompatActivity;

import static dji.midware.data.manager.P3.ServiceManager.getContext;

public class MenuActivity extends AppCompatActivity {

    private Intent a,b,c,d,e;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //全屏 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Button bt1 = (Button)findViewById(R.id.button7);
        Button bt2 = (Button)findViewById(R.id.button8);
        Button bt3 = (Button)findViewById(R.id.button9);
        Button bt4 = (Button)findViewById(R.id.button10);
        Button bt5 = (Button)findViewById(R.id.button11);

        MyListener listener = new MyListener();
        bt1.setTag(1);         //给button设置标记
        bt1.setOnClickListener(listener);
        bt2.setTag(2);
        bt2.setOnClickListener(listener);
        bt3.setTag(3);
        bt3.setOnClickListener(listener);
        bt4.setTag(4);
        bt4.setOnClickListener(listener);
        bt5.setTag(5);
        bt5.setOnClickListener(listener);



    }
    public class MyListener implements View.OnClickListener{
        TextView text = (TextView)findViewById(R.id.text);
        @Override
        public void onClick(View v){
            int tag = (Integer)v.getTag(); //找到每个button的标记
            switch(tag){
                case 1:
                    a = new Intent(MenuActivity.this,MainActivity.class);
                    startActivity(a);
                    break;
                case 2:
                    b = new Intent(MenuActivity.this,UxActivity.class);
                    startActivity(b);
                    break;
                case 3:
                    Intent launchIntent1 = getContext().getPackageManager().getLaunchIntentForPackage("dji.go.v4");
                    if (launchIntent1 != null) {
                        getContext().startActivity(launchIntent1);//null pointer check in case package name was not found
                    } else {
                        DialogUtils.showDialog(getContext(),
                                getResources()
                                        .getString(
                                                R.string.component_listview_redirect_go4_failure));
                    }
                    break;
                case 4:
                    Intent launchIntent2 = getContext().getPackageManager().getLaunchIntentForPackage("dji.pilot");
                    if (launchIntent2 != null) {
                        getContext().startActivity(launchIntent2);//null pointer check in case package name was not found
                    }else {
                        DialogUtils.showDialog(getContext(),
                                getResources()
                                        .getString(
                                                R.string.component_listview_redirect_go_failure));
                    }
                    break;
                case 5:
                    text.setText("5");
                    break;
            }
        }
    }

}

