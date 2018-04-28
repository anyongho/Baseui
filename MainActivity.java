package com.example.caucse.baseui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class MainActivity extends Activity {
    static {
        System.loadLibrary("tensorflow_inference");
    }
    private static final String MODEL_FILE = "file:///assets/retrained_graph.pb";
    private TensorFlowInferenceInterface inferenceInterface;

    private void initMyModel(){
        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(getAssets(), MODEL_FILE);
    }

    Button capture_btn;
    Button exit_btn;
    Button db_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMyModel();
        capture_btn = (Button) findViewById(R.id.recapture);
        exit_btn = (Button) findViewById(R.id.exit);
        db_btn = (Button) findViewById(R.id.db_btn);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        capture_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Capture_Activity.class);
                startActivity(intent);
            }
        });

        exit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        db_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewActivity.class);
                startActivity(intent);
            }
        });

    }
}
