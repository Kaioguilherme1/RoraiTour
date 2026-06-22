package com.example.roraitour.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.roraitour.utils.Constants;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_FAVORITE = "favorite_places";
    public static final String TABLE_CUSTOM = "custom_places";

    public DatabaseHelper(@Nullable Context context) {
        super(context, Constants.DB_NAME, null, Constants.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FAVORITE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "xid TEXT,"
                + "name TEXT,"
                + "category TEXT,"
                + "latitude REAL,"
                + "longitude REAL,"
                + "image TEXT,"
                + "description TEXT,"
                + "distance REAL"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_CUSTOM + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "description TEXT,"
                + "category TEXT,"
                + "latitude REAL,"
                + "longitude REAL,"
                + "image TEXT,"
                + "history TEXT,"
                + "created_at TEXT"
                + ")");

        // Users table for local authentication (email + hashed password)
        db.execSQL("CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "uid TEXT,"
                + "name TEXT,"
                + "email TEXT UNIQUE,"
                + "password_hash TEXT,"
                + "profile_image TEXT,"
                + "created_at TEXT"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOM);
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}

