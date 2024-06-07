package com.example.bdescuela;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SeleccionarAulaActivity extends AppCompatActivity {

    private Button buttonAgregarAula;
    private Button btnSeleccionarTodo;
    private RecyclerView recyclerViewAulas;
    private AulaAdapter aulaAdapter;
    private List<Aula> aulaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_aula);

        buttonAgregarAula = findViewById(R.id.buttonAgregarAula);
        recyclerViewAulas = findViewById(R.id.recyclerViewAulas);
        btnSeleccionarTodo = findViewById(R.id.btnSeleccionarTodo);
        aulaList = new ArrayList<>();
        aulaAdapter = new AulaAdapter(aulaList, this);
        recyclerViewAulas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAulas.setAdapter(aulaAdapter);

        buttonAgregarAula.setOnClickListener(v -> showAgregarAulaDialog());
        btnSeleccionarTodo.setOnClickListener(v -> cambiarPreferencias());
        actualizarListaAulas();
    }

    private void cambiarPreferencias() {
        SharedPreferences sharedPreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("nombreAula", null);
        editor.apply();

        Intent intent = new Intent(SeleccionarAulaActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void showAgregarAulaDialog() {
        AgregarAulaDialog dialog = new AgregarAulaDialog();
        dialog.show(getSupportFragmentManager(), "AgregarAulaDialog");
    }

    public void actualizarListaAulas() {
        aulaList.clear();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("aula", null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                @SuppressLint("Range") int capacidad = cursor.getInt(cursor.getColumnIndex("capacidad"));
                @SuppressLint("Range") int color = cursor.getInt(cursor.getColumnIndex("color"));
                aulaList.add(new Aula(id, nombre, capacidad, color));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        aulaAdapter.notifyDataSetChanged();
    }
}
