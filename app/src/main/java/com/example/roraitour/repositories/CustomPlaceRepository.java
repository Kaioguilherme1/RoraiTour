package com.example.roraitour.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.roraitour.database.DatabaseHelper;
import com.example.roraitour.models.CustomPlace;

import java.util.ArrayList;
import java.util.List;

public class CustomPlaceRepository {
    private final DatabaseHelper dbHelper;

    public CustomPlaceRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long create(CustomPlace place) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(place);
        return db.insert(DatabaseHelper.TABLE_CUSTOM, null, values);
    }

    public int update(CustomPlace place) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(place);
        return db.update(DatabaseHelper.TABLE_CUSTOM, values, "id = ?", new String[]{String.valueOf(place.getId())});
    }

    public int delete(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_CUSTOM, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateVisited(int id, boolean visited) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_visited", visited ? 1 : 0);
        db.update(DatabaseHelper.TABLE_CUSTOM, values, "id = ?", new String[]{String.valueOf(id)});
    }

    public CustomPlace getById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CUSTOM + " WHERE id = ?", new String[]{String.valueOf(id)});
        CustomPlace place = null;
        if (cursor.moveToFirst()) {
            place = fromCursor(cursor);
        }
        cursor.close();
        return place;
    }

    public List<CustomPlace> getAll() {
        List<CustomPlace> places = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CUSTOM + " ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            places.add(fromCursor(cursor));
        }
        cursor.close();
        return places;
    }

    private ContentValues toContentValues(CustomPlace place) {
        ContentValues values = new ContentValues();
        values.put("name", place.getName());
        values.put("description", place.getDescription());
        values.put("category", place.getCategory());
        values.put("latitude", place.getLatitude());
        values.put("longitude", place.getLongitude());
        values.put("image", place.getImage());
        values.put("history", place.getHistory());
        values.put("created_at", place.getCreatedAt());
        values.put("is_visited", place.isVisited() ? 1 : 0);
        return values;
    }

    private CustomPlace fromCursor(Cursor cursor) {
        CustomPlace place = new CustomPlace();
        place.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        place.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        place.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        place.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
        place.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
        place.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
        place.setImage(cursor.getString(cursor.getColumnIndexOrThrow("image")));
        place.setHistory(cursor.getString(cursor.getColumnIndexOrThrow("history")));
        place.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        place.setVisited(cursor.getInt(cursor.getColumnIndexOrThrow("is_visited")) == 1);
        return place;
    }
}

