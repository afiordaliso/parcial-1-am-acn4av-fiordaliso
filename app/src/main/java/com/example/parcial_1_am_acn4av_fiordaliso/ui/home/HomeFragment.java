package com.example.parcial_1_am_acn4av_fiordaliso.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
    private String userId = "Agustin"; // TODO: Reemplazar con el usuario autenticado

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

        db = FirebaseFirestore.getInstance();

        listaTransacciones = view.findViewById(R.id.listaTransacciones);
        fabMain = view.findViewById(R.id.fabMain);
        tvTotalMonto = view.findViewById(R.id.tvTotalMonto);
        tvIngresosMonto = view.findViewById(R.id.tvIngresosMonto);
        tvGastosMonto = view.findViewById(R.id.tvGastosMonto);

        fabMain.setOnClickListener(v -> mostrarDialogoNuevaTransaccion());

        cargarTransaccionesDesdeFirestore();
    }

    private void mostrarDialogoNuevaTransaccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_transaccion, null);
        builder.setView(dialogView);

        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        EditText etMonto = dialogView.findViewById(R.id.etMonto);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinnerTipo);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.tipos_transaccion, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        builder.setTitle("Nueva Transacción");
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String descripcion = etDescripcion.getText().toString().trim();
            String montoStr = etMonto.getText().toString().trim();
            String tipo = spinnerTipo.getSelectedItem().toString();

            if (!descripcion.isEmpty() && !montoStr.isEmpty()) {
                try {
                    double monto = Double.parseDouble(montoStr);
                    guardarTransaccionEnFirestore(descripcion, tipo, monto);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.create().show();
    }

    private void guardarTransaccionEnFirestore(String descripcion, String tipo, double monto) {
        Map<String, Object> movimiento = new HashMap<>();
        movimiento.put("descripcion", descripcion);
        movimiento.put("tipo", tipo);
        movimiento.put("monto", monto);
        movimiento.put("fecha", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        db.collection("usuarios").document(userId)
                .collection("movimientos").add(movimiento)
                .addOnSuccessListener(documentReference -> cargarTransaccionesDesdeFirestore())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al guardar", Toast.LENGTH_SHORT).show());
    }

    private void cargarTransaccionesDesdeFirestore() {
        listaTransacciones.removeAllViews();
        total = 0;
        totalIngresos = 0;
        totalGastos = 0;

        db.collection("usuarios").document(userId)
                .collection("movimientos").get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        agregarTransaccion(doc.getId(), doc.getString("descripcion"), doc.getString("tipo"), doc.getDouble("monto"), doc.getString("fecha"));
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al cargar transacciones", Toast.LENGTH_SHORT).show());
    }

    private void agregarTransaccion(String transaccionId, String descripcion, String tipo, double monto, String fecha) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_transaccion, listaTransacciones, false);

        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        ImageView ivIcono = item.findViewById(R.id.ivIcono);

        tvDescripcion.setText(descripcion);
        tvMonto.setText(String.format(Locale.getDefault(), "$ %.2f", monto));
        tvFecha.setText(fecha);

        if (tipo.equalsIgnoreCase("Ingreso")) {
            ivIcono.setImageResource(R.drawable.baseline_arrow_upward_24);
            ivIcono.setTag("Ingreso");
            totalIngresos += monto;
            total += monto;
        } else {
            ivIcono.setImageResource(R.drawable.baseline_arrow_downward_24);
            ivIcono.setTag("Gasto");
            totalGastos += monto;
            total -= monto;
        }

        actualizarResumen();

        item.setOnClickListener(v -> {
            movimientoOriginal = new Movimiento(descripcion, tipo, monto, fecha);
            viewSeleccionado = item;

            Intent intent = new Intent(getContext(), EditarMovimientoActivity.class);
            intent.putExtra("movimiento", movimientoOriginal);
            editarMovimientoLauncher.launch(intent);
        });

        listaTransacciones.addView(item);
    }

    private void actualizarResumen() {
        tvTotalMonto.setText(String.format(Locale.getDefault(), "$ %.2f", total));
        tvIngresosMonto.setText(String.format(Locale.getDefault(), "$ %.2f", totalIngresos));
        tvGastosMonto.setText(String.format(Locale.getDefault(), "$ %.2f", totalGastos));
    }

    private void editarTransaccion(View item, Movimiento anterior, Movimiento nuevo) {
        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        ImageView ivIcono = item.findViewById(R.id.ivIcono);

        // Restar valores anteriores
        if (anterior.getTipo().equalsIgnoreCase("Ingreso")) {
            totalIngresos -= anterior.getMonto();
            total -= anterior.getMonto();
        } else {
            totalGastos -= anterior.getMonto();
            total += anterior.getMonto();
        }

        // Aplicar nuevos valores
        tvDescripcion.setText(nuevo.getDescripcion());
        tvMonto.setText(String.format(Locale.getDefault(), "$ %.2f", nuevo.getMonto()));
        String nuevaFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        tvFecha.setText(nuevaFecha);

        if (nuevo.getTipo().equalsIgnoreCase("Ingreso")) {
            ivIcono.setImageResource(R.drawable.baseline_arrow_upward_24);
            ivIcono.setTag("Ingreso");
            totalIngresos += nuevo.getMonto();
            total += nuevo.getMonto();
        } else {
            ivIcono.setImageResource(R.drawable.baseline_arrow_downward_24);
            ivIcono.setTag("Gasto");
            totalGastos += nuevo.getMonto();
            total -= nuevo.getMonto();
        }

        actualizarResumen();
    }
    private void actualizarTransaccionEnFirestore(View item, Movimiento anterior, Movimiento nuevo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String transaccionId = anterior.getId(); // Esto requiere que `Movimiento` tenga un campo `id`
        if (transaccionId == null || transaccionId.isEmpty()) {
            Toast.makeText(getContext(), "Error: ID de transacción no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> datosActualizados = new HashMap<>();
        datosActualizados.put("descripcion", nuevo.getDescripcion());
        datosActualizados.put("tipo", nuevo.getTipo());
        datosActualizados.put("monto", nuevo.getMonto());
        datosActualizados.put("fecha", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        db.collection("usuarios").document(userId) // Reemplazar con el usuario autenticado
                .collection("movimientos").document(transaccionId)
                .update(datosActualizados)
                .addOnSuccessListener(aVoid -> {
                    editarTransaccion(item, anterior, nuevo); // Actualiza la vista en pantalla
                    Toast.makeText(getContext(), "Transacción actualizada en Firestore", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar en Firestore", Toast.LENGTH_SHORT).show());
    }
}