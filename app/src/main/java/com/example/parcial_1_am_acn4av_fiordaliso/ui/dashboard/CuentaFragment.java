package com.example.parcial_1_am_acn4av_fiordaliso.ui.dashboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.parcial_1_am_acn4av_fiordaliso.EditarCuentaActivity;
import com.example.parcial_1_am_acn4av_fiordaliso.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CuentaFragment extends Fragment {

    private LinearLayout listaCuentas;
    private Button btnAgregarCuenta;

    private final ActivityResultLauncher<Intent> editarCuentaLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK || result.getResultCode() == Activity.RESULT_FIRST_USER) {
                    cargarCuentasDesdeFirestore();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuentas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listaCuentas = view.findViewById(R.id.listaCuentas);
        btnAgregarCuenta = view.findViewById(R.id.btnAgregarCuenta);

        btnAgregarCuenta.setOnClickListener(v -> mostrarDialogoAgregarCuenta());

        cargarCuentasDesdeFirestore();

        Button btnDescargarInfo = view.findViewById(R.id.btnDescargarInfo);
        if (btnDescargarInfo != null) {
            btnDescargarInfo.setOnClickListener(v -> {
                String url = "https://www.hubspot.com/hubfs/media/Elementosdeunplanfinanciero.png";

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setTitle("Descargando infografía");
                request.setDescription("Tu archivo se está descargando...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, "infografia_finanzas.png");

                DownloadManager manager =
                        (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);
                if (manager != null) {
                    manager.enqueue(request);
                    Toast.makeText(getContext(), "Descarga iniciada", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombreCuenta);
        datos.put("userId", userId);
        datos.put("fecha", fechaHoy);

        FirebaseFirestore.getInstance()
                .collection("cuentas")
                .add(datos)
                .addOnSuccessListener(docRef -> {
                    String idGenerado = docRef.getId();
                    docRef.update("id", idGenerado);
                    Toast.makeText(getContext(), "Cuenta guardada", Toast.LENGTH_SHORT).show();
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
                        String fecha = doc.getString("fecha");

                        if (nombre != null && id != null) {
                            agregarCuentaALista(nombre, id, fecha);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar cuentas", Toast.LENGTH_SHORT).show()
                );
    }

    private void agregarCuentaALista(String nombre, String cuentaId, String fecha) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View itemView = inflater.inflate(R.layout.item_cuenta, listaCuentas, false);

        TextView tvNombre = itemView.findViewById(R.id.tvNombreCuenta);
        TextView tvFecha = itemView.findViewById(R.id.tvFechaCuenta);
        ImageView icon = itemView.findViewById(R.id.iconCuenta);

        tvNombre.setText(nombre);
        tvFecha.setText(fecha != null ? fecha : "");

        icon.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.colorPrimary),
                PorterDuff.Mode.SRC_IN
        );

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), EditarCuentaActivity.class);
            intent.putExtra("cuentaId", cuentaId);
            intent.putExtra("cuentaNombre", nombre);
            editarCuentaLauncher.launch(intent);
        });

        listaCuentas.addView(itemView);
    }
}