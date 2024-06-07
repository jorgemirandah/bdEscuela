package com.example.bdescuela;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class TutorDetailActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TutorPagerAdapter pagerAdapter;
    private TextView textViewCount;
    private Button buttonEliminar;

    private DatabaseHelper dbHelper;
    private int bebeId;
    private List<Tutor> tutorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_detail);
        viewPager = findViewById(R.id.viewPager);
        textViewCount = findViewById(R.id.textViewCount);
        buttonEliminar = findViewById(R.id.buttonEliminar);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        bebeId = intent.getIntExtra("bebe_id", -1);

        cargarTutores();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                guardarTutor(viewPager.getCurrentItem());
                actualizarTextViewCount();
                verificarTutorVacio(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    guardarTutor(viewPager.getCurrentItem());
                }
            }
        });

        buttonEliminar.setOnClickListener(v -> eliminarTutorActual());
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarTutores();
    }

    private void cargarTutores() {
        tutorList = dbHelper.obtenerTutoresPorBebeId(bebeId);
        if (tutorList.isEmpty()) {
            Tutor nuevoTutor = new Tutor();
            nuevoTutor.setBebeId(bebeId);
            tutorList.add(nuevoTutor);
        }
        pagerAdapter = new TutorPagerAdapter(this, tutorList);
        viewPager.setAdapter(pagerAdapter);
        actualizarTextViewCount();
    }

    private void guardarTutores() {
        for (int i = 0; i < pagerAdapter.getItemCount(); i++) {
            guardarTutor(i);
        }
    }

    private void guardarTutor(int position) {
        if (position < tutorList.size()) {
            Tutor tutor = tutorList.get(position);
            View view = viewPager.findViewWithTag("view" + position);
            if (view != null) {
                EditText editTextNombre = view.findViewById(R.id.editTextNombre);
                EditText editTextApellido = view.findViewById(R.id.editTextApellido);
                EditText editTextTelefono = view.findViewById(R.id.editTextTelefono);
                EditText editTextEmail = view.findViewById(R.id.editTextEmail);
                EditText editTextDireccion = view.findViewById(R.id.editTextDireccion);

                tutor.setNombre(editTextNombre.getText().toString());
                tutor.setApellido(editTextApellido.getText().toString());
                tutor.setTelefono(editTextTelefono.getText().toString());
                tutor.setEmail(editTextEmail.getText().toString());
                tutor.setDireccion(editTextDireccion.getText().toString());

                dbHelper.guardarTutor(tutor);
            }
        }
    }

    private void actualizarTextViewCount() {
        String texto = (viewPager.getCurrentItem() + 1) + "/" + tutorList.size();
        textViewCount.setText(texto);
    }

    private void verificarTutorVacio(int position) {
        if (position == tutorList.size() - 1 && !tutorList.get(position).isEmpty()) {
            Tutor nuevoTutor = new Tutor();
            nuevoTutor.setBebeId(bebeId);
            tutorList.add(nuevoTutor);
            pagerAdapter.notifyItemInserted(tutorList.size() - 1);
            actualizarTextViewCount();
        }
    }

    private void eliminarTutorActual() {
        int position = viewPager.getCurrentItem();
        if (tutorList.size() > 1) {
            Tutor tutorAEliminar = tutorList.get(position);
            dbHelper.eliminarTutor(tutorAEliminar.getId());
            tutorList.remove(position);
            pagerAdapter.notifyItemRemoved(position);
            if (position == tutorList.size()) {
                viewPager.setCurrentItem(position - 1);
            }
            actualizarTextViewCount();
        }
    }
}
