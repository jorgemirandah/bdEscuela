package com.example.bdescuela;

public class Aula {
    private int id;
    private String nombre;
    private int capacidad;
    private int color;

    public Aula(int id, String nombre, int capacidad, int color) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
    }
/*
    public Aula(String nombre, int capacidad, int color) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.color = color;
    }
*/

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getColor() {
        return color;
    }
}
