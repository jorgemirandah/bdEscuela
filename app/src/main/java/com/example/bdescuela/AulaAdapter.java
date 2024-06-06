package com.example.bdescuela;

import android.content.Context;
import android.content.SharedPreferences;
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

    public AulaAdapter(List<Aula> aulaList, Context context) {
        this.aulaList = aulaList;
        this.context = context;
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
        holder.textViewCapacidad.setText(""+aula.getCapacidad());
        holder.itemView.setBackgroundColor(aula.getColor());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarNombreAulaEnSharedPreferences(aula.getNombre());
            }
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
        Toast.makeText(context, "Aula seleccionada: " + nombreAula, Toast.LENGTH_SHORT).show();
    }

}
