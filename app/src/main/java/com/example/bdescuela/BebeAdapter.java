package com.example.bdescuela;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BebeAdapter extends RecyclerView.Adapter<BebeAdapter.BebeViewHolder> {

    private List<Bebe> bebeList;
    private OnBebeClickListener listener;
    private Context context;
    private String fechaActual;
    private DatabaseHelper dbHelper;

    public BebeAdapter(List<Bebe> bebeList, OnBebeClickListener listener, Context context, String fechaActual) {
        this.bebeList = bebeList;
        this.listener = listener;
        this.context = context;
        this.fechaActual = fechaActual;
        this.dbHelper = new DatabaseHelper(context);
    }
    public void setFechaActual(String fechaActual){
        this.fechaActual = fechaActual;
    }
    @NonNull
    @Override
    public BebeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bebe, parent, false);
        return new BebeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BebeViewHolder holder, int position) {
        Bebe bebe = bebeList.get(position);
        holder.textViewNombre.setText(bebe.getNombre() + " " + bebe.getApellido());
        if(bebe.getImagen() != null){
            holder.imageViewAvatar.setImageBitmap(BitmapFactory.decodeByteArray(bebe.getImagen(), 0, bebe.getImagen().length));
        }else{
            holder.imageViewAvatar.setImageResource(R.drawable.baseline_person_24); // Imagen por defecto
        }
        holder.imageViewAvatar.setOnClickListener(v -> listener.onAvatarClick(position));
        holder.checkBoxAsistencia.setChecked(bebe.isAsistiendo());
        holder.checkBoxAsistencia.setOnCheckedChangeListener((buttonView, isChecked) -> bebe.setAsistiendo(isChecked));
        holder.btnDetalles.setOnClickListener(v -> {
            Intent intent = new Intent(context, TutorDetailActivity.class);
            intent.putExtra("bebe_id", bebe.getId());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            showEditOrDeleteDialog(position);
            return true;
        });
        holder.imageViewAlerta.setOnClickListener(v -> {
            int bebeId = bebe.getId();
            String fechaMenu = fechaActual;

            // Obtener la lista de intolerancias en común
            List<String> intoleranciasEnComun = dbHelper.getIntoleranciasEnComun(bebeId, fechaMenu);

            if (intoleranciasEnComun.isEmpty()) {
                Toast.makeText(context, context.getString(R.string.no_intolerancias), Toast.LENGTH_SHORT).show();
            } else {
                StringBuilder intoleranciasStr = new StringBuilder("Intolerancias en común: ");
                for (String intolerancia : intoleranciasEnComun) {
                    intoleranciasStr.append("\n").append(intolerancia);
                }
                Toast.makeText(context, intoleranciasStr.toString(), Toast.LENGTH_LONG).show();
            }
        });

        // Verificar si el bebé tiene una intolerancia que está en el menú del día
        Log.d("BebeAdapter", "Verificando intolerancia para bebé: " + bebe.getId() + " en la fecha: " + fechaActual);
        boolean tieneIntoleranciaEnMenu = dbHelper.tieneIntoleranciaEnMenu(fechaActual, bebe.getId());
        Log.d("BebeAdapter", "Tiene intolerancia: " + tieneIntoleranciaEnMenu);
        holder.imageViewAlerta.setVisibility(tieneIntoleranciaEnMenu ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return bebeList.size();
    }

    private void showEditOrDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Selecciona una opción")
                .setItems(new String[]{"Editar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        showEditDialog(position);
                    } else if (which == 1) {
                        showDeleteConfirmationDialog(position);
                    }
                })
                .show();
    }

    private void showEditDialog(int position) {
        Bebe bebe = bebeList.get(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_bebe, null);
        EditText editTextNombre = dialogView.findViewById(R.id.editTextNombre);
        EditText editTextApellido = dialogView.findViewById(R.id.editTextApellido);
        Spinner spinnerAulas = dialogView.findViewById(R.id.spinnerAulas);
        LinearLayout checkboxContainer = dialogView.findViewById(R.id.checkboxContainer);

        editTextNombre.setText(bebe.getNombre());
        editTextApellido.setText(bebe.getApellido());
        List<String> aulaList = getAulasFromDatabase();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, aulaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAulas.setAdapter(adapter);
        int aulaPosition = aulaList.indexOf(bebe.getAula());
        if (aulaPosition >= 0) {
            spinnerAulas.setSelection(aulaPosition);
        }

        // Se ponen todas las intolerancias de la bd en checkbox, con true o false según la bd
        List<String> intoleranciasComunes = dbHelper.obtenerIntoleranciasComunes();

        for (String intolerancia : intoleranciasComunes) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(intolerancia);
            checkboxContainer.addView(checkBox);

            if (dbHelper.bebeTieneIntolerancia(bebe.getId(), intolerancia)) {
                checkBox.setChecked(true);
            }
        }

        new AlertDialog.Builder(context)
                .setTitle("Editar Bebé")
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String nuevoNombre = editTextNombre.getText().toString();
                    String nuevoApellido = editTextApellido.getText().toString();
                    String nuevoAula = spinnerAulas.getSelectedItem().toString();

                    bebe.setNombre(nuevoNombre);
                    bebe.setApellido(nuevoApellido);
                    bebe.setAula(nuevoAula);
                    dbHelper.actualizarBebe(bebe);

                    // Se actualizan las intolerancias
                    dbHelper.eliminarIntoleranciasDelBebe(bebe.getId());

                    for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                        View view = checkboxContainer.getChildAt(i);
                        if (view instanceof CheckBox) {
                            CheckBox checkBox = (CheckBox) view;
                            if (checkBox.isChecked()) {
                                String descripcionIntolerancia = checkBox.getText().toString();
                                int intoleranciaId = dbHelper.obtenerIntoleranciaComunId(descripcionIntolerancia);
                                dbHelper.insertarIntolerancia(bebe.getId(), intoleranciaId);
                            }
                        }
                    }

                    notifyItemChanged(position);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Bebé")
                .setMessage("¿Estás seguro de que deseas eliminar este bebé?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    try {
                        Bebe bebe = bebeList.get(position);
                        dbHelper.deleteBebe(bebe.getId());
                        bebeList.remove(position);
                        notifyItemRemoved(position);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public static class BebeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre;
        ImageView imageViewAvatar;
        CheckBox checkBoxAsistencia;
        Button btnDetalles;
        ImageView imageViewAlerta;

        public BebeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
            checkBoxAsistencia = itemView.findViewById(R.id.checkBoxAsistencia);
            btnDetalles = itemView.findViewById(R.id.btnDetalles);
            imageViewAlerta = itemView.findViewById(R.id.imageViewAlerta);
        }
    }

    private List<String> getAulasFromDatabase() {
        List<String> aulas = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("aula", new String[]{"nombre"}, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                aulas.add(nombre);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return aulas;
    }

}
