package com.example.saludtotal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class CacheDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "salud_total_cache.db";
    private static final int DB_VERSION = 1;

    public CacheDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE especialidades (id TEXT PRIMARY KEY, nombre TEXT)");
        db.execSQL("CREATE TABLE medicos (id TEXT PRIMARY KEY, nombre TEXT, "
                + "especialidad_id TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS especialidades");
        db.execSQL("DROP TABLE IF EXISTS medicos");
        onCreate(db);
    }

    // ---------- ESPECIALIDADES ----------
    public void guardarEspecialidades(List<String> ids, List<String> nombres) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("especialidades", null, null);
            for (int i = 0; i < ids.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("id", ids.get(i));
                v.put("nombre", nombres.get(i));
                db.insert("especialidades", null, v);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void leerEspecialidades(List<String> ids, List<String> nombres) {
        ids.clear();
        nombres.clear();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, nombre FROM especialidades", null);
        while (c.moveToNext()) {
            ids.add(c.getString(0));
            nombres.add(c.getString(1));
        }
        c.close();
    }

    // ---------- MEDICOS ----------
    public void guardarMedicos(String especialidadId, List<String> ids, List<String> nombres) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("medicos", "especialidad_id = ?", new String[]{especialidadId});
            for (int i = 0; i < ids.size(); i++) {
                ContentValues v = new ContentValues();
                v.put("id", ids.get(i));
                v.put("nombre", nombres.get(i));
                v.put("especialidad_id", especialidadId);
                db.insertWithOnConflict("medicos", null, v,
                        SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void leerMedicos(String especialidadId, List<String> ids, List<String> nombres) {
        ids.clear();
        nombres.clear();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, nombre FROM medicos WHERE especialidad_id = ?",
                new String[]{especialidadId});
        while (c.moveToNext()) {
            ids.add(c.getString(0));
            nombres.add(c.getString(1));
        }
        c.close();
    }
}