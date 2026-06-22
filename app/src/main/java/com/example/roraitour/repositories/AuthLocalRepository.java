package com.example.roraitour.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

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
        if (email == null) return -1;
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("uid", uid == null ? "" : uid);
        values.put("name", name);
        values.put("email", cleanEmail);
        values.put("password_hash", hashPassword(plainPassword));
        values.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        
        long rowId = db.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (rowId != -1) {
            prefs.edit()
                    .putString("local_user_uid", uid == null ? "" : uid)
                    .putString("local_user_name", name == null ? "" : name)
                    .putString("local_user_email", cleanEmail)
                    .apply();
        }
        return rowId;
    }

    public boolean updateName(String email, String newName) {
        if (email == null) return false;
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", newName);
        int rows = db.update("users", values, "email = ?", new String[]{cleanEmail});
        if (rows > 0) {
            prefs.edit().putString("local_user_name", newName).apply();
            return true;
        }
        return false;
    }

    public boolean checkPassword(String email, String plainPassword) {
        if (email == null) return false;
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ? AND password_hash = ?", 
                new String[]{cleanEmail, hashPassword(plainPassword)})) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        if (email == null) return false;
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password_hash", hashPassword(newPassword));
        return db.update("users", values, "email = ?", new String[]{cleanEmail}) > 0;
    }

    public boolean updateProfileImage(String email, String imageUri) {
        if (email == null || email.isEmpty()) {
            Log.e("AuthRepo", "Tentativa de atualizar imagem com email nulo");
            return false;
        }
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("profile_image", imageUri);
        
        int rows = db.update("users", values, "email = ?", new String[]{cleanEmail});
        Log.d("AuthRepo", "Update imagem para " + cleanEmail + ". Linhas afetadas: " + rows);
        
        if (rows > 0) {
            prefs.edit().putString("local_user_image", imageUri).apply();
            return true;
        }
        return false;
    }

    public User authenticate(String email, String plainPassword) {
        if (email == null) return null;
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND password_hash = ?", 
                new String[]{cleanEmail, hashPassword(plainPassword)})) {
            if (cursor.moveToFirst()) {
                User u = new User();
                u.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
                u.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                u.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                String profileImage = cursor.getString(cursor.getColumnIndexOrThrow("profile_image"));
                
                prefs.edit()
                        .putString("local_user_uid", u.getUid() == null ? "" : u.getUid())
                        .putString("local_user_name", u.getName() == null ? "" : u.getName())
                        .putString("local_user_email", u.getEmail())
                        .putString("local_user_image", profileImage)
                        .apply();
                return u;
            }
        } catch (Exception e) {
            Log.e("AuthRepo", "Erro na autenticação", e);
        }
        return null;
    }

    public boolean userExistsInDatabase(String email) {
        if (email == null || email.isEmpty()) return false;
        String cleanEmail = email.trim().toLowerCase();
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{cleanEmail})) {
            return cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void setLoggedOut() {
        prefs.edit()
                .remove("local_user_uid")
                .remove("local_user_name")
                .remove("local_user_email")
                .remove("local_user_image")
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getString("local_user_email", null) != null;
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
