package com.ishinomakihackathon2016.bluespring.vr;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity implements View.OnClickListener{

    private Toolbar mToolbar;
    private ImageButton mShareButton;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getColor(R.color.toolbar_title));
        mToolbar.setTitle(getString(R.string.app_name));

        mShareButton = (ImageButton)findViewById(R.id.share_button);
        mShareButton.setOnClickListener(this);

        mLoginButton = (Button)findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.share_button){

        }else if( id == R.id.login_button){

        }
    }
}
