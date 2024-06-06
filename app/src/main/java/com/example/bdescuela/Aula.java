package com.example.bdescuela;

public class Aula {
    private String nombre;
    private int capacidad;
    private int color;

    public Aula(String nombre, int capacidad, int color) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
