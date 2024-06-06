package com.example.bdescuela;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "escuela_infantil.db";
    private static final int DATABASE_VERSION = 1;

    // Sentencias para crear las tablas
    private static final String CREATE_TABLE_BEBE = "CREATE TABLE bebe (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre TEXT NOT NULL," +
            "apellido TEXT NOT NULL," +
            "aula TEXT NOT NULL," +
            "imagen BLOB" +
            ");";

    private static final String CREATE_TABLE_ASISTENCIA = "CREATE TABLE asistencia (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bebe_id INTEGER NOT NULL," +
            "fecha DATE NOT NULL," +
            "hora_entrada TIME NOT NULL," +
            "hora_salida TIME," +
            "FOREIGN KEY (bebe_id) REFERENCES bebe(id)" +
            ");";

    private static final String CREATE_TABLE_TUTOR = "CREATE TABLE tutor (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bebe_id INTEGER NOT NULL," +
            "nombre TEXT NOT NULL," +
            "apellido TEXT NOT NULL," +
            "telefono TEXT NOT NULL," +
            "email TEXT," +
            "direccion TEXT," +
            "FOREIGN KEY (bebe_id) REFERENCES bebe(id)" +
            ");";

    private static final String CREATE_TABLE_AULA = "CREATE TABLE aula (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre TEXT NOT NULL," +
            "capacidad INTEGER NOT NULL," +
            "color INTEGER NOT NULL" +
            ");";

    private static final String CREATE_TABLE_MENU = "CREATE TABLE menu (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "fecha DATE NOT NULL," +
            "descripcion TEXT NOT NULL" +
            ");";

    private static final String CREATE_TABLE_INTOLERANCIA = "CREATE TABLE intolerancia (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bebe_id INTEGER NOT NULL," +
            "descripcion TEXT NOT NULL," +
            "FOREIGN KEY (bebe_id) REFERENCES bebe(id)" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BEBE);
        db.execSQL(CREATE_TABLE_ASISTENCIA);
        db.execSQL(CREATE_TABLE_TUTOR);
        db.execSQL(CREATE_TABLE_AULA);
        db.execSQL(CREATE_TABLE_MENU);
        db.execSQL(CREATE_TABLE_INTOLERANCIA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bebe");
        db.execSQL("DROP TABLE IF EXISTS asistencia");
        db.execSQL("DROP TABLE IF EXISTS tutor");
        db.execSQL("DROP TABLE IF EXISTS aula");
        db.execSQL("DROP TABLE IF EXISTS menu");
        db.execSQL("DROP TABLE IF EXISTS intolerancia");
        onCreate(db);
    }
}
