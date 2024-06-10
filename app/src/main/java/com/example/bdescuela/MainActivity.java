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
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnBebeClickListener {
    private TextView textViewFecha;
    private RecyclerView recyclerViewBebes;
    private BebeAdapter bebeAdapter;
    private List<Bebe> bebeList;
    private Calendar calendar;
    private Button buttonSeleccionarAula, buttonInsertarBebe, btnGuardarAsistencia, btnAgregarMenu;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private int selectedBebePosition;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);
        textViewFecha = findViewById(R.id.textViewFecha);
        recyclerViewBebes = findViewById(R.id.recyclerViewBebes);
        buttonSeleccionarAula = findViewById(R.id.buttonSeleccionarAula);
        buttonInsertarBebe = findViewById(R.id.buttonInsertarBebe);
        btnGuardarAsistencia = findViewById(R.id.btnGuardarAsistencia);
        btnAgregarMenu = findViewById(R.id.btnAgregarMenu);
        calendar = Calendar.getInstance();
        bebeList = new ArrayList<>();
        bebeAdapter = new BebeAdapter(bebeList, this, this, textViewFecha.getText().toString());
        recyclerViewBebes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBebes.setAdapter(bebeAdapter);

        updateDateLabel();

        Log.d("MainActivity", "Fecha pasada al adaptador: " + textViewFecha.getText().toString());

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

        Toast.makeText(this, "Asistencia guardada", Toast.LENGTH_SHORT).show();
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
        selectedBebePosition = position; // Asegúrate de registrar la posición del bebé seleccionado
        // Verificar si el permiso de la cámara está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido, lanzar la cámara
            launchCamera();
        } else {
            // Solicitar el permiso de la cámara
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
        Log.d("MainActivity", "Fecha actualizada: " + fecha);
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
