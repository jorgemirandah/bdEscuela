package com.example.bdescuela;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnBebeClickListener {
    private static final int REQUEST_CODE_PICK_FILE = 1;
    private TextView textViewFecha;
    private BebeAdapter bebeAdapter;
    private List<Bebe> bebeList;
    private Calendar calendar;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private int selectedBebePosition;
    private DatabaseHelper databaseHelper;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);
        textViewFecha = findViewById(R.id.textViewFecha);
        RecyclerView recyclerViewBebes = findViewById(R.id.recyclerViewBebes);
        Button buttonSeleccionarAula = findViewById(R.id.buttonSeleccionarAula);
        Button buttonInsertarBebe = findViewById(R.id.buttonInsertarBebe);
        Button btnGuardarAsistencia = findViewById(R.id.btnGuardarAsistencia);
        Button btnAgregarMenu = findViewById(R.id.btnAgregarMenu);
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            importNinosFromCSV(uri);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        requestPermissions();

        Button btnImportar = findViewById(R.id.btnImportar);
        btnImportar.setOnClickListener(v -> filePickerLauncher.launch("text/csv"));

        calendar = Calendar.getInstance();
        bebeList = new ArrayList<>();
        bebeAdapter = new BebeAdapter(bebeList, this, this, textViewFecha.getText().toString());
        recyclerViewBebes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBebes.setAdapter(bebeAdapter);

        updateDateLabel();

        textViewFecha.setOnClickListener(v ->  showDatePickerDialog());

        buttonSeleccionarAula.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SeleccionarAulaActivity.class);
            startActivity(intent);
        });

        buttonInsertarBebe.setOnClickListener(v -> showInsertarBebeDialog());

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                            assert imageBitmap != null;
                            byte[] imageByteArray = bitmapToByteArray(imageBitmap);

                            // Guardar la imagen en la base de datos
                            Bebe selectedBebe = bebeList.get(selectedBebePosition);
                            databaseHelper.updateBebeImage(selectedBebe.getId(), imageByteArray);

                            selectedBebe.setImagen(imageByteArray);
                            bebeAdapter.notifyItemChanged(selectedBebePosition);
                        }
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permiso concedido, lanzar la cámara
                        launchCamera();
                    } else {
                        // Permiso denegado
                    }
                }
        );
        btnGuardarAsistencia.setOnClickListener(v -> guardarAsistencia());
        btnAgregarMenu.setOnClickListener(v -> agregarMenu());
    }
    private String getPathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
            }
            cursor.close();
        }
        return filePath;
    }
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO}, PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void importNinosFromCSV(Uri uri) throws IOException {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        String sqlBebe = "INSERT INTO bebe (nombre, apellido, aula) VALUES (?, ?, ?)";
        String sqlTutorPadre = "INSERT INTO tutor (bebe_id, nombre, movil, telefono, email) VALUES (?, ?, ?, ?, ?)";
        String sqlTutorMadre = "INSERT INTO tutor (bebe_id, nombre, movil, telefono, email) VALUES (?, ?, ?, ?, ?)";
        String sqlAula = "INSERT INTO aula (nombre) VALUES (?)";

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            br.readLine(); // Skip header row
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                // Ensure the values array has exactly 11 elements
                values = ensureCorrectColumnLength(values, 11);

                if (values.length == 11) {
                    // Check if aula exists
                    Cursor cursor = db.rawQuery("SELECT id FROM aula WHERE nombre = ?", new String[]{values[3]});
                    long aulaId;
                    if (cursor.moveToFirst()) {
                        aulaId = cursor.getLong(0);
                    } else {
                        db.execSQL(sqlAula, new String[]{values[3]});
                        aulaId = db.compileStatement("SELECT last_insert_rowid()").simpleQueryForLong();
                    }
                    cursor.close();

                    // Insert bebe
                    db.execSQL(sqlBebe, new Object[]{values[1], values[2], aulaId});
                    long bebeId = db.compileStatement("SELECT last_insert_rowid()").simpleQueryForLong();

                    // Insert tutor padre
                    db.execSQL(sqlTutorPadre, new Object[]{bebeId, values[4], values[5], values[6], values[10]});

                    // Insert tutor madre
                    db.execSQL(sqlTutorMadre, new Object[]{bebeId, values[7], values[8], values[9], values[10]});
                } else {
                    // Handle the case where the number of columns is not as expected
                    Log.w("CSVImport", "Fila con columnas insuficientes: " + Arrays.toString(values));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private String[] ensureCorrectColumnLength(String[] values, int expectedLength) {
        if (values.length < expectedLength) {
            String[] newValues = new String[expectedLength];
            System.arraycopy(values, 0, newValues, 0, values.length);
            for (int i = values.length; i < expectedLength; i++) {
                newValues[i] = "";
            }
            return newValues;
        } else if (values.length > expectedLength) {
            return Arrays.copyOfRange(values, 0, expectedLength);
        } else {
            return values;
        }
    }


    private void agregarMenu() {
        String fecha = textViewFecha.getText().toString();

        Intent intent = new Intent(this, AgregarMenuActivity.class);
        intent.putExtra("fecha", fecha);

        startActivity(intent);

    }

    private void guardarAsistencia() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String fechaAsistencia = textViewFecha.getText().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horaEntrada = dateFormat.format(new Date());

        for (Bebe bebe : bebeList) {
            if (bebe.isAsistiendo()) {
                ContentValues values = new ContentValues();
                values.put("bebe_id", bebe.getId());
                values.put("fecha", fechaAsistencia);
                values.put("hora_entrada", horaEntrada);

                db.insert("asistencia", null, values);
            }
        }

        Toast.makeText(this, R.string.asistencia_guardada, Toast.LENGTH_SHORT).show();
        db.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Actualiza la lista de bebés cada vez que se vuelve a la actividad MainActivity
        actualizarListaBebes();
        actualizarAsistenciaPorFecha(textViewFecha.getText().toString());
    }

    @Override
    public void onAvatarClick(int position) {
        selectedBebePosition = position;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }

    //Actualiza la fecha y llama para actualizar asistencia
    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String fecha = sdf.format(calendar.getTime());
        textViewFecha.setText(fecha);
        actualizarListaBebes();
        actualizarAsistenciaPorFecha(fecha);

        if (bebeAdapter != null) {
            bebeAdapter.setFechaActual(fecha);
            bebeAdapter.notifyDataSetChanged();
        }

    }
    private void actualizarAsistenciaPorFecha(String fecha) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Integer> bebesAsistentes = dbHelper.obtenerAsistenciaPorFecha(fecha);
        for (Bebe bebe : bebeList) {
            bebe.setAsistiendo(bebesAsistentes.contains(bebe.getId()));
        }
        bebeAdapter.notifyDataSetChanged();
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showInsertarBebeDialog() {
        InsertarBebeDialog dialog = new InsertarBebeDialog();
        dialog.show(getSupportFragmentManager(), "InsertarBebeDialog");
    }

    public void actualizarListaBebes() {
        if (bebeList != null) {
            bebeList.clear();
        }

        String nombreAula = obtenerNombreAula();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL para obtener los bebés
        String[] projection = {"id", "nombre", "apellido", "aula", "imagen"};
        String selection = null;
        String[] selectionArgs = null;

        // Si nombreAula es null se seleccionan de todas las aulas, si no del aula seleccionada
        if (nombreAula != null) {
            selection = "aula = ?";
            selectionArgs = new String[]{nombreAula};
        }

        Cursor cursor = db.query("bebe", projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                @SuppressLint("Range") String apellido = cursor.getString(cursor.getColumnIndex("apellido"));
                @SuppressLint("Range") String aula = cursor.getString(cursor.getColumnIndex("aula"));
                @SuppressLint("Range") byte[] imagen = cursor.getBlob(cursor.getColumnIndex("imagen"));
                bebeList.add(new Bebe(id, nombre, apellido, aula, imagen));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        bebeAdapter.notifyDataSetChanged();
    }

    private String obtenerNombreAula() {
        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("nombreAula", null);
    }
}
