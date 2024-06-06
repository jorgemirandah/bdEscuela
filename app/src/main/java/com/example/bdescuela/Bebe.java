package com.example.bdescuela;

public class Bebe {
    private String nombre;
    private String apellido;
    private String aula;

    public Bebe(String nombre, String apellido, String aula) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.aula = aula;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getAula() {
        return aula;
    }
}
