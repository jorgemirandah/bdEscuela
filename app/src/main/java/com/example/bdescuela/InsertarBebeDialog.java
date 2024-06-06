package com.example.bdescuela;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class InsertarBebeDialog extends AppCompatDialogFragment {

    private EditText editTextNombre, editTextApellido;
    private Spinner spinnerAulas;
    private Button buttonInsertar;
    private List<String> aulaList;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_insertar_bebe, null);

        editTextNombre = view.findViewById(R.id.editTextNombre);
        editTextApellido = view.findViewById(R.id.editTextApellido);
        spinnerAulas = view.findViewById(R.id.spinnerAulas);
        buttonInsertar = view.findViewById(R.id.buttonInsertar);

        aulaList = getAulasFromDatabase();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, aulaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAulas.setAdapter(adapter);

        builder.setView(view)
                .setTitle("Insertar Bebé")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acción al cancelar
                    }
                });

        buttonInsertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = editTextNombre.getText().toString();
                String apellido = editTextApellido.getText().toString();
                String aula = spinnerAulas.getSelectedItem().toString();
                insertarBebe(nombre, apellido, aula);
                dismiss();
            }
        });

        return builder.create();
    }

    private List<String> getAulasFromDatabase() {
        List<String> aulas = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("aula", new String[]{"nombre"}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                aulas.add(nombre);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return aulas;
    }

    private void insertarBebe(String nombre, String apellido, String aula) {
        // Insertar bebé en la base de datos
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("apellido", apellido);
        values.put("aula", aula);
        db.insert("bebe", null, values);
        db.close();

        // Actualizar la lista de bebés en la actividad principal
        MainActivity activity = (MainActivity) getActivity();
        activity.actualizarListaBebes();
    }
}
