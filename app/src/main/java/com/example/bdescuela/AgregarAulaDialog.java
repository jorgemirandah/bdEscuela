package com.example.bdescuela;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AgregarAulaDialog extends AppCompatDialogFragment {

    private EditText editTextNombre, editTextCapacidad;
    private TextView textViewColor;
    private Button buttonAgregar, buttonSeleccionarColor;
    private int aulaColor = 0xFFFFFF; // Uso el color blanco por defecto

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_agregar_aula, null);

        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextCapacidad = view.findViewById(R.id.editTextCapacidad);
        textViewColor = view.findViewById(R.id.textViewColor);
        buttonAgregar = view.findViewById(R.id.buttonAgregar);
        buttonSeleccionarColor = view.findViewById(R.id.buttonSeleccionarColor);

        builder.setView(view)
                .setTitle("Agregar Aula")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acción al cancelar
                    }
                });

        buttonSeleccionarColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPickerDialog();
            }
        });

        buttonAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = editTextNombre.getText().toString();
                String capacidadStr = editTextCapacidad.getText().toString();

                if (nombre.isEmpty() || capacidadStr.isEmpty()) {
                    Toast.makeText(getContext(), "Nombre y capacidad son obligatorios", Toast.LENGTH_SHORT).show();
                    return;
                }

                int capacidad = Integer.parseInt(capacidadStr);
                agregarAula(nombre, capacidad, aulaColor);
                dismiss();
            }
        });

        return builder.create();
    }

    private void openColorPickerDialog() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(getContext(), aulaColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                aulaColor = color;
                textViewColor.setBackgroundColor(aulaColor);
            }
        });
        colorPicker.show();
    }

    private void agregarAula(String nombre, int capacidad, int color) {
        // Insertar aula en la base de datos
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("capacidad", capacidad);
        values.put("color", color);
        db.insert("aula", null, values);
        db.close();

        // Actualizar la lista de aulas en la actividad
        SeleccionarAulaActivity activity = (SeleccionarAulaActivity) getActivity();
        assert activity != null;
        activity.actualizarListaAulas();
    }
}
