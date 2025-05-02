package com.example.parcial_1_am_acn4av_fiordaliso.ui.home;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.parcial_1_am_acn4av_fiordaliso.R;
import com.example.parcial_1_am_acn4av_fiordaliso.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupFAB();
        setupUI();
        return root;
    }

    private void setupUI() {
        // Inicialización de componentes UI si es necesario
    }

    private void setupFAB() {
        binding.fabMain.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        try {
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

            try {
                Object backgroundHelper = PopupMenu.class.getDeclaredField("mPopup").get(popup);
                backgroundHelper.getClass()
                        .getDeclaredMethod("setForceShowIcon", boolean.class)
                        .invoke(backgroundHelper, true);
            } catch (Exception e) {
            }

            popup.setOnMenuItemClickListener(item -> {
                handleMenuItemClick(item.getItemId());
                return true;
            });

            popup.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMenuItemClick(int menuItemId) {
        String tipo = "";
        String monto = "";
        int icono = 0;
        int color = 0;

        if (menuItemId == R.id.menu_ingreso) {
            tipo = "Ingreso";
            monto = "+$10.000";
            icono = R.drawable.baseline_arrow_upward_24;
            color = R.color.colorIngreso;
        }
        else if (menuItemId == R.id.menu_gasto) {
            tipo = "Gasto";
            monto = "-$2.000";
            icono = R.drawable.baseline_arrow_downward_24;
            color = R.color.colorGasto;
        }
        else if (menuItemId == R.id.menu_transferencia) {
            tipo = "Transferencia";
            monto = "$1.500";
            icono = R.drawable.baseline_close_fullscreen_24;
            color = R.color.colorTransferencia;
        }

        if (!tipo.isEmpty()) {
            agregarTransaccion(tipo, monto, icono, color);
        }
    }


    private void agregarTransaccion(String tipo, String monto, int iconResId, int colorResId) {
        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_transaccion, binding.listaTransacciones, false);

        ImageView ivIcono = itemView.findViewById(R.id.ivIcono);
        TextView tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
        TextView tvMonto = itemView.findViewById(R.id.tvMonto);

        // Configurar icono
        ivIcono.setImageResource(iconResId);
        ivIcono.setColorFilter(ContextCompat.getColor(requireContext(), colorResId));

        // Configurar textos
        tvDescripcion.setText(tipo);
        tvMonto.setText(monto);
        tvMonto.setTextColor(ContextCompat.getColor(requireContext(), colorResId));

        // Agregar a la lista (posición 0 = arriba del todo)
        binding.listaTransacciones.addView(itemView, 0);

        // Scroll automático para ver la nueva transacción
        binding.scrollTransacciones.post(() -> {
            binding.scrollTransacciones.fullScroll(ScrollView.FOCUS_UP);
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}