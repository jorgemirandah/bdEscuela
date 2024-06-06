package com.example.bdescuela;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SeleccionarAulaActivity extends AppCompatActivity {

    private Button buttonAgregarAula;
    private RecyclerView recyclerViewAulas;
    private AulaAdapter aulaAdapter;
    private List<Aula> aulaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar_aula);

        buttonAgregarAula = findViewById(R.id.buttonAgregarAula);
        recyclerViewAulas = findViewById(R.id.recyclerViewAulas);

        aulaList = new ArrayList<>();
        aulaAdapter = new AulaAdapter(aulaList, this);
        recyclerViewAulas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAulas.setAdapter(aulaAdapter);

        buttonAgregarAula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAgregarAulaDialog();
            }
        });

        // Cargar aulas desde la base de datos al iniciar la pantalla
        actualizarListaAulas();
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
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                @SuppressLint("Range") int capacidad = cursor.getInt(cursor.getColumnIndex("capacidad"));
                @SuppressLint("Range") int color = cursor.getInt(cursor.getColumnIndex("color"));
                aulaList.add(new Aula(nombre, capacidad, color));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        aulaAdapter.notifyDataSetChanged();
    }
}
