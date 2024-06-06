package com.example.bdescuela;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView textViewFecha;
    private RecyclerView recyclerViewBebes;
    private BebeAdapter bebeAdapter;
    private List<Bebe> bebeList;
    private Calendar calendar;
    private Button buttonSeleccionarAula, buttonInsertarBebe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textViewFecha = findViewById(R.id.textViewFecha);
        recyclerViewBebes = findViewById(R.id.recyclerViewBebes);
        buttonSeleccionarAula = findViewById(R.id.buttonSeleccionarAula);
        buttonInsertarBebe = findViewById(R.id.buttonInsertarBebe);
        calendar = Calendar.getInstance();
        updateDateLabel();
        bebeList = new ArrayList<>();
        bebeAdapter = new BebeAdapter(bebeList);
        actualizarListaBebes();

        textViewFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSeleccionarAula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SeleccionarAulaActivity.class);
                startActivity(intent);
            }
        });

        buttonInsertarBebe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInsertarBebeDialog();
            }
        });

        recyclerViewBebes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBebes.setAdapter(bebeAdapter);

    }
    @Override
    protected void onResume() {
        super.onResume();
        // Actualiza la lista de bebés cada vez que se vuelve a la actividad MainActivity
        actualizarListaBebes();
    }

    private void updateDateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        textViewFecha.setText(sdf.format(calendar.getTime()));
    }

    private void showDatePickerDialog() {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel();
            }
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
        if (nombreAula == null) {
            // No se ha seleccionado ningún aula, así que no hay bebés que mostrar
            bebeAdapter.notifyDataSetChanged();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Consulta SQL para obtener los bebés que pertenecen al aula específica
        String[] projection = {"nombre", "apellido", "aula"};
        String selection = "aula = ?";
        String[] selectionArgs = {nombreAula};
        Cursor cursor = db.query("bebe", projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                @SuppressLint("Range") String apellido = cursor.getString(cursor.getColumnIndex("apellido"));
                @SuppressLint("Range") String aula = cursor.getString(cursor.getColumnIndex("aula"));
                bebeList.add(new Bebe(nombre, apellido, aula));
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