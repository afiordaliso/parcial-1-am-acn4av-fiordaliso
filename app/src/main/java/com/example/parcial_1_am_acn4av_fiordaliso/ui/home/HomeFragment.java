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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parcial_1_am_acn4av_fiordaliso.EditarMovimientoActivity;
import com.example.parcial_1_am_acn4av_fiordaliso.Movimiento;
import com.example.parcial_1_am_acn4av_fiordaliso.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private LinearLayout listaTransacciones;
    private FloatingActionButton fabMain;
    private TextView tvTotalMonto, tvIngresosMonto, tvGastosMonto;

    private double total = 0;
    private double totalIngresos = 0;
    private double totalGastos = 0;

    private View viewSeleccionado = null;
    private Movimiento movimientoOriginal;

    private final ActivityResultLauncher<Intent> editarMovimientoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Movimiento actualizado = result.getData().getParcelableExtra("movimientoEditado");
                    if (actualizado != null && viewSeleccionado != null && movimientoOriginal != null) {
                        editarTransaccion(viewSeleccionado, movimientoOriginal, actualizado);
                        Toast.makeText(getContext(), "Movimiento actualizado", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listaTransacciones = view.findViewById(R.id.listaTransacciones);
        fabMain = view.findViewById(R.id.fabMain);
        tvTotalMonto = view.findViewById(R.id.tvTotalMonto);
        tvIngresosMonto = view.findViewById(R.id.tvIngresosMonto);
        tvGastosMonto = view.findViewById(R.id.tvGastosMonto);

        fabMain.setOnClickListener(v -> mostrarDialogoNuevaTransaccion());
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
                    agregarTransaccion(descripcion, tipo, monto);
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

    private void agregarTransaccion(String descripcion, String tipo, double monto) {
        View item = LayoutInflater.from(getContext()).inflate(R.layout.item_transaccion, listaTransacciones, false);

        TextView tvDescripcion = item.findViewById(R.id.tvDescripcion);
        TextView tvMonto = item.findViewById(R.id.tvMonto);
        TextView tvFecha = item.findViewById(R.id.tvFecha);
        ImageView ivIcono = item.findViewById(R.id.ivIcono);

        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

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
            TextView descripcionView = item.findViewById(R.id.tvDescripcion);
            TextView montoView = item.findViewById(R.id.tvMonto);
            TextView fechaView = item.findViewById(R.id.tvFecha);
            ImageView iconoView = item.findViewById(R.id.ivIcono);

            String descActual = descripcionView.getText().toString();
            String montoStr = montoView.getText().toString().replace("$", "").trim();
            double montoActual = Double.parseDouble(montoStr);
            String fechaActual = fechaView.getText().toString();
            String tipoActual = iconoView.getTag().toString();

            movimientoOriginal = new Movimiento(descActual, tipoActual, montoActual, fechaActual);
            viewSeleccionado = item;

            Intent intent = new Intent(getContext(), EditarMovimientoActivity.class);
            intent.putExtra("movimiento", movimientoOriginal);
            editarMovimientoLauncher.launch(intent);
        });

        listaTransacciones.addView(item);
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

    private void actualizarResumen() {
        tvTotalMonto.setText(String.format(Locale.getDefault(), "$ %.2f", total));
        tvIngresosMonto.setText(String.format(Locale.getDefault(), "$ %.2f", totalIngresos));
        tvGastosMonto.setText(String.format(Locale.getDefault(), "$ %.2f", totalGastos));
    }
}