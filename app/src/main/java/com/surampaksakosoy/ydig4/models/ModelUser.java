package com.surampaksakosoy.ydig4.models;

public class ModelUser {
    private int id;
    private String sumber_login, id_login, nama, email, version;

    public ModelUser() {
    }

    public ModelUser(int id, String sumber_login, String id_login, String nama, String email, String version) {
        this.id = id;
        this.sumber_login = sumber_login;
        this.id_login = id_login;
        this.nama = nama;
        this.email = email;
        this.version = version;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSumber_login() {
        return sumber_login;
    }

    public String getId_login() {
        return id_login;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getVersion() {
        return version;
    }
}
