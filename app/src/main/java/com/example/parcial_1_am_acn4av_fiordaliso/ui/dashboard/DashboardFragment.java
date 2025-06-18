package com.example.parcial_1_am_acn4av_fiordaliso.ui.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.parcial_1_am_acn4av_fiordaliso.EditarCuentaActivity;
import com.example.parcial_1_am_acn4av_fiordaliso.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private LinearLayout listaCuentas;
    private Button btnAgregarCuenta;

    private final ActivityResultLauncher<Intent> editarCuentaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK || result.getResultCode() == Activity.RESULT_FIRST_USER) {
                    cargarCuentasDesdeFirestore(); // Recargar si fue editada o eliminada
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listaCuentas = view.findViewById(R.id.listaCuentas);
        btnAgregarCuenta = view.findViewById(R.id.btnAgregarCuenta);

        btnAgregarCuenta.setOnClickListener(v -> mostrarDialogoAgregarCuenta());

        cargarCuentasDesdeFirestore();
    }

    private void mostrarDialogoAgregarCuenta() {
        EditText input = new EditText(requireContext());
        input.setHint("Nombre de la cuenta");

        new AlertDialog.Builder(requireContext())
                .setTitle("Nueva Cuenta")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombreCuenta = input.getText().toString().trim();
                    if (!nombreCuenta.isEmpty()) {
                        guardarCuentaEnFirestore(nombreCuenta);
                    } else {
                        Toast.makeText(getContext(), "Ingrese un nombre válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void guardarCuentaEnFirestore(String nombreCuenta) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombreCuenta);
        datos.put("userId", userId);

        FirebaseFirestore.getInstance()
                .collection("cuentas")
                .add(datos)
                .addOnSuccessListener(docRef -> {
                    String idGenerado = docRef.getId();
                    docRef.update("id", idGenerado);
                    Toast.makeText(getContext(), "✅ Cuenta guardada", Toast.LENGTH_SHORT).show();
                    cargarCuentasDesdeFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarCuentasDesdeFirestore() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        listaCuentas.removeAllViews();

        FirebaseFirestore.getInstance()
                .collection("cuentas")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String nombre = doc.getString("nombre");
                        String id = doc.contains("id") ? doc.getString("id") : doc.getId();

                        if (nombre != null && id != null) {
                            agregarCuentaALista(nombre, id);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar cuentas", Toast.LENGTH_SHORT).show()
                );
    }

    private void agregarCuentaALista(String nombre, String cuentaId) {
        CardView card = new CardView(requireContext());
        card.setCardElevation(8);
        card.setRadius(16);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

        TextView texto = new TextView(requireContext());
        texto.setText(nombre);
        texto.setTextSize(16);
        texto.setPadding(24, 24, 24, 24);
        texto.setTextColor(Color.parseColor("#212121"));

        card.addView(texto);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditarCuentaActivity.class);
            intent.putExtra("cuentaId", cuentaId);
            intent.putExtra("cuentaNombre", nombre);
            editarCuentaLauncher.launch(intent); // 👉 usar launcher
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 0);

        listaCuentas.addView(card, params);
    }
}