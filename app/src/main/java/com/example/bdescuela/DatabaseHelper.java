package com.example.bdescuela;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;

    private static final String DATABASE_NAME = "escuela_infantil.db";
    private static final int DATABASE_VERSION = 11;
    private static final String TABLE_BEBE = "bebe";
    private static final String TABLE_ASISTENCIA = "asistencia";
    private static final String TABLE_TUTOR = "tutor";
    private static final String TABLE_AULA = "aula";
    private static final String TABLE_MENU = "menu";
    private static final String TABLE_INTOLERANCIA = "intolerancia";
    private static final String TABLE_INTOLERANCIA_COMUN = "intolerancia_comun";
    private static final String TABLE_MENU_INTOLERANCIA_COMUN = "menu_intolerancia_comun";

    private static final String CREATE_TABLE_BEBE = "CREATE TABLE " + TABLE_BEBE + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre TEXT NOT NULL," +
            "apellido TEXT NOT NULL," +
            "aula TEXT NOT NULL," +
            "imagen BLOB" +
            ");";

    private static final String CREATE_TABLE_ASISTENCIA = "CREATE TABLE " + TABLE_ASISTENCIA + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bebe_id INTEGER NOT NULL," +
            "fecha DATE NOT NULL," +
            "FOREIGN KEY (bebe_id) REFERENCES bebe(id)" +
            ");";

    private static final String CREATE_TABLE_TUTOR = "CREATE TABLE " + TABLE_TUTOR + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bebe_id INTEGER NOT NULL," +
            "nombre TEXT," +
            "apellido TEXT," +
            "movil TEXT," +
            "telefono TEXT," +
            "email TEXT," +
            "FOREIGN KEY (bebe_id) REFERENCES bebe(id)" +
            ");";

    private static final String CREATE_TABLE_AULA = "CREATE TABLE " + TABLE_AULA + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "nombre TEXT NOT NULL," +
            "capacidad INTEGER," +
            "color INTEGER" +
            ");";

    private static final String CREATE_TABLE_MENU = "CREATE TABLE " + TABLE_MENU + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "fecha DATE NOT NULL," +
            "descripcion TEXT" +
            ");";

    private static final String CREATE_TABLE_MENU_INTOLERANCIA_COMUN = "CREATE TABLE " + TABLE_MENU_INTOLERANCIA_COMUN + " (" +
            "menu_id INTEGER NOT NULL," +
            "intolerancia_comun_id INTEGER NOT NULL," +
            "FOREIGN KEY (menu_id) REFERENCES " + TABLE_MENU + "(id)," +
            "FOREIGN KEY (intolerancia_comun_id) REFERENCES " + TABLE_INTOLERANCIA_COMUN + "(id)" +
            ");";

    private static final String CREATE_TABLE_INTOLERANCIA = "CREATE TABLE " + TABLE_INTOLERANCIA + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "bebe_id INTEGER NOT NULL," +
            "intolerancia_comun_id INTEGER NOT NULL," +
            "FOREIGN KEY (bebe_id) REFERENCES bebe(id)," +
            "FOREIGN KEY (intolerancia_comun_id) REFERENCES " + TABLE_INTOLERANCIA_COMUN + "(id)" +
            ");";

    private static final String CREATE_TABLE_INTOLERANCIA_COMUN = "CREATE TABLE " + TABLE_INTOLERANCIA_COMUN + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "descripcion TEXT NOT NULL" +
            ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BEBE);
        db.execSQL(CREATE_TABLE_ASISTENCIA);
        db.execSQL(CREATE_TABLE_TUTOR);
        db.execSQL(CREATE_TABLE_AULA);
        db.execSQL(CREATE_TABLE_MENU);
        db.execSQL(CREATE_TABLE_INTOLERANCIA);
        db.execSQL(CREATE_TABLE_INTOLERANCIA_COMUN);
        db.execSQL(CREATE_TABLE_MENU_INTOLERANCIA_COMUN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEBE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASISTENCIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TUTOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AULA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTOLERANCIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTOLERANCIA_COMUN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU_INTOLERANCIA_COMUN);
        onCreate(db);
    }

    public void updateBebeImage(int id, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("imagen", image);
        db.update(TABLE_BEBE, values, "id = ?", new String[]{String.valueOf(id)});
    }
    public void deleteBebe(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("bebe", "id = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void eliminarBebesPorAula(String nombreAula) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("bebe", "aula = ?", new String[]{nombreAula});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();
    }


    public int obtenerSiguienteIdAula(){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT MAX(id) AS max_id FROM aula";
        Cursor cursor = db.rawQuery(query, null);
        int siguienteId = 1;

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") int maxId = cursor.getInt(cursor.getColumnIndex("max_id"));
            siguienteId = maxId + 1;
        }
        cursor.close();
        return siguienteId;
    }

    public List<Integer> obtenerAsistenciaPorFecha(String fecha) {
        List<Integer> bebesAsistentes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT bebe_id FROM asistencia WHERE fecha = ?";
        Cursor cursor = db.rawQuery(query, new String[]{fecha});

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int bebeId = cursor.getInt(cursor.getColumnIndex("bebe_id"));
                bebesAsistentes.add(bebeId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bebesAsistentes;
    }
    @SuppressLint("Range")
    public List<Tutor> obtenerTutoresPorBebeId(int bebeId) {
        List<Tutor> tutorList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tutor WHERE bebe_id = ?", new String[]{String.valueOf(bebeId)});
        if (cursor.moveToFirst()) {
            do {
                Tutor tutor = new Tutor(
                        cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getInt(cursor.getColumnIndex("bebe_id")),
                        cursor.getString(cursor.getColumnIndex("nombre")),
                        cursor.getString(cursor.getColumnIndex("apellido")),
                        cursor.getString(cursor.getColumnIndex("telefono")),
                        cursor.getString(cursor.getColumnIndex("email")),
                        cursor.getString(cursor.getColumnIndex("movil"))
                );
                tutorList.add(tutor);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return tutorList;
    }


    public void guardarTutor(Tutor tutor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("bebe_id", tutor.getBebeId());
        values.put("nombre", tutor.getNombre());
        values.put("apellido", tutor.getApellido());
        values.put("telefono", tutor.getTelefono());
        values.put("email", tutor.getEmail());
        values.put("movil", tutor.getMovil());

        if (tutor.getId() == 0) {
            long id = db.insert("tutor", null, values);
            tutor.setId((int) id);
        } else {
            db.update("tutor", values, "id = ?", new String[]{String.valueOf(tutor.getId())});
        }
    }
    public void eliminarTutor(int tutorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tutor", "id = ?", new String[]{String.valueOf(tutorId)});
    }
    @SuppressLint("Range")
    public List<String> obtenerIntoleranciasComunes() {
        List<String> intolerancias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT descripcion FROM " + TABLE_INTOLERANCIA_COMUN, null);

        if (cursor.moveToFirst()) {
            do {
                intolerancias.add(cursor.getString(cursor.getColumnIndex("descripcion")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return intolerancias;
    }
    @SuppressLint("Range")
    public int obtenerIntoleranciaComunId(String descripcion) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_INTOLERANCIA_COMUN + " WHERE descripcion = ?", new String[]{descripcion});
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return id;
    }


    public long guardarMenu(String descripcion, String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("descripcion", descripcion);
        values.put("fecha", fecha);
        return db.insert(TABLE_MENU, null, values);
    }
    public void insertarIntoleranciaComun(String descripcion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("descripcion", descripcion);
        db.insert(TABLE_INTOLERANCIA_COMUN, null, values);
    }
    @SuppressLint("Range")
    public String obtenerMenuPorFecha(String fecha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT descripcion FROM " + TABLE_MENU + " WHERE fecha = ?", new String[]{fecha});
        String menu = null;
        if (cursor.moveToFirst()) {
            menu = cursor.getString(cursor.getColumnIndex("descripcion"));
        }
        cursor.close();
        return menu;
    }
    @SuppressLint("Range")
    public List<Integer> obtenerIntoleranciasPorMenuId(long menuId) {
        List<Integer> intolerancias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT intolerancia_comun_id FROM menu_intolerancia_comun WHERE menu_id = ?", new String[]{String.valueOf(menuId)});

        if (cursor.moveToFirst()) {
            do {
                intolerancias.add(cursor.getInt(cursor.getColumnIndex("intolerancia_comun_id")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return intolerancias;
    }
    @SuppressLint("Range")
    public long obtenerMenuIdPorFecha(String fecha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + TABLE_MENU + " WHERE fecha = ?", new String[]{fecha});
        long menuId = -1;
        if (cursor.moveToFirst()) {
            menuId = cursor.getLong(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return menuId;
    }

    @SuppressLint("Range")
    public String obtenerIntoleranciaComunDescripcionPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT descripcion FROM " + TABLE_INTOLERANCIA_COMUN + " WHERE id = ?", new String[]{String.valueOf(id)});
        String descripcion = null;
        if (cursor.moveToFirst()) {
            descripcion = cursor.getString(cursor.getColumnIndex("descripcion"));
        }
        cursor.close();
        return descripcion;
    }
    public void eliminarMenuPorFecha(String fecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MENU + " WHERE fecha = ?", new Object[]{fecha});
    }

    public void guardarMenuIntoleranciaComun(long menuId, int intoleranciaComunId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("menu_id", menuId);
        values.put("intolerancia_comun_id", intoleranciaComunId);
        db.insert("menu_intolerancia_comun", null, values);
    }

    public boolean tieneIntoleranciaEnMenu(String fecha, int bebeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM menu_intolerancia_comun mic " +
                "JOIN menu m ON mic.menu_id = m.id " +
                "JOIN intolerancia_comun ic ON mic.intolerancia_comun_id = ic.id " +
                "JOIN intolerancia i ON ic.id = i.intolerancia_comun_id " +
                "WHERE m.fecha = ? AND i.bebe_id = ?";

        Cursor cursor = db.rawQuery(query, new String[]{fecha, String.valueOf(bebeId)});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        return count > 0;
    }
    @SuppressLint("Range")
    public List<String> getIntoleranciasEnComun(int bebeId, String fechaMenu) {
        List<String> intoleranciasEnComun = new ArrayList<>();

        String query = "SELECT ic.descripcion " +
                "FROM intolerancia_comun ic " +
                "JOIN intolerancia i ON ic.id = i.intolerancia_comun_id " +
                "JOIN menu_intolerancia_comun mic ON ic.id = mic.intolerancia_comun_id " +
                "JOIN menu m ON mic.menu_id = m.id " +
                "WHERE i.bebe_id = ? AND m.fecha = ?";

        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(bebeId), fechaMenu})) {
            while (cursor.moveToNext()) {
                intoleranciasEnComun.add(cursor.getString(cursor.getColumnIndex("descripcion")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return intoleranciasEnComun;
    }

    public boolean bebeTieneIntolerancia(int bebeId, String descripcionIntolerancia) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_INTOLERANCIA + " i " +
                "JOIN " + TABLE_INTOLERANCIA_COMUN + " ic ON i.intolerancia_comun_id = ic.id " +
                "WHERE i.bebe_id = ? AND ic.descripcion = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(bebeId), descripcionIntolerancia});
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    public void eliminarIntoleranciasDelBebe(int bebeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INTOLERANCIA, "bebe_id = ?", new String[]{String.valueOf(bebeId)});
    }

    public void insertarIntolerancia(int bebeId, int intoleranciaComunId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("bebe_id", bebeId);
        values.put("intolerancia_comun_id", intoleranciaComunId);
        db.insert(TABLE_INTOLERANCIA, null, values);
    }
    public void eliminarIntoleranciaComun(String descripcion) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INTOLERANCIA_COMUN, "descripcion = ?", new String[]{descripcion});
    }

    public void actualizarBebe(Bebe bebe) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", bebe.getNombre());
        values.put("apellido", bebe.getApellido());
        values.put("aula", bebe.getAula());
        db.update(TABLE_BEBE, values, "id = ?", new String[]{String.valueOf(bebe.getId())});
    }

    public void importNinosFromCSV(String csvFilePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sqlBebe = "INSERT INTO bebe (nombre, apellido, aula) VALUES (?, ?, ?)";
        String sqlTutorPadre = "INSERT INTO tutor (bebe_id, nombre, apellido, telefono) VALUES (?, ?, ?, ?)";
        String sqlTutorMadre = "INSERT INTO tutor (bebe_id, nombre, apellido, telefono) VALUES (?, ?, ?, ?)";
        String sqlAula = "INSERT INTO aula (nombre) VALUES (?)";

        Uri contentUri = MediaStore.Files.getContentUri("external");
        String[] projection = {MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME};
        String selection = MediaStore.Files.FileColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[]{"ninos.csv"};

        try (Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                Uri uri = ContentUris.withAppendedId(contentUri, id);

                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    br.readLine(); // Skip header row
                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(",");

                        db.execSQL(sqlBebe, new String[]{values[1], values[2], values[3]});

                        long bebeId = db.compileStatement("SELECT last_insert_rowid()").simpleQueryForLong();

                        db.execSQL(sqlTutorPadre, new Object[]{bebeId, values[4], values[5], values[6]});
                        db.execSQL(sqlTutorMadre, new Object[]{bebeId, values[8], values[9], values[10]});

                        if (values[3] != null && !values[3].isEmpty()) {
                            db.execSQL(sqlAula, new String[]{values[3]});
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

}
