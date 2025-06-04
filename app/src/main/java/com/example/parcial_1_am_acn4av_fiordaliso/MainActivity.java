package com.example.parcial_1_am_acn4av_fiordaliso;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar Firebase
        initializeFirebase();

        // 2. Configurar navegación con retardo para asegurar que el NavHost esté listo
        getWindow().getDecorView().post(() -> {
            setupNavigation();
            testFirebaseConnection();
        });
    }

    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this);
            mDatabase = FirebaseDatabase.getInstance().getReference();
            Log.d(TAG, "Firebase inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error al inicializar Firebase", e);
            showToast("Error al conectar con Firebase");
        }
    }

    private void setupNavigation() {
        try {
            BottomNavigationView navView = findViewById(R.id.nav_view);
            navView.setVisibility(View.GONE);

            // Configurar NavController con el host fragment
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_dashboard,
                    R.id.navigation_notifications)
                    .build();

            NavigationUI.setupWithNavController(navView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.navigation_home ||
                        destination.getId() == R.id.navigation_dashboard ||
                        destination.getId() == R.id.navigation_notifications) {
                    navView.setVisibility(View.VISIBLE);
                } else {
                    navView.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error en configuración de navegación", e);
            showToast("Error en sistema de navegación");
        }
    }

    private void testFirebaseConnection() {
        try {
            mDatabase.child("connection_test").setValue("test_value")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Conexión a Firebase exitosa");
                            showToast("Conectado a Firebase");
                        } else {
                            Log.e(TAG, "Error en conexión Firebase", task.getException());
                            showToast("Error en conexión Firebase");
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error al probar conexión Firebase", e);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}