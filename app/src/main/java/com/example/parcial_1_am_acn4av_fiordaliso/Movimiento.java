package com.example.parcial_1_am_acn4av_fiordaliso;

import android.os.Parcel;
import android.os.Parcelable;

public class Movimiento implements Parcelable {
    private String id;
    private String descripcion;
    private String tipo;
    private double monto;
    private String fecha;

    // Constructor con ID
    public Movimiento(String id, String descripcion, String tipo, double monto, String fecha) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : "SIN_ID";
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
    }

    // Constructor sin ID (nuevo movimiento)
    public Movimiento(String descripcion, String tipo, double monto, String fecha) {
        this("SIN_ID", descripcion, tipo, monto, fecha);
    }

    protected Movimiento(Parcel in) {
        id = in.readString();
        descripcion = in.readString();
        tipo = in.readString();
        monto = in.readDouble();
        fecha = in.readString();
    }

    public static final Creator<Movimiento> CREATOR = new Creator<>() {
        @Override
        public Movimiento createFromParcel(Parcel in) {
            return new Movimiento(in);
        }

        @Override
        public Movimiento[] newArray(int size) {
            return new Movimiento[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(descripcion);
        dest.writeString(tipo);
        dest.writeDouble(monto);
        dest.writeString(fecha);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = (id != null && !id.trim().isEmpty()) ? id : "SIN_ID";
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public double getMonto() {
        return monto;
    }

    public String getFecha() {
        return fecha;
    }
}