package com.example.bdescuela;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AulaAdapter extends RecyclerView.Adapter<AulaAdapter.AulaViewHolder> {

    final List<Aula> aulaList;
    private final Context context;
    private final DatabaseHelper dbHelper;

    public AulaAdapter(List<Aula> aulaList, Context context) {
        this.aulaList = aulaList;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public AulaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_aula, parent, false);
        return new AulaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AulaViewHolder holder, int position) {
        Aula aula = aulaList.get(position);
        holder.textViewNombre.setText(aula.getNombre());
        holder.textViewCapacidad.setText(String.valueOf(aula.getCapacidad()));
        holder.itemView.setBackgroundColor(aula.getColor());

        holder.itemView.setOnClickListener(v -> guardarNombreAulaEnSharedPreferences(aula.getNombre()));

        holder.itemView.setOnLongClickListener(v -> {
            mostrarDialogoEditarEliminar(position, aula.getNombre());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return aulaList.size();
    }

    public static class AulaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre, textViewCapacidad;

        public AulaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewCapacidad = itemView.findViewById(R.id.textViewCapacidad);
        }
    }

    private void guardarNombreAulaEnSharedPreferences(String nombreAula) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nombreAula", nombreAula);
        editor.apply();
        Toast.makeText(context, context.getString(R.string.aula_seleccionada) + nombreAula, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void mostrarDialogoEditarEliminar(int position, String nombreAula) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.editar_eliminar) + " " + nombreAula)
                .setPositiveButton(R.string.editar, (dialog, which) -> editarAula(position))
                .setNegativeButton(R.string.eliminar, (dialog, which) -> eliminarAula(position))
                .show();
    }

    private void eliminarAula(int position) {
        Aula aula = aulaList.get(position);
        String nombreAula = aula.getNombre();

        dbHelper.eliminarBebesPorAula(nombreAula);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("aula", "nombre = ?", new String[]{nombreAula});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        db.close();

        aulaList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, aulaList.size());
    }

    private void editarAula(int position) {
        Aula aula = aulaList.get(position);

        // Launch the AgregarAulaDialog with pre-filled data
        AgregarAulaDialog dialog = new AgregarAulaDialog();
        Bundle args = new Bundle();
        args.putString("nombre", aula.getNombre());
        args.putInt("capacidad", aula.getCapacidad());
        args.putInt("color", aula.getColor());
        args.putInt("position", position);
        dialog.setArguments(args);
        dialog.show(((SeleccionarAulaActivity) context).getSupportFragmentManager(), "AgregarAulaDialog");
    }
}
