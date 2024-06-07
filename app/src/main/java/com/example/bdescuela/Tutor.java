package com.example.bdescuela;

public class Tutor {
    private int id;
    private int bebeId;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;
    private String direccion;

    public Tutor() {
    }

    public Tutor(int id, int bebeId, String nombre, String apellido, String telefono, String email, String direccion) {
        this.id = id;
        this.bebeId = bebeId;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isEmpty() {
        return (nombre == null || nombre.isEmpty()) &&
                (apellido == null || apellido.isEmpty()) &&
                (telefono == null || telefono.isEmpty()) &&
                (email == null || email.isEmpty()) &&
                (direccion == null || direccion.isEmpty());
    }

}
