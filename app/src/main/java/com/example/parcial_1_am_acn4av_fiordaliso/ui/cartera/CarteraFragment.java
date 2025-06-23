package com.example.parcial_1_am_acn4av_fiordaliso.ui.cartera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.parcial_1_am_acn4av_fiordaliso.EditarTicketActivity;
import com.example.parcial_1_am_acn4av_fiordaliso.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class CarteraFragment extends Fragment {

    private LinearLayout listaTickets;
    private Button btnAgregarTicket;
    private final String MARKETSTACK_API_KEY = "f02ca1e024127225efec3b5b1cac2621"; // Reemplazá con tu clave real

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_carteras, container, false);

        btnAgregarTicket = view.findViewById(R.id.btnAgregarTicket);
        listaTickets = view.findViewById(R.id.listaTickets);

        btnAgregarTicket.setOnClickListener(v -> mostrarDialogoSeleccionarTicker());

        cargarTicketsDesdeFirestore();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            cargarTicketsDesdeFirestore();
        }
    }


    private void mostrarDialogoSeleccionarTicker() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_agregar_ticket, null);

        Spinner spinnerTickers = dialogView.findViewById(R.id.spinnerTickers);
        EditText etCantidad = dialogView.findViewById(R.id.etCantidad);

        List<String> tickers = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, tickers);
        spinnerTickers.setAdapter(adapter);

        cargarTickersDesdeMarketstack(tickers, adapter);

        new AlertDialog.Builder(requireContext())
                .setTitle("Nuevo Ticket")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String ticker = spinnerTickers.getSelectedItem().toString();
                    String cantidadStr = etCantidad.getText().toString().trim();
                    if (!cantidadStr.isEmpty()) {
                        int cantidad = Integer.parseInt(cantidadStr);
                        obtenerPrecioDesdeMarketstack(ticker, (precio, cambio) -> {
                            guardarTicketEnFirestore(ticker, cantidad, precio, cambio);
                        });
                    } else {
                        Toast.makeText(getContext(), "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cargarTickersDesdeMarketstack(List<String> tickers, ArrayAdapter<String> adapter) {
        new Thread(() -> {
            try {
                String urlStr = "https://api.marketstack.com/v1/tickers?access_key=" + MARKETSTACK_API_KEY + "&limit=100";
                HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
                con.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject root = new JSONObject(response.toString());
                JSONArray data = root.getJSONArray("data");

                List<String> nuevosTickers = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    String symbol = obj.getString("symbol");
                    nuevosTickers.add(symbol);
                }

                requireActivity().runOnUiThread(() -> {
                    tickers.clear();
                    tickers.addAll(nuevosTickers);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al cargar tickers", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private interface PrecioCallback {
        void onPrecioListo(double precio, String cambio);
    }

    private void obtenerPrecioDesdeMarketstack(String ticker, PrecioCallback callback) {
        new Thread(() -> {
            try {
                String urlStr = "https://api.marketstack.com/v1/eod?access_key=" + MARKETSTACK_API_KEY + "&symbols=" + ticker;
                HttpURLConnection con = (HttpURLConnection) new URL(urlStr).openConnection();
                con.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject root = new JSONObject(response.toString());

                if (root.has("error")) {
                    String mensaje = root.getJSONObject("error").getString("message");
                    throw new Exception("API error: " + mensaje);
                }

                JSONArray data = root.getJSONArray("data");
                if (data.length() == 0) {
                    throw new Exception("No hay datos disponibles para el ticker: " + ticker);
                }

                JSONObject info = data.getJSONObject(0);
                double close = info.getDouble("close");
                double open = info.getDouble("open");
                double variacion = ((close - open) / open) * 100;
                String cambio = String.format("%.2f%%", variacion);

                requireActivity().runOnUiThread(() -> callback.onPrecioListo(close, cambio));

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al obtener precio: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }
    private void guardarTicketEnFirestore(String ticker, int cantidad, double precio, String variacion) {
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("ticker", ticker);
        ticket.put("cantidad", cantidad);
        ticket.put("precioActual", precio);
        ticket.put("variacion", variacion);
        ticket.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseFirestore.getInstance()
                .collection("tickets")
                .add(ticket)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(getContext(), "Ticket guardado", Toast.LENGTH_SHORT).show();
                    cargarTicketsDesdeFirestore();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar ticket", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarTicketsDesdeFirestore() {
        listaTickets.removeAllViews();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("tickets")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    LayoutInflater inflater = LayoutInflater.from(requireContext());

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String ticker = doc.getString("ticker") != null ? doc.getString("ticker") : "—";
                        long cantidad = doc.getLong("cantidad") != null ? doc.getLong("cantidad") : 0;
                        double precio = doc.getDouble("precioActual") != null ? doc.getDouble("precioActual") : 0;
                        String cambioRaw = doc.getString("variacion") != null ? doc.getString("variacion") : "0.00";

                        String cambio = cambioRaw.endsWith("%") ? cambioRaw : cambioRaw + "%";
                        double valorTotal = precio * cantidad;

                        View card = inflater.inflate(R.layout.item_ticket, listaTickets, false);

                        TextView tvTickerCantidad = card.findViewById(R.id.tvTickerCantidad);
                        TextView tvVariacion = card.findViewById(R.id.tvVariacion);
                        TextView tvValor = card.findViewById(R.id.tvValor);

                        tvTickerCantidad.setText(ticker + " (x" + cantidad + ")");

                        // Variación con color solo en el número
                        String textoVariacion = "Variación: ";
                        SpannableString spannable = new SpannableString(textoVariacion + cambio);
                        int color = cambio.startsWith("-") ? 0xFFE53935 : 0xFF43A047;
                        spannable.setSpan(
                                new ForegroundColorSpan(color),
                                textoVariacion.length(),
                                textoVariacion.length() + cambio.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        );
                        tvVariacion.setText(spannable);

                        // Formateo elegante con separador de miles
                        tvValor.setText(String.format("$%,.2f", valorTotal));
                        tvValor.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

                        card.setOnClickListener(v -> {
                            Intent intent = new Intent(requireContext(), EditarTicketActivity.class);
                            intent.putExtra("docId", doc.getId());
                            intent.putExtra("ticker", ticker);
                            intent.putExtra("cantidad", cantidad);
                            intent.putExtra("precioActual", precio);
                            intent.putExtra("variacion", cambio);
                            startActivityForResult(intent, 123);
                        });

                        listaTickets.addView(card);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar tickets", Toast.LENGTH_SHORT).show()
                );
    }
}