package com.jitsik.onemage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void browsePressed(View view) {
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
    }

}
