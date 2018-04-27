package com.example.caucse.baseui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

public class Capture_Activity extends Activity {
    ImageView img = null;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,1);
        img=(ImageView)findViewById(R.id.img);
    }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        img.setImageURI(data.getData());
    }
}
