package com.example.caucse.baseui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Capture_Activity extends AppCompatActivity {
   ///tensorflow작업에 필요한 변수들
   private static final int INPUT_SIZE = 299; //이미지 사이즈
    private static final int IMAGE_MEAN = 0;
    private static final float IMAGE_STD = 255.0f;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";
    ///tensorflow 파일 위치
    private static final String MODEL_FILE = "file:///android_asset/rounded_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/output_labels.txt";

   ///사진 촬영에 필요한 변수들
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String imageFilePath;
    private Uri photoUri;
    private Classifier classifier;
    private Executor executor = Executors.newSingleThreadExecutor(); //쓰레드 동작
    private TextView txtResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        txtResult = (TextView)findViewById(R.id.txtResult);
        grantUriPermission();
        sendTakePhotoIntent();// camera intent 호출
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //텐서플로우 초기화 및 그래프파일 메모리에 탑재
        initTensorFlowAndLoadModel();

        ////// 데이터베이스 정보가져오기
        TextView beerName = (TextView) findViewById(R.id.name);
        TextView beerCountry = (TextView) findViewById(R.id.country);
        TextView beerFlavor = (TextView) findViewById(R.id.flavor);
        TextView beerKind = (TextView) findViewById(R.id.kind);
        TextView beerIBU = (TextView) findViewById(R.id.ibu);
        TextView beerAlcohol = (TextView) findViewById(R.id.alcohol);
        TextView beerKcal = (TextView) findViewById(R.id.kcal);
        DBHandler dbHandler = DBHandler.open(this);
        try {
            int ID = 2; // 3번째인 cass로 가정한다.
            Cursor cursor = dbHandler.select(ID);
            if (cursor.getCount() == 0) {
                Toast.makeText(this, "데이터가 없습니다.",
                        Toast.LENGTH_SHORT).show();
            } else {
                String Name = cursor.getString(cursor.getColumnIndex("Name"));
                String Country = cursor.getString(cursor.getColumnIndex("Country"));
                String Flavor = cursor.getString(cursor.getColumnIndex("Flavor"));
                String Kind = cursor.getString(cursor.getColumnIndex("Kind"));
                Float IBU = cursor.getFloat(cursor.getColumnIndex("IBU"));
                Float Alcohol = cursor.getFloat(cursor.getColumnIndex("Alcohol"));
                int kcal = cursor.getInt(cursor.getColumnIndex("kcal"));
                beerName.setText(Name);
                beerCountry.setText(Country);
                beerFlavor.setText(Flavor);
                beerKind.setText(Kind);
                beerIBU.setText(String.valueOf(IBU));
                beerAlcohol.setText(String.valueOf(Alcohol));
                beerKcal.setText(String.valueOf(kcal));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // 이미지는 안드로이드용 텐서플로우가 인식할 수 있는 포맷인 비트맵으로 변환해서 텐서플로우에 넘깁니다

            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            recognize_bitmap(bitmap);
            //            //권한 확인
            grantUriPermission();
            ExifInterface exif = null;

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int exifOrientation;
            int exifDegree;

            if (exif != null) {
                exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                exifDegree = exifOrientationToDegrees(exifOrientation);
            } else {
                exifDegree = 0;
            }

            ((ImageView)findViewById(R.id.img)).setImageBitmap(rotate(bitmap, exifDegree));
        }else {
        finish();
        }
    }

    //상수를 받아 각도로 변환시켜주는 메소드
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //비트맵을 각도대로 회전시켜 결과를 반환해주는 메소드이다.
    private Bitmap rotate(Bitmap bitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    //카메라 intent 함수
    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    //이미지가 저장될 파일 만들어내기
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Beer_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;
    }
    private void grantUriPermission() {
        //갤러리, 카메라 사용 권한 체크

        if(ContextCompat.checkSelfPermission(Capture_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            //최초 권한 요청인지 혹은 사용자에게 의한 재요청인지 확인
            if(ActivityCompat.shouldShowRequestPermissionRationale(Capture_Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //사용자가 임의로 권한을 취소하면 권한 재요청
                ActivityCompat.requestPermissions(Capture_Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                ActivityCompat.requestPermissions(Capture_Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else if(ContextCompat.checkSelfPermission(Capture_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            //최초 권한인지 혹은 사용자에게 의한 재요청인지 확인
            if(ActivityCompat.shouldShowRequestPermissionRationale(Capture_Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                // 사용자가 임의로 권한을 취소하면 권한재요청
                ActivityCompat.requestPermissions(Capture_Activity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }else {
                ActivityCompat.requestPermissions(Capture_Activity.this,  new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1 );
            }
        }else if(ContextCompat.checkSelfPermission(Capture_Activity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            // 최초 권한 요청인지 혹은 사용자에게 의한 재요청인지 확인
            if(ActivityCompat.shouldShowRequestPermissionRationale(Capture_Activity.this, Manifest.permission.CAMERA)){
                // 사용자가 임의로 권한을 취소 시킨 경우, 권한 재요청
                ActivityCompat.requestPermissions(Capture_Activity.this, new String[]{Manifest.permission.CAMERA}, 1);
            }else {
                ActivityCompat.requestPermissions(Capture_Activity.this, new String[]{Manifest.permission.CAMERA}, 1);
            }
    }
    //텐서플로우 초기화 및 그래프파일 메모리에 탑재
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    //비트맵 인식 및 결과표시
    private void recognize_bitmap(Bitmap bitmap) {

        // 비트맵을 처음에 정의된 INPUT SIZE에 맞춰 스케일링 (상의 왜곡이 일어날수 있는데, 이건 나중에 따로 설명할게요)
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        // classifier 의 recognizeImage 부분이 실제 inference 를 호출해서 인식작업을 하는 부분.
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        // 결과값은 Classifier.Recognition 구조로 리턴되는데, 원래는 여기서 결과값을 배열로 추출가능하지만,

        // 여기서는 간단하게 그냥 통째로 txtResult에 뿌려줍니다.
       txtResult.setText(results.toString());
    }
}
