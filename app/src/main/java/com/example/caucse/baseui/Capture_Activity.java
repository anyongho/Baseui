package com.example.caucse.baseui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Capture_Activity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String imageFilePath;
    private Uri photoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        sendTakePhotoIntent();// camera intent 호출
        ////// 이 사이에서 맥주를 판별해내는 알고리즘이 들어가야 할 것이다.

        TextView beerName = (TextView) findViewById(R.id.name);
        TextView beerCountry = (TextView) findViewById(R.id.country);
        TextView beerFlavor = (TextView) findViewById(R.id.flavor);
        TextView beerKind = (TextView) findViewById(R.id.kind);
        TextView beerIBU = (TextView) findViewById(R.id.ibu);
        TextView beerAlcohol = (TextView) findViewById(R.id.alcohol);
        TextView beerKcal = (TextView) findViewById(R.id.kcal);
        DBHandler dbHandler = DBHandler.open(this);
        try {
            int ID = 3; // 3번째인 cass로 가정한다.
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
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
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
}
