package com.example.gpsmovil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference HumedadRef, PresionRef, VelocidadRef, TemperaturaRef;
    TextView txtT, txtH, txtP, txtV;
    EditText editT, editH;
    Button btnT, btnH, btnIrMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtT = findViewById(R.id.valor_Temperatura);
        txtH = findViewById(R.id.valor_Humedad);
        txtP = findViewById(R.id.valor_Presion);
        txtV = findViewById(R.id.valor_Velocidad);
        editT = findViewById(R.id.setvalor_Temperatura);
        editH = findViewById(R.id.setvalor_Humedad);
        btnT = findViewById(R.id.clickBotonTemperatura);
        btnH = findViewById(R.id.clickBotonHumedad);
        btnIrMapa = findViewById(R.id.btnIrMapa);

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        TemperaturaRef = db.getReference("sensores/temperatura");
        HumedadRef = db.getReference("sensores/humedad");
        PresionRef = db.getReference("sensores/presion");
        VelocidadRef = db.getReference("sensores/velocidad");

        TemperaturaRef.addValueEventListener(setListener(txtT, " °C"));
        HumedadRef.addValueEventListener(setListener(txtH, " %"));
        PresionRef.addValueEventListener(setListener(txtP, " hPa"));
        VelocidadRef.addValueEventListener(setListener(txtV, " km/h"));

        btnT.setOnClickListener(v -> {
            String val = editT.getText().toString();
            if (!val.isEmpty()) {
                TemperaturaRef.setValue(Double.parseDouble(val));
                editT.setText("");
            }
        });

        btnH.setOnClickListener(v -> {
            String val = editH.getText().toString();
            if (!val.isEmpty()) {
                HumedadRef.setValue(Double.parseDouble(val));
                editH.setText("");
            }
        });

        btnIrMapa.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MapaActivity.class));
        });
    }

    public ValueEventListener setListener(TextView t, String u) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if (s.exists()) {
                    t.setText(s.getValue().toString() + u);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }
}