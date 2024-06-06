package com.example.bdescuela;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BebeAdapter extends RecyclerView.Adapter<BebeAdapter.BebeViewHolder> {

    private List<Bebe> bebeList;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public BebeAdapter(List<Bebe> bebeList) {
        this.bebeList = bebeList;
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
        holder.textViewAula.setText(bebe.getAula());
        holder.imageViewAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });

    }

    @Override
    public int getItemCount() {
        return bebeList.size();
    }

    public static class BebeViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre, textViewAula;
        ImageView imageViewAvatar;

        public BebeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewAula = itemView.findViewById(R.id.textViewAula);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatar);
        }
    }
}
