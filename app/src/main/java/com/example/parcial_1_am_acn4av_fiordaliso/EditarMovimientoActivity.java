package com.example.parcial_1_am_acn4av_fiordaliso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditarMovimientoActivity extends AppCompatActivity {
    private EditText etDescripcion, etMonto;
    private Spinner spinnerTipo;
    private Button btnGuardar;

    private Movimiento movimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_movimiento);

        etDescripcion = findViewById(R.id.etDescripcion);
        etMonto = findViewById(R.id.etMonto);
        spinnerTipo = findViewById(R.id.spinnerTipo);
        btnGuardar = findViewById(R.id.btnGuardar);

        movimiento = getIntent().getParcelableExtra("movimiento");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tipos_transaccion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        if (movimiento != null) {
            etDescripcion.setText(movimiento.getDescripcion());
            etMonto.setText(String.valueOf(movimiento.getMonto()));

            int index = movimiento.getTipo().equalsIgnoreCase("Ingreso") ? 0 : 1;
            spinnerTipo.setSelection(index);
        }

        btnGuardar.setOnClickListener(v -> {
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

            Movimiento editado = new Movimiento(
                    nuevaDescripcion,
                    nuevoTipo,
                    nuevoMonto,
                    movimiento.getFecha() // conservamos la fecha original
            );

            Intent resultIntent = new Intent();
            resultIntent.putExtra("movimientoEditado", editado);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}