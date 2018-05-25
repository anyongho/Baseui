package com.example.caucse.baseui;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.StringTokenizer;



import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Capture_Activity extends AppCompatActivity {
   ///tensorflow작업에 필요한 변수들
    String str1[] = new String[10];
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
    private TextView beerName;
    private TextView beerCountry;
    private TextView beerFlavor;
    private TextView beerKind;
    private TextView beerIBU;
    private TextView beerAlcohol;
    private TextView beerKcal;
    //tensorflow 결과값 total_num
    private int total_num;
    //progress bar
    private static final int PROGRESS_DIALOG = 100;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        grantUriPermission();//카메라 앨범 권한확인
        //텐서플로우 초기화 및 그래프파일 메모리에 탑재
        initTensorFlowAndLoadModel();
        //텐서플로우 초기화를 위한 handler for delay
        showDialog(PROGRESS_DIALOG);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run(){
                // camera intent 호출
                dismissDialog(PROGRESS_DIALOG);
                sendTakePhotoIntent();
            }
          }, 3000); //3초뒤에 호출
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            //bitmap 전환
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
            setContentView(R.layout.activity_capture);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ImageButton kind = (ImageButton) findViewById(R.id.imageButton);
            // kind 정보 버튼을 클릭하면 동작하는 정보창
            kind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };
                    new android.support.v7.app.AlertDialog.Builder(Capture_Activity.this)
                            .setTitle("kind 정보")
                            .setMessage("맥주의 종류는 다음과 같다.")
                            .setMessage("1) Lager : 하면(下面) 발효방식으로 제조한 맥주를 말한다. 알코올 도수가 낮은 편. 향과 깊은 맛이 적은 대신 깔끔하고 시원한 청량감을 가지고 있다\n" +
                                    "2) Pilsner : Lager의 한 종류.체코 스타일 맥주로 밝고 투명한 노란빛의 맥주이다. 홉의 쌉쌀한 맛이 느껴지나 시원하고 상큼한 향이 특징이다.\n" +
                                    "3) Dunkel : Lager의 한 종류. 둥켈은 독일어로 Dark를 뜻하며, 독일 스타일 라거 흑맥주이다. 검게 볶은 보리를 사용하며, 고소하고 은은한 맛과 향이 난다. 스타우트보다 쓴맛이 적은 편이다.\n" +
                                    "4) Witbier : 벨기에식 밀맥주.연하고 가벼운 질감과 무게감을 가졌고 평균 알코올 도수는 4.5~5.5% 라 산뜻하게 즐기기 좋다\n" +
                                    "5) Stout : 상면발효방식으로 생산되는 영국식 맥주의 한 종류. 맥아 또는 보리를 볶아서 제조하기 때문에 탄 맛이 나는 것이 특징\n" +
                                    "6) Vodka : 순도 100%의 vodka가 아닌 10%정도의 vodka를 함량한 맥주\n" +
                                    "7) Radler : 레모네이드, 소다 등의 소프트 드링크(Soft Drink)와 밝은색 라거 맥주의 혼합주.\n" +
                                    "8) German Hefeweizen : 독일식 밀맥주. 거품이 매우 부드럽고, 탄산이 비교적 적은 편")
                            .setNegativeButton("닫기", cancelListener)
                            .show();
                }
            });
            // IBU 정보 버튼을 클릭하면 동작하는 정보창
            ImageButton IBU = (ImageButton) findViewById(R.id.imageButton2);
            // kind search 버튼을 클릭하면 동작하는 정보창
            IBU.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };
                    new android.support.v7.app.AlertDialog.Builder(Capture_Activity.this)
                            .setTitle("IBU 정보")
                            .setMessage("IBU : International Bitterness Unit에 약자\n" + "숫자가 클수록 쓴맛이 강한것을 의미한다\n" + "홉의 쓴맛이 강조된 스타일이 아니라면 미표기인 경우가 많다")
                            .setNegativeButton("닫기", cancelListener)
                            .show();
                }
            });

            ImageButton ABV = (ImageButton) findViewById(R.id.imageButton3);
            // kind search 버튼을 클릭하면 동작하는 정보창
            ABV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };
                    new android.support.v7.app.AlertDialog.Builder(Capture_Activity.this)
                            .setTitle("ABV 정보")
                            .setMessage("ABV : Alcohol By volume에 약자" + "보통 %로 표시하며 몇 도다 하는 표현으로 쓰임\n" +"대한민국의 대부분의 라거 맥주들이 4~5%정도. 소주는 19.5%")
                            .setNegativeButton("닫기", cancelListener)
                            .show();
                }
            });
            ImageButton kcal = (ImageButton) findViewById(R.id.imageButton4);
            // kind search 버튼을 클릭하면 동작하는 정보창
            kcal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };
                    new android.support.v7.app.AlertDialog.Builder(Capture_Activity.this)
                            .setTitle("kcal 정보")
                            .setMessage("맥주 한캔 355ml를 기준으로 kcal 정보를 제공")
                            .setNegativeButton("닫기", cancelListener)
                            .show();
                }
            });
            txtResult = (TextView)findViewById(R.id.txtResult);
            beerName = (TextView) findViewById(R.id.name);
            beerCountry = (TextView) findViewById(R.id.country);
            beerFlavor = (TextView) findViewById(R.id.flavor);
            beerKind = (TextView) findViewById(R.id.kind);
            beerIBU = (TextView) findViewById(R.id.ibu);
            beerAlcohol = (TextView) findViewById(R.id.alcohol);
            beerKcal = (TextView) findViewById(R.id.kcal);
            // 이미지는 안드로이드용 텐서플로우가 인식할 수 있는 포맷인 비트맵으로 변환해서 텐서플로우에 넘깁니다
            recognize_bitmap(bitmap);
            ((ImageView)findViewById(R.id.img)).setImageBitmap(rotate(bitmap, exifDegree));
            database();
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
    //database sql 함수
    private void database(){
        int ID = 3;
        DBHandler dbHandler = DBHandler.open(this);
        try {
            if (str1[0].equals("kgb")) {
                ID = 11;
            } else if (str1[0].equals("heineken")) {
                ID = 7;
            } else if (str1[0].equals("paulaner")) {
                ID = 24;
            } else if (str1[0].equals("tsingtao")) {
                ID = 1;
            } else if (str1[0].equals("kronenbourg")) {
                ID = 21;
            } else if (str1[0].equals("tiger")) {
                ID = 23;
            } else if (str1[0].equals("san")) {
                ID = 16;
            } else if (str1[0].equals("pilsner")) {
                ID = 25;
            } else if (str1[0].equals("desperados")) {
                ID = 13;
            } else if (str1[0].equals("krombacher")) {
                ID = 22;
            } else if (str1[0].equals("suntory")) {
                ID = 17;
            } else if (str1[0].equals("kirin")) {
                ID = 12;
            } else if (str1[0].equals("filite")) {
                ID = 9;
            } else if (str1[0].equals("stella")) {
                ID = 18;
            } else if (str1[0].equals("carlsberg")) {
                ID = 10;
            } else if (str1[0].equals("cass")) {
                ID = 3;
            } else if (str1[0].equals("guinness")) {
                ID = 6;
            } else if (str1[0].equals("hoegaarden")) {
                ID = 4;
            } else if (str1[0].equals("asahi")) {
                ID = 5;
            } else if (str1[0].equals("sapporo")) {
                ID = 8;
            } else if (str1[0].equals("kozel dark")) {
                ID = 20;
            } else if (str1[0].equals("max")) {
                ID = 14;
            } else if (str1[0].equals("yebisu")) {
                ID = 19;
            } else if (str1[0].equals("budweiser")) {
                ID = 15;
            } else if (str1[0].equals("hite")) {
                ID = 2;
            } else{
                Toast.makeText(this, "정보를 추출해내지 못했습니다.",
                        Toast.LENGTH_SHORT).show();
            }
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

    //갤러리, 카메라 사용 권한 체크
    private void grantUriPermission() {
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
        total_num=0;
        // 비트맵을 처음에 정의된 INPUT SIZE에 맞춰 스케일링 (상의 왜곡이 일어날수 있는데, 이건 나중에 따로 설명할게요)
        bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);
        // classifier 의 recognizeImage 부분이 실제 inference 를 호출해서 인식작업을 하는 부분.
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        // 결과값은 Classifier.Recognition 구조로 리턴되는데, 원래는 여기서 결과값을 배열로 추출가능하지만,
        String result = results.toString() ;

        // 결과값을 원하는대로 파싱하기
        StringTokenizer st1 = new StringTokenizer(result, "[](),%1234567890. ");
        while(st1.hasMoreTokens()) {
          str1[total_num] = st1.nextToken();
          total_num++;
        }
        // 여기서는 간단하게 classifier 정보를 통째로 txtResult에 뿌려줍니다.
        txtResult.setText(results.toString());

    }
    @Override
    protected Dialog onCreateDialog(int id){
        switch(id)
        {
            case PROGRESS_DIALOG:
                progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); //프로그레스 스타트
                progressDialog.setMessage("로딩중입니다...");
                break;
        }
        return progressDialog;
    }
}
