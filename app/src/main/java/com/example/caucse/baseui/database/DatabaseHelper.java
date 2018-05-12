package com.example.caucse.baseui.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.caucse.baseui.model.Product;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "Beerinfo2";
    public static final String DBLOCATION = "/data/data/com.example.caucse.baseui/databases/";
    private Context mContext;
    private SQLiteDatabase mDatabase;



    public DatabaseHelper(Context context) {

        super(context, DBNAME, null, 1);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
     //필요없음
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
     //필요없음
    }

    public void openDatabase(){
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        if(mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null   , SQLiteDatabase.OPEN_READWRITE);
    }

    public void closeDatabase() {
        if(mDatabase != null) {
            mDatabase.close();
        }
    }


    public List<Product> getListProduct(){
        Product product = null;
        ArrayList<Product> productList = new ArrayList<Product>();
        openDatabase();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM Beerinfo", null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            product = new Product(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getFloat(5), cursor.getFloat(6), cursor.getInt(7));
            productList.add(product);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return productList;
    }

}

