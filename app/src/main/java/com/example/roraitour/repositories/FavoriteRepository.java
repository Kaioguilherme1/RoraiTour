package com.example.roraitour.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.roraitour.database.DatabaseHelper;
import com.example.roraitour.models.FavoritePlace;
import com.example.roraitour.models.TouristPlace;

import java.util.ArrayList;
import java.util.List;

public class FavoriteRepository {
    private final DatabaseHelper dbHelper;

    public FavoriteRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long save(TouristPlace place) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("xid", place.getXid());
        values.put("name", place.getDisplayName());
        values.put("category", place.getCategory());
        values.put("latitude", place.getLatitude());
        values.put("longitude", place.getLongitude());
        values.put("image", place.getImage());
        values.put("description", place.getDescription());
        values.put("distance", place.getDistance());
        values.put("is_visited", place.isVisited() ? 1 : 0);
        return db.insert(DatabaseHelper.TABLE_FAVORITE, null, values);
    }

    public void updateVisited(String xid, String name, boolean visited) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_visited", visited ? 1 : 0);
        db.update(DatabaseHelper.TABLE_FAVORITE, values, "xid = ? OR name = ?", new String[]{xid == null ? "" : xid, name});
    }

    public boolean isFavorite(String xid, String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + DatabaseHelper.TABLE_FAVORITE + " WHERE xid = ? OR name = ?",
                new String[]{xid == null ? "" : xid, name == null ? "" : name}
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public int deleteByPlace(TouristPlace place) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                DatabaseHelper.TABLE_FAVORITE,
                "xid = ? OR name = ?",
                new String[]{place.getXid() == null ? "" : place.getXid(), place.getDisplayName()}
        );
    }

    public List<FavoritePlace> getAll() {
        List<FavoritePlace> places = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_FAVORITE + " ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            FavoritePlace place = new FavoritePlace();
            place.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            place.setXid(cursor.getString(cursor.getColumnIndexOrThrow("xid")));
            place.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            place.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
            place.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
            place.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
            place.setImage(cursor.getString(cursor.getColumnIndexOrThrow("image")));
            place.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            place.setDistance(cursor.getDouble(cursor.getColumnIndexOrThrow("distance")));
            place.setVisited(cursor.getInt(cursor.getColumnIndexOrThrow("is_visited")) == 1);
            places.add(place);
        }
        cursor.close();
        return places;
    }
}

