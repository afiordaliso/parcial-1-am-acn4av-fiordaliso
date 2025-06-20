package com.example.parcial_1_am_acn4av_fiordaliso.ui.splash;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.parcial_1_am_acn4av_fiordaliso.R;

public class SplashFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivPublicidad = view.findViewById(R.id.imagePublicidad);
        ivPublicidad.setImageResource(R.drawable.image_publicidad);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Navigation.findNavController(view).navigate(R.id.action_splash_to_home);
        }, 3000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
