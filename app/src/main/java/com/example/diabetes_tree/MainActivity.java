package com.example.diabetes_tree;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    EditText editGlukosa, editTDarah, editBMI, editUmur;
    TextView textHasil, hGlukosa, hTDarah, hBmi, hUmur;
    Button btnCek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editGlukosa = findViewById(R.id.editGlukosa);
        editTDarah = findViewById(R.id.editTDarah);
        editBMI = findViewById(R.id.editBMI);
        editUmur = findViewById(R.id.editUmur);

        hGlukosa = findViewById(R.id.hasilglukosa);
        hTDarah = findViewById(R.id.hasiltdarah);
        hBmi = findViewById(R.id.hasilbmi);
        hUmur = findViewById(R.id.hasilumur);

        textHasil = findViewById(R.id.hasil);
        btnCek = findViewById(R.id.buttonCek);

        btnCek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new NetworkTask().execute();
            }
        });

    }
    private class NetworkTask extends AsyncTask<Void, Void, String> {
        private String hasil1, hasil2, hasil3, hasil4;
        @Override
        protected String doInBackground(Void... params) {
            try {
                //Flask server
                URL url = new URL("http://192.168.1.83:5000/predict_diabetes");

                // Buka SV
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");

                String textGlukosa = editGlukosa.getText().toString();
                String textTDarah = editTDarah.getText().toString();
                String textBMI = editBMI.getText().toString();
                String textUmur = editUmur.getText().toString();

                double glukosa = Double.parseDouble(textGlukosa);
                double tdarah = Double.parseDouble(textTDarah);
                double bmi = Double.parseDouble(textBMI);
                double umur = Double.parseDouble(textUmur);

                /*glukosa_mapping = {'Baik': 1, 'Buruk': 2, 'Normal': 3, 'Sedang': 4}
                blood_pressure_mapping = {'Normal': 1, 'Prahipertensi': 2, 'Hipertensi1': 3,
                'Hipertensi2': 4, 'Krisis': 5}
                bmi_mapping = {'Normal': 1, 'Obesitas': 2, 'Kurang': 3}
                age_mapping = {'Dewasa': 0, 'Lansia': 1}
                diabetes_mapping = {'Tidak': 0, 'Ya': 1}*/

                int v1 = 0, v2 = 0, v3 = 0, v4 = 0;

                if (glukosa <= 140){
                    v1 = 0;
                    hasil1 = "Baik";
                } else if (glukosa >140 && glukosa < 200) {
                    v1 = 2;
                    hasil1 = "Sedang";
                } else if (glukosa > 200) {
                    v1 = 1;
                    hasil1 = "Buruk";
                }

                if (tdarah <= 80){
                    v2 = 3;
                    hasil2 = "Normal";
                } else if (tdarah > 80 && tdarah < 90) {
                    v2 = 4;
                    hasil2 = "Pra-Hipertensi";
                } else if (tdarah >= 90 && tdarah < 100) {
                    v2 = 0;
                    hasil2 = "Hipertensi-1";
                } else if (tdarah >= 100 && tdarah < 120) {
                    v2 = 1;
                    hasil2 = "Hipertensi-2";
                } else if (tdarah >= 120) {
                    v2 = 2;
                    hasil2 = "Krisis";
                }

                if (bmi <= 18.5){
                    v3 = 0;
                    hasil3 = "Kurang";
                } else if (bmi > 18.5 && bmi < 30) {
                    v3 = 1;
                    hasil3 = "Normal";
                } else if (bmi >= 30) {
                    v3 = 2;
                    hasil3 = "Obesitas";
                }

                if (umur >= 21 && umur <= 59){
                    v4 = 0;
                    hasil4 = "Dewasa";
                } else if (umur > 59) {
                    v4 = 1;
                    hasil4 = "Lansia";
                }

                // Prepare data
                JSONObject inputData = new JSONObject();
                inputData.put("new_data", new JSONArray(new int[]{v1, v2, v3, v4}));

                // Send data
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] inputBytes = inputData.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(inputBytes, 0, inputBytes.length);
                }
                Log.d("NetworkTask", "Request Sent: " + inputData.toString());

                // Read data
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Proses Respon
                    try (Scanner scanner = new Scanner(connection.getInputStream())) {
                        String response = scanner.useDelimiter("\\A").next();
                        // Ubah JSON jadi String
                        JSONObject jsonResponse = new JSONObject(response);
                        String prediction = jsonResponse.getString("prediction");
                        Log.d("NetworkTask", "Response Received: " + response);
                        return prediction;
                    }
                } else {
                    Log.e("NetworkTask", "Error Response Code: " + connection.getResponseCode());
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("NetworkTask", "Exception: " + e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null && !result.isEmpty()) {
                textHasil.setText(": " + result);
                hGlukosa.setText(": "+ hasil1);
                hTDarah.setText(": " + hasil2);
                hBmi.setText(": " + hasil3);
                hUmur.setText(": " + hasil4);
            } else {
                textHasil.setText("Diabetes: " + result);
                Toast.makeText(MainActivity.this, "Error processing result", Toast.LENGTH_SHORT).show();
            }
        }
    }
}