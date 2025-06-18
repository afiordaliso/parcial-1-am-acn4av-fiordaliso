package com.example.parcial_1_am_acn4av_fiordaliso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarMovimientoActivity extends AppCompatActivity {
    private EditText etDescripcion, etMonto;
    private Spinner spinnerTipo;
    private Button btnGuardar;

    private Movimiento movimiento;
    private FirebaseFirestore db;
    private String userId = "Agustin"; // TODO: Reemplazar con el usuario autenticado dinámico
    private String transaccionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_movimiento);

        db = FirebaseFirestore.getInstance();

        etDescripcion = findViewById(R.id.etDescripcion);
        etMonto = findViewById(R.id.etMonto);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Configurar el Spinner correctamente
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_transaccion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        movimiento = getIntent().getParcelableExtra("movimiento");
        transaccionId = getIntent().getStringExtra("transaccionId"); // ID de Firestore

        if (movimiento != null) {
            etDescripcion.setText(movimiento.getDescripcion());
            etMonto.setText(String.valueOf(movimiento.getMonto()));

            // 🚀 Asegurar que el Spinner refleje si es "Ingreso" o "Gasto"
            if (movimiento.getTipo().equalsIgnoreCase("Ingreso")) {
                spinnerTipo.setSelection(0); // Primera opción
            } else {
                spinnerTipo.setSelection(1); // Segunda opción
            }

            // Validación: evitar IDs nulos antes de actualizar Firestore
            if (transaccionId == null || transaccionId.isEmpty()) {
                Toast.makeText(this, "Error: ID de transacción no encontrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        btnGuardar.setOnClickListener(v -> guardarCambiosEnFirestore());
    }

    private void guardarCambiosEnFirestore() {
        if (transaccionId == null || transaccionId.isEmpty()) {
            Toast.makeText(this, "Error: ID de transacción no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevaDescripcion = etDescripcion.getText().toString().trim();
        String montoStr = etMonto.getText().toString().trim();
        String nuevoTipo = spinnerTipo.getSelectedItem().toString();

        if (nuevaDescripcion.isEmpty() || montoStr.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double nuevoMonto;
        try {
            nuevoMonto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Map con los datos actualizados
        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("descripcion", nuevaDescripcion);
        datosActualizados.put("tipo", nuevoTipo);
        datosActualizados.put("monto", nuevoMonto);
        datosActualizados.put("fecha", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        db.collection("usuarios").document(userId)
                .collection("movimientos").document(transaccionId)
                .update(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    // Enviar cambios de vuelta a `HomeFragment`
                    Intent resultIntent = new Intent();
                    Movimiento editado = new Movimiento(transaccionId, nuevaDescripcion, nuevoTipo, nuevoMonto, movimiento.getFecha());
                    resultIntent.putExtra("movimientoEditado", editado);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar en Firestore", Toast.LENGTH_SHORT).show());
    }
}