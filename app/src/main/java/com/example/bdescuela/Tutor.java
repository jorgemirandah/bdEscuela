package com.example.bdescuela;

public class Tutor {
    private int id;
    private int bebeId;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String movil;

    public Tutor() {
    }

    public Tutor(int id, int bebeId, String nombre, String apellido, String telefono, String email, String movil) {
        this.id = id;
        this.bebeId = bebeId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.movil = movil;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBebeId() {
        return bebeId;
    }

    public void setBebeId(int bebeId) {
        this.bebeId = bebeId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }

    public boolean isEmpty() {
        return (nombre == null || nombre.isEmpty()) &&
                (apellido == null || apellido.isEmpty()) &&
                (telefono == null || telefono.isEmpty()) &&
                (email == null || email.isEmpty()) &&
                (movil == null || movil.isEmpty());
    }

}
