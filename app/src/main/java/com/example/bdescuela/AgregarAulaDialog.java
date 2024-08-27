package com.example.bdescuela;

import android.app.Dialog;
import android.content.ContentValues;
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
    private int aulaColor = 0xFFFFFF; // Default white color
    private boolean isEditing = false;
    private int position = -1;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_agregar_aula, null);

        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextCapacidad = view.findViewById(R.id.editTextCapacidad);
        textViewColor = view.findViewById(R.id.textViewColor);
        Button buttonAgregar = view.findViewById(R.id.buttonAgregar);
        Button buttonSeleccionarColor = view.findViewById(R.id.buttonSeleccionarColor);

        builder.setView(view)
                .setTitle(R.string.agregar_aula)
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                });

        buttonSeleccionarColor.setOnClickListener(v -> openColorPickerDialog());

        buttonAgregar.setOnClickListener(v -> {
            String nombre = editTextNombre.getText().toString();
            String capacidadStr = editTextCapacidad.getText().toString();

            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), R.string.nombre_obligatorio, Toast.LENGTH_SHORT).show();
                return;
            }
            int capacidad = 0;
            try {
                capacidad = Integer.parseInt(capacidadStr);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isEditing) {
                editarAula(nombre, capacidad, aulaColor, position);
            } else {
                agregarAula(nombre, capacidad, aulaColor);
            }
            dismiss();
        });

        // Check if this dialog was opened for editing an existing aula
        Bundle args = getArguments();
        if (args != null) {
            editTextNombre.setText(args.getString("nombre"));
            editTextCapacidad.setText(String.valueOf(args.getInt("capacidad")));
            aulaColor = args.getInt("color");
            textViewColor.setBackgroundColor(aulaColor);
            isEditing = true;
            position = args.getInt("position");
            builder.setTitle(R.string.editar_aula);
        }

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
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int siguienteId = dbHelper.obtenerSiguienteIdAula();

        ContentValues values = new ContentValues();
        values.put("id", siguienteId);
        values.put("nombre", nombre);
        values.put("capacidad", capacidad);
        values.put("color", color);

        db.insert("aula", null, values);

        SeleccionarAulaActivity activity = (SeleccionarAulaActivity) getActivity();
        if (activity != null) {
            activity.actualizarListaAulas();
        }
    }

    private void editarAula(String nombre, int capacidad, int color, int position) {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("capacidad", capacidad);
        values.put("color", color);

        AulaAdapter adapter = ((SeleccionarAulaActivity) getActivity()).getAulaAdapter();
        Aula aula = adapter.aulaList.get(position);
        db.update("aula", values, "id = ?", new String[]{String.valueOf(aula.getId())});

        aula.setNombre(nombre);
        aula.setCapacidad(capacidad);
        aula.setColor(color);
        adapter.notifyItemChanged(position);
    }
}
