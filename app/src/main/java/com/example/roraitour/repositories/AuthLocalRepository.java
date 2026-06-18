package com.example.roraitour.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.example.roraitour.database.DatabaseHelper;
import com.example.roraitour.models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AuthLocalRepository {

    private final DatabaseHelper dbHelper;
    private final SharedPreferences prefs;

    public AuthLocalRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public long saveUser(String uid, String name, String email, String plainPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid == null ? "" : uid);
        values.put("name", name);
        values.put("email", email);
        values.put("password_hash", hashPassword(plainPassword));
        values.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        long rowId = db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId != -1) {
            prefs.edit()
                    .putString("local_user_uid", uid == null ? "" : uid)
                    .putString("local_user_name", name == null ? "" : name)
                    .putString("local_user_email", email == null ? "" : email)
                    .apply();
        }
        return rowId;
    }

    public User authenticate(String email, String plainPassword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT uid, name, email FROM users WHERE email = ? AND password_hash = ?", new String[]{email, hashPassword(plainPassword)});
        try {
            if (cursor.moveToFirst()) {
                User u = new User();
                u.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                u.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                // persist session
                prefs.edit()
                        .putString("local_user_uid", u.getUid() == null ? "" : u.getUid())
                        .putString("local_user_name", u.getName() == null ? "" : u.getName())
                        .putString("local_user_email", u.getEmail() == null ? "" : u.getEmail())
                        .apply();
                return u;
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public void setLoggedOut() {
        prefs.edit()
                .remove("local_user_uid")
                .remove("local_user_name")
                .remove("local_user_email")
                .apply();
    }

    private String hashPassword(String password) {
        if (password == null) return "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toString(password.hashCode());
        }
    }
}
