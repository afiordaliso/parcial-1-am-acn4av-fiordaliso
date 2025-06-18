package com.example.parcial_1_am_acn4av_fiordaliso.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parcial_1_am_acn4av_fiordaliso.EditarMovimientoActivity;
import com.example.parcial_1_am_acn4av_fiordaliso.Movimiento;
import com.example.parcial_1_am_acn4av_fiordaliso.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private LinearLayout listaTransacciones;
    private FloatingActionButton fabMain;
    private TextView tvTotalMonto, tvIngresosMonto, tvGastosMonto;

    private double total = 0;
    private double totalIngresos = 0;
    private double totalGastos = 0;

    private View viewSeleccionado = null;
    private Movimiento movimientoOriginal;

    private FirebaseFirestore db;
    private String userId;
    private final ActivityResultLauncher<Intent> editarMovimientoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Movimiento actualizado = result.getData().getParcelableExtra("movimientoEditado");
                    if (actualizado != null && viewSeleccionado != null && movimientoOriginal != null) {
                        actualizarTransaccionEnFirestore(viewSeleccionado, movimientoOriginal, actualizado);
                        Toast.makeText(getContext(), "Transacción actualizada", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔥 Inicializar elementos correctamente
        listaTransacciones = view.findViewById(R.id.listaTransacciones);
        tvTotalMonto = view.findViewById(R.id.tvTotalMonto);
        tvIngresosMonto = view.findViewById(R.id.tvIngresosMonto);
        tvGastosMonto = view.findViewById(R.id.tvGastosMonto);
        fabMain = view.findViewById(R.id.fabMain);

        // 🚀 Validación mejorada: evitar errores de referencia nula
        if (listaTransacciones == null || tvTotalMonto == null || tvIngresosMonto == null || tvGastosMonto == null || fabMain == null) {
            Toast.makeText(getContext(), "Error: Elementos de la vista no inicializados correctamente", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 Configurar el botón de nueva transacción
        fabMain.setOnClickListener(v -> mostrarDialogoNuevaTransaccion());

        // 🔥 Obtener usuario autenticado de Firebase
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioActual != null) {
            userId = usuarioActual.getUid();
            cargarTransaccionesDesdeFirestore(userId);
        } else {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }
    }
    private void mostrarDialogoNuevaTransaccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_transaccion, null);
        builder.setView(dialogView);

        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        EditText etMonto = dialogView.findViewById(R.id.etMonto);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);

        if (etDescripcion == null || etMonto == null || spinnerTipo == null) {
            Toast.makeText(getContext(), "Error: No se pudieron inicializar los campos del formulario", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.tipos_transaccion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        builder.setTitle("Nueva Transacción");
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String descripcion = etDescripcion.getText().toString().trim();
            String montoStr = etMonto.getText().toString().trim();
            String tipo = spinnerTipo.getSelectedItem().toString();

            // 🔥 Validaciones mejoradas para evitar errores
            if (descripcion.isEmpty()) {
                Toast.makeText(getContext(), "Error: La descripción no puede estar vacía", Toast.LENGTH_SHORT).show();
                return;
            }

            if (montoStr.isEmpty()) {
                Toast.makeText(getContext(), "Error: Debes ingresar un monto", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double monto = Double.parseDouble(montoStr);

                if (monto <= 0) {
                    Toast.makeText(getContext(), "Error: El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🔥 Corrección: Validar tipo de transacción antes de guardar
                if (!tipo.equalsIgnoreCase("Ingreso") && !tipo.equalsIgnoreCase("Gasto")) {
                    Toast.makeText(getContext(), "Error: Tipo de transacción inválido", Toast.LENGTH_SHORT).show();
                    return;
                }

                guardarTransaccionEnFirestore(descripcion, tipo, monto);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Error: Monto inválido, ingrese un número válido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void guardarTransaccionEnFirestore(String descripcion, String tipo, double monto) {
        FirebaseUser usuarioActual = FirebaseAuth.getInstance().getCurrentUser();
        if (usuarioActual == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = usuarioActual.getUid();

        if (descripcion == null || tipo == null || descripcion.trim().isEmpty() || tipo.trim().isEmpty() || monto <= 0) {
            Toast.makeText(getContext(), "Error: Datos inválidos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!tipo.equalsIgnoreCase("Ingreso") && !tipo.equalsIgnoreCase("Gasto")) {
            Toast.makeText(getContext(), "Error: Tipo de transacción inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        String fecha = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        Map<String, Object> movimiento = new HashMap<>();
        movimiento.put("userId", userId);
        movimiento.put("descripcion", descripcion.trim());
        movimiento.put("tipo", tipo.trim());
        movimiento.put("monto", monto);
        movimiento.put("fecha", fecha);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movimientos")
                .add(movimiento)
                .addOnSuccessListener(documentReference -> {
                    // Guardamos el ID generado como parte del documento, si lo necesitás luego
                    String idGenerado = documentReference.getId();
                    documentReference.update("id", idGenerado) // opcional
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "✅ Transacción agregada correctamente", Toast.LENGTH_SHORT).show();
                                cargarTransaccionesDesdeFirestore(userId);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "⚠️ Transacción guardada, pero no se pudo registrar el ID: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "❌ Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
    private void cargarTransaccionesDesdeFirestore(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (listaTransacciones == null) {
            Toast.makeText(getContext(), "Error: listaTransacciones no inicializada", Toast.LENGTH_SHORT).show();
            return;
        }

        listaTransacciones.removeAllViews();
        total = 0;
        totalIngresos = 0;
        totalGastos = 0;

        db.collection("movimientos")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot == null || querySnapshot.isEmpty()) {
                        Toast.makeText(getContext(), "No hay transacciones registradas", Toast.LENGTH_SHORT).show();
                        actualizarResumen();
                        return;
                    }

                    List<DocumentSnapshot> docs = querySnapshot.getDocuments();

                    Collections.sort(docs, (a, b) -> {
                        String f1 = a.getString("fecha");
                        String f2 = b.getString("fecha");
                        return f2 != null && f1 != null ? f2.compareTo(f1) : 0;
                    });

                    for (DocumentSnapshot doc : docs) {
                        String descripcion = doc.getString("descripcion");
                        String tipo = doc.getString("tipo");
                        Double monto = doc.getDouble("monto");
                        String fechaRaw = doc.getString("fecha");
                        String transaccionId = doc.contains("id") ? doc.getString("id") : doc.getId();

                        if (descripcion == null || tipo == null || monto == null || fechaRaw == null || transaccionId == null) {
                            Log.w("Firestore", "Transacción con campos nulos o ID faltante.");
                            continue;
                        }

                        String fechaFormateada = fechaRaw.length() >= 8
                                ? fechaRaw.substring(6, 8) + "/" + fechaRaw.substring(4, 6) + "/" + fechaRaw.substring(0, 4)
                                : fechaRaw;

                        agregarTransaccion(transaccionId, descripcion, tipo, monto, fechaFormateada);

                        if ("Ingreso".equalsIgnoreCase(tipo)) {
                            totalIngresos += monto;
                            total += monto;
                        } else if ("Gasto".equalsIgnoreCase(tipo)) {
                            totalGastos += monto;
                            total -= monto;
                        } else {
                            Log.w("Firestore", "Tipo desconocido: " + tipo);
                        }
                    }

                    actualizarResumen();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar transacciones: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    private void agregarTransaccion(String transaccionId, String descripcion, String tipo, double monto, String fecha) {
        if (listaTransacciones == null) {
            Toast.makeText(getContext(), "Error: listaTransacciones no inicializada", Toast.LENGTH_SHORT).show();
            return;
        }

        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_transaccion, listaTransacciones, false);

        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        ImageView ivIcono = item.findViewById(R.id.ivIcono);

        if (tvDescripcion == null || tvMonto == null || tvFecha == null || ivIcono == null) {
            Toast.makeText(getContext(), "Error: No se pudieron inicializar los elementos de la transacción", Toast.LENGTH_SHORT).show();
            return;
        }

        tvDescripcion.setText(descripcion);
        tvMonto.setText(String.format(Locale.getDefault(), "$ %.2f", monto));
        tvFecha.setText(fecha);

        if ("Ingreso".equalsIgnoreCase(tipo)) {
            ivIcono.setImageResource(R.drawable.baseline_arrow_upward_24);
            ivIcono.setTag("Ingreso");
        } else if ("Gasto".equalsIgnoreCase(tipo)) {
            ivIcono.setImageResource(R.drawable.baseline_arrow_downward_24);
            ivIcono.setTag("Gasto");
        } else {
            Toast.makeText(getContext(), "Error: Tipo de transacción desconocido", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🧠 Validar ID antes de pasar al intent
        item.setOnClickListener(v -> {
            viewSeleccionado = item;

            String idSeguro = (transaccionId == null || transaccionId.trim().isEmpty()) ? "SIN_ID" : transaccionId;
            Log.d("DEBUG", "Transacción seleccionada - ID: " + idSeguro);

            movimientoOriginal = new Movimiento(
                    idSeguro,
                    descripcion,
                    tipo,
                    monto,
                    fecha
            );

            Intent intent = new Intent(getContext(), EditarMovimientoActivity.class);
            intent.putExtra("movimiento", movimientoOriginal);
            editarMovimientoLauncher.launch(intent);
        });

        listaTransacciones.addView(item);

        if (listaTransacciones.indexOfChild(item) == -1) {
            Toast.makeText(getContext(), "Error: La transacción no se agregó correctamente", Toast.LENGTH_SHORT).show();
        }
    }
    private void actualizarResumen() {
        if (tvTotalMonto != null && tvIngresosMonto != null && tvGastosMonto != null) {
            tvTotalMonto.setText(String.format(Locale.getDefault(), "$ %.2f", total));
            tvIngresosMonto.setText(String.format(Locale.getDefault(), "$ %.2f", totalIngresos));
            tvGastosMonto.setText(String.format(Locale.getDefault(), "$ %.2f", totalGastos));
        } else {
            Toast.makeText(getContext(), "Error: No se pudo actualizar el resumen", Toast.LENGTH_SHORT).show();
        }
    }


    private void editarTransaccion(View item, Movimiento anterior, Movimiento nuevo) {
        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        ImageView ivIcono = item.findViewById(R.id.ivIcono);

        if (anterior.getTipo().equalsIgnoreCase("Ingreso")) {
            totalIngresos -= anterior.getMonto();
            total -= anterior.getMonto();
        } else {
            totalGastos -= anterior.getMonto();
            total += anterior.getMonto();
        }

        tvDescripcion.setText(nuevo.getDescripcion());
        tvMonto.setText(String.format(Locale.getDefault(), "$ %.2f", nuevo.getMonto()));
        tvFecha.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        if (nuevo.getTipo().equalsIgnoreCase("Ingreso")) {
            ivIcono.setImageResource(R.drawable.baseline_arrow_upward_24);
            totalIngresos += nuevo.getMonto();
            total += nuevo.getMonto();
        } else {
            ivIcono.setImageResource(R.drawable.baseline_arrow_downward_24);
            totalGastos += nuevo.getMonto();
            total -= nuevo.getMonto();
        }

        actualizarResumen();
    }

    private void actualizarTransaccionEnFirestore(View item, Movimiento anterior, Movimiento nuevo) {
        if (nuevo.getId() == null || nuevo.getId().isEmpty()) {
            Toast.makeText(getContext(), "Error: ID de transacción no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaFormateada = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("descripcion", nuevo.getDescripcion());
        datosActualizados.put("tipo", nuevo.getTipo());
        datosActualizados.put("monto", nuevo.getMonto());
        datosActualizados.put("fecha", fechaFormateada); // ⏱ mantener consistencia

        FirebaseFirestore.getInstance()
                .collection("movimientos") // 🔄 corregido
                .document(nuevo.getId())
                .update(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    editarTransaccion(item, anterior, nuevo);
                    cargarTransaccionesDesdeFirestore(userId);
                    Toast.makeText(getContext(), "Transacción actualizada", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar en Firestore", Toast.LENGTH_SHORT).show());
    }
}