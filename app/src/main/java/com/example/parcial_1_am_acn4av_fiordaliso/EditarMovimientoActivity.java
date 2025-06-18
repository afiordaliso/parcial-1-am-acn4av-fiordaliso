package com.example.parcial_1_am_acn4av_fiordaliso;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditarMovimientoActivity extends AppCompatActivity {

    private EditText etDescripcion, etMonto;
    private Spinner spinnerTipo;
    private Button btnGuardar, btnEliminar;

    private Movimiento movimiento;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_movimiento);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "SIN_USUARIO";

        etDescripcion = findViewById(R.id.etDescripcion);
        etMonto = findViewById(R.id.etMonto);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_transaccion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        movimiento = getIntent().getParcelableExtra("movimiento");

        if (movimiento == null || movimiento.getId().equals("SIN_ID")) {
            Toast.makeText(this, "Error: No se recibió una transacción válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etDescripcion.setText(movimiento.getDescripcion());
        etMonto.setText(String.valueOf(movimiento.getMonto()));
        spinnerTipo.setSelection("Ingreso".equalsIgnoreCase(movimiento.getTipo()) ? 0 : 1);

        btnGuardar.setOnClickListener(v -> guardarCambiosEnFirestore());
        btnEliminar.setOnClickListener(v -> confirmarEliminacion());
    }

    private void guardarCambiosEnFirestore() {
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

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("descripcion", nuevaDescripcion);
        datosActualizados.put("tipo", nuevoTipo);
        datosActualizados.put("monto", nuevoMonto);
        datosActualizados.put("fecha", new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date()));

        db.collection("movimientos")
                .document(movimiento.getId())
                .update(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    Intent resultIntent = new Intent();
                    Movimiento editado = new Movimiento(movimiento.getId(), nuevaDescripcion, nuevoTipo, nuevoMonto, movimiento.getFecha());
                    resultIntent.putExtra("movimientoEditado", editado);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar en Firestore", Toast.LENGTH_SHORT).show());
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar transacción?")
                .setMessage("Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarTransaccion())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarTransaccion() {
        db.collection("movimientos")
                .document(movimiento.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "🗑️ Transacción eliminada", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_FIRST_USER);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
    }
}