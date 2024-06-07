package com.example.bdescuela;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TutorPagerAdapter extends RecyclerView.Adapter<TutorPagerAdapter.TutorViewHolder> {
    private List<Tutor> tutorList;
    private LayoutInflater inflater;

    public TutorPagerAdapter(Context context, List<Tutor> tutorList) {
        this.inflater = LayoutInflater.from(context);
        this.tutorList = tutorList;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_tutor, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        Tutor tutor = tutorList.get(position);
        holder.editTextNombre.setText(tutor.getNombre());
        holder.editTextApellido.setText(tutor.getApellido());
        holder.editTextTelefono.setText(tutor.getTelefono());
        holder.editTextEmail.setText(tutor.getEmail());
        holder.editTextDireccion.setText(tutor.getDireccion());
        holder.itemView.setTag("view" + position);
    }

    @Override
    public int getItemCount() {
        return tutorList.size();
    }

    class TutorViewHolder extends RecyclerView.ViewHolder {
        EditText editTextNombre;
        EditText editTextApellido;
        EditText editTextTelefono;
        EditText editTextEmail;
        EditText editTextDireccion;

        public TutorViewHolder(View itemView) {
            super(itemView);
            editTextNombre = itemView.findViewById(R.id.editTextNombre);
            editTextApellido = itemView.findViewById(R.id.editTextApellido);
            editTextTelefono = itemView.findViewById(R.id.editTextTelefono);
            editTextEmail = itemView.findViewById(R.id.editTextEmail);
            editTextDireccion = itemView.findViewById(R.id.editTextDireccion);
        }
    }
}
