package com.example.parcial_1_am_acn4av_fiordaliso;

import android.os.Parcel;
import android.os.Parcelable;

public class Movimiento implements Parcelable {
    private String descripcion;
    private String tipo;
    private double monto;
    private String fecha;

    public Movimiento(String descripcion, String tipo, double monto, String fecha) {
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
    }

    protected Movimiento(Parcel in) {
        descripcion = in.readString();
        tipo = in.readString();
        monto = in.readDouble();
        fecha = in.readString();
    }

    public static final Creator<Movimiento> CREATOR = new Creator<Movimiento>() {
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
        dest.writeString(descripcion);
        dest.writeString(tipo);
        dest.writeDouble(monto);
        dest.writeString(fecha);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // ✅ Getters agregados
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