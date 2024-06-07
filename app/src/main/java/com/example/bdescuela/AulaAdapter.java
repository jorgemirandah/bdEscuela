package com.example.bdescuela;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AulaAdapter extends RecyclerView.Adapter<AulaAdapter.AulaViewHolder> {

    private List<Aula> aulaList;
    private Context context;
    private DatabaseHelper dbHelper;

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
        holder.textViewCapacidad.setText("" + aula.getCapacidad());
        holder.itemView.setBackgroundColor(aula.getColor());

        holder.itemView.setOnClickListener(v -> guardarNombreAulaEnSharedPreferences(aula.getNombre()));

        holder.itemView.setOnLongClickListener(v -> {
            mostrarDialogoConfirmacion(position);
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

    private void mostrarDialogoConfirmacion(int position) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este aula?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarAula(position))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarAula(int position) {
        Aula aula = aulaList.get(position);
        dbHelper.deleteAula(aula.getId());
        aulaList.remove(position);
        notifyItemRemoved(position);
        Toast.makeText(context, "Aula eliminada: " + aula.getNombre(), Toast.LENGTH_SHORT).show();
    }
}
