package com.example.parcial_1_am_acn4av_fiordaliso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextNombre, editTextEmail, editTextPassword;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        initUI();
    }

    private void initUI() {
        editTextNombre = findViewById(R.id.editTextNombre);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String nombre = editTextNombre.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInput(nombre, email, password)) return;

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        saveUserToFirestore(user, nombre);
                        showToast("Registro exitoso!");
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showToast("Error: " + task.getException().getMessage());
                    }
                });
    }

    private boolean validateInput(String nombre, String email, String password) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showToast("Complete todos los campos");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Email inválido");
            return false;
        }

        if (password.length() < 6) {
            showToast("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }

    private void saveUserToFirestore(FirebaseUser user, String nombre) {
        if (user == null) return;

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("name", nombre);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(user.getUid()).set(userData);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}