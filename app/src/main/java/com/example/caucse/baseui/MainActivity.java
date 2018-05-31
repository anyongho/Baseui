package com.example.caucse.baseui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private int id_view;
    Button capture_btn;
    Button exit_btn;
    Button db_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        capture_btn = (Button) findViewById(R.id.recapture);
        exit_btn = (Button) findViewById(R.id.exit);
        db_btn = (Button) findViewById(R.id.db_btn);
        //촬영버튼
        capture_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                 DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), Capture_Activity.class);
                        startActivity(intent);
                    }
                };
                DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), Album_Activity.class);
                        startActivity(intent);
                    }
                };

                DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    }
                };

               new AlertDialog.Builder(MainActivity.this)
                       .setTitle("이미지 업로드 방법 선택")
                       .setMessage("이미지 업로드 방법을 선택하세요")
                       .setPositiveButton("사진촬영", cameraListener)
                       .setNeutralButton("앨범선택", albumListener)
                       .setNegativeButton("취소", cancelListener)
                       .show();
            }
        });
        //DB버튼
        db_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewActivity.class);
                startActivity(intent);
            }
        });
        //종료버튼
        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
