package com.surampaksakosoy.ydig4.models;

public class ModelUser {
    private int id;
    private String sumber_login, id_login, nama, email;

    public ModelUser() {
    }

    public ModelUser(int id, String sumber_login, String id_login, String nama, String email) {
        this.id = id;
        this.sumber_login = sumber_login;
        this.id_login = id_login;
        this.nama = nama;
        this.email = email;
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

    public void setSumber_login(String sumber_login) {
        this.sumber_login = sumber_login;
    }

    public String getId_login() {
        return id_login;
    }

    public void setId_login(String id_login) {
        this.id_login = id_login;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
