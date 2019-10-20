package com.surampaksakosoy.ydig4.models;

public class ModelStreaming {
    private int id, type_pesan;
    private String pesan, waktu, jam, id_login, photo, uniq_id;

    public ModelStreaming(int id, String pesan, String waktu, String jam, String id_login, String photo, String uniq_id, int type_pesan) {
        this.id = id;
        this.pesan = pesan;
        this.waktu = waktu;
        this.jam = jam;
        this.id_login = id_login;
        this.photo = photo;
        this.uniq_id = uniq_id;
        this.type_pesan = type_pesan;
    }

    public int getId() {
        return id;
    }

    public String getPesan() {
        return pesan;
    }

    public String getWaktu() {
        return waktu;
    }

    public String getJam() {
        return jam;
    }

    public String getId_login() {
        return id_login;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUniq_id() {
        return uniq_id;
    }

    public int getType_pesan() {
        return type_pesan;
    }
}
