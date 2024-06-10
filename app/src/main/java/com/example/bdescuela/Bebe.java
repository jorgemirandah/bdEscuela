package com.example.bdescuela;

public class Bebe {
    private final int id;
    private String nombre;
    private String apellido;
    private String aula;
    private byte[] imagen;
    private boolean asistiendo;

    public Bebe(int id, String nombre, String apellido, String aula, byte[] imagen) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.aula = aula;
        this.imagen = imagen;
        this.asistiendo = false;
    }

    public int getId() {
        return id;
    }
    public void setNombre(String nombre){
        this.nombre = nombre;
    }
    public void setApellido(String apellido){
        this.apellido = apellido;
    }
    public void setAula(String aula){
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

    public void setImagen(byte[] imagen){
        this.imagen = imagen;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public boolean isAsistiendo() {
        return asistiendo;
    }

    public void setAsistiendo(boolean asistiendo) {
        this.asistiendo = asistiendo;
    }

    public String getNombreCompleto(){
        return getNombre() + " " + getApellido();
    }
}
