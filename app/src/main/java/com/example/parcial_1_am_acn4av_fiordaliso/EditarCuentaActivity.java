package com.example.parcial_1_am_acn4av_fiordaliso;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditarCuentaActivity extends AppCompatActivity {

    private EditText etNombreCuenta;
    private Button btnGuardar, btnEliminar;
    private String cuentaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_cuenta);

        etNombreCuenta = findViewById(R.id.etNombreCuenta);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnEliminar = findViewById(R.id.btnEliminar);

        cuentaId = getIntent().getStringExtra("cuentaId");
        String nombre = getIntent().getStringExtra("cuentaNombre");

        if (cuentaId == null || nombre == null) {
            Toast.makeText(this, "Cuenta no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etNombreCuenta.setText(nombre);

        btnGuardar.setOnClickListener(v -> {
            String nuevoNombre = etNombreCuenta.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                FirebaseFirestore.getInstance()
                        .collection("cuentas")
                        .document(cuentaId)
                        .update("nombre", nuevoNombre)
                        .addOnSuccessListener(unused -> {
                            setResult(RESULT_OK);
                            finish();
                        });
            }
        });

        btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar cuenta")
                    .setMessage("¿Estás seguro de eliminar esta cuenta?")
                    .setPositiveButton("Sí", (d, w) -> {
                        FirebaseFirestore.getInstance()
                                .collection("cuentas")
                                .document(cuentaId)
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    setResult(Activity.RESULT_FIRST_USER);
                                    finish();

                                });
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}