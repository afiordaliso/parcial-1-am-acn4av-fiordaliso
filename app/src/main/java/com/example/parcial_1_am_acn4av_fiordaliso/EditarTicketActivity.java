package com.example.parcial_1_am_acn4av_fiordaliso;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditarTicketActivity extends AppCompatActivity {

    private EditText etCantidad;
    private TextView tvTicker, tvVariacion, tvValorActual;
    private Button btnGuardar, btnEliminar;
    private String docId;
    private double precio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_ticket);

        etCantidad = findViewById(R.id.etEditarCantidad);
        tvTicker = findViewById(R.id.tvEditarTicker);
        tvVariacion = findViewById(R.id.tvEditarVariacion);
        tvValorActual = findViewById(R.id.tvEditarValorActual);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        btnEliminar = findViewById(R.id.btnEliminarTicket);

        Intent intent = getIntent();
        docId = intent.getStringExtra("docId");
        String ticker = intent.getStringExtra("ticker");
        long cantidad = intent.getLongExtra("cantidad", 0);
        precio = intent.getDoubleExtra("precioActual", 0);
        String variacion = intent.getStringExtra("variacion");

        tvTicker.setText(ticker);
        etCantidad.setText(String.valueOf(cantidad));
        tvVariacion.setText("Variación: " + variacion);
        actualizarValorTotal((int) cantidad);

        etCantidad.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int nuevaCantidad = Integer.parseInt(s.toString());
                    actualizarValorTotal(nuevaCantidad);
                } catch (NumberFormatException e) {
                    tvValorActual.setText("Valor actual: ---");
                }
            }
        });

        btnGuardar.setOnClickListener(v -> {
            String nuevaCantidadStr = etCantidad.getText().toString().trim();
            if (nuevaCantidadStr.isEmpty()) {
                Toast.makeText(this, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                return;
            }

            int nuevaCantidad = Integer.parseInt(nuevaCantidadStr);
            if (nuevaCantidad <= 0) {
                Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("tickets")
                    .document(docId)
                    .update("cantidad", nuevaCantidad)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Ticket actualizado", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                    );
        });

        btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("¿Eliminar ticket?")
                    .setMessage("Esta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("tickets")
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Ticket eliminado", Toast.LENGTH_SHORT).show();
                                    setResult(Activity.RESULT_OK);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al eliminar ticket", Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void actualizarValorTotal(int cantidad) {
        double total = precio * cantidad;
        tvValorActual.setText("Valor actual: $" + String.format("%.2f", total));
    }
}