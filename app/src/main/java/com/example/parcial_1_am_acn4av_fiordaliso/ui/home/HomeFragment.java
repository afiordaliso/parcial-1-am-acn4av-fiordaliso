package com.example.parcial_1_am_acn4av_fiordaliso.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
        return root;
    }

    private void setupFAB() {
        binding.fabMain.setOnClickListener(v -> showPopupMenu(v));
    }

    private void showPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        try {
            // Inflar el menú
            popup.getMenuInflater().inflate(R.menu.fab_menu, popup.getMenu());

            // Listener para las opciones del menú
            popup.setOnMenuItemClickListener(item -> {
                handleMenuItemClick(item.getItemId());
                return true;
            });

            popup.show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al mostrar el menú", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void handleMenuItemClick(int menuItemId) {
        if (menuItemId == R.id.menu_ingreso) {
            showToast("Ingreso seleccionado");

        } else if (menuItemId == R.id.menu_gasto) {
            showToast("Gasto seleccionado");

        } else if (menuItemId == R.id.menu_transferencia) {
            showToast("Transferencia seleccionada");

        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}