package com.example.bdescuela;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AgregarMenuActivity extends AppCompatActivity {
    private EditText editTextMenu;
    private LinearLayout linearLayoutIntolerancias;

    private DatabaseHelper dbHelper;
    private String fecha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_menu);

        editTextMenu = findViewById(R.id.editTextMenu);
        linearLayoutIntolerancias = findViewById(R.id.linearLayoutIntolerancias);
        Button buttonAgregarIntolerancia = findViewById(R.id.buttonAgregarIntolerancia);
        fecha = getIntent().getStringExtra("fecha");
        dbHelper = new DatabaseHelper(this);

        cargarIntoleranciasComunes();
        cargarMenuSiExiste();

        buttonAgregarIntolerancia.setOnClickListener(v -> agregarIntoleranciaComun());
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarMenu();
    }

    private void cargarIntoleranciasComunes() {
        List<String> intoleranciasComunes = dbHelper.obtenerIntoleranciasComunes();
        if (intoleranciasComunes.isEmpty()) {
            Toast.makeText(this, R.string.no_hay_intolerancia, Toast.LENGTH_LONG).show();
        }
        linearLayoutIntolerancias.removeAllViews();
        for (String intolerancia : intoleranciasComunes) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(intolerancia);
            linearLayout.addView(checkBox);

            Button buttonEliminar = new Button(this);
            buttonEliminar.setText(R.string.eliminar);
            buttonEliminar.setOnClickListener(v -> eliminarIntoleranciaComun(intolerancia));
            linearLayout.addView(buttonEliminar);

            linearLayoutIntolerancias.addView(linearLayout);
        }
    }
    private void eliminarIntoleranciaComun(String descripcion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.eliminar_intolerancia)
                .setMessage(R.string.seguro_eliminar_intolerancia)
                .setPositiveButton(R.string.si, (dialog, which) -> {
                    dbHelper.eliminarIntoleranciaComun(descripcion);
                    cargarIntoleranciasComunes();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void cargarMenuSiExiste() {
        String menu = dbHelper.obtenerMenuPorFecha(fecha);
        if (menu != null) {
            editTextMenu.setText(menu);

            long menuId = dbHelper.obtenerMenuIdPorFecha(fecha);
            List<Integer> intoleranciasIds = dbHelper.obtenerIntoleranciasPorMenuId(menuId);
            for (int intoleranciaId : intoleranciasIds) {
                String descripcion = dbHelper.obtenerIntoleranciaComunDescripcionPorId(intoleranciaId);
                for (int i = 0; i < linearLayoutIntolerancias.getChildCount(); i++) {
                    LinearLayout linearLayout = (LinearLayout) linearLayoutIntolerancias.getChildAt(i);
                    CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
                    if (checkBox.getText().toString().equals(descripcion)) {
                        checkBox.setChecked(true);
                    }
                }
            }
        }
    }

    private void agregarIntoleranciaComun() {
        EditText input = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.agregar_intolerancia);
        builder.setView(input);
        builder.setPositiveButton(R.string.agregar, (dialog, which) -> {
            String intolerancia = input.getText().toString().trim();
            if (!intolerancia.isEmpty()) {
                dbHelper.insertarIntoleranciaComun(intolerancia);
                cargarIntoleranciasComunes();
            }
        });
        builder.setNegativeButton(R.string.cancelar, null);
        builder.show();
    }

    private void guardarMenu() {
        String menu = editTextMenu.getText().toString().trim();
        // Se mira que se haya seleccionado intolerancias o se haya puesto descripción para insertarlo
        boolean hasIntoleranciasSeleccionadas = false;
        for (int i = 0; i < linearLayoutIntolerancias.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) linearLayoutIntolerancias.getChildAt(i);
            CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
            if (checkBox.isChecked()) {
                hasIntoleranciasSeleccionadas = true;
                break;
            }
        }

        if (menu.isEmpty() && !hasIntoleranciasSeleccionadas) {
            // No hacer nada si no se ha ingresado un menú ni se han seleccionado intolerancias
            return;
        }

        // Se elimina el menú si hay uno con la misma fecha para actualizarlo
        dbHelper.eliminarMenuPorFecha(fecha);

        // Guardar el menú
        long menuId = dbHelper.guardarMenu(menu, fecha);

        // Guardar las intolerancias seleccionadas
        for (int i = 0; i < linearLayoutIntolerancias.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) linearLayoutIntolerancias.getChildAt(i);
            CheckBox checkBox = (CheckBox) linearLayout.getChildAt(0);
            if (checkBox.isChecked()) {
                String intolerancia = checkBox.getText().toString();
                int intoleranciaId = dbHelper.obtenerIntoleranciaComunId(intolerancia);
                dbHelper.guardarMenuIntoleranciaComun(menuId, intoleranciaId);
            }
        }
    }
}
