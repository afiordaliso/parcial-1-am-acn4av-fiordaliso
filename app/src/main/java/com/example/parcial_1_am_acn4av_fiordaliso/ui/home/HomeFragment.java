package com.example.parcial_1_am_acn4av_fiordaliso.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Este fragmento dibuja el contenido debajo del status bar
        root.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // Evento en el botón de "+"
        binding.botonAgregar.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Agregar nueva transacción", Toast.LENGTH_SHORT).show();

            // Crear nueva transacción (dinámicamente)
            TextView nuevaTransaccion = new TextView(getContext());
            nuevaTransaccion.setText("⭐ Transferencia recibida\nLunes, 21 Abr. 2025\n+$1.000,00");
            nuevaTransaccion.setPadding(16, 16, 16, 16);
            nuevaTransaccion.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));

            // Agregar al contenedor de transacciones
            binding.listaTransacciones.addView(nuevaTransaccion);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
