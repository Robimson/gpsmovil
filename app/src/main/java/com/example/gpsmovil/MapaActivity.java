package com.example.gpsmovil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mapa;
    private Marker marker;
    private DatabaseReference coordRef;
    private TextView tLat, tLon;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    private boolean esPrimeraVez = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        tLat = findViewById(R.id.txtLatitud);
        tLon = findViewById(R.id.txtLongitud);

        coordRef = FirebaseDatabase.getInstance().getReference("coordenadas");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        iniciarRastreoGPS();
    }

    private void iniciarRastreoGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location loc : locationResult.getLocations()) {
                    if (loc != null) {
                        // Enviar coordenadas actuales a Firebase
                        coordRef.child("latitud").setValue(loc.getLatitude());
                        coordRef.child("longitud").setValue(loc.getLongitude());
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, getMainLooper());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;

        mapa.getUiSettings().setZoomControlsEnabled(true);

        coordRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if(s.exists() && s.hasChild("latitud") && s.hasChild("longitud")){
                    double lat = Double.parseDouble(s.child("latitud").getValue().toString());
                    double lon = Double.parseDouble(s.child("longitud").getValue().toString());

                    tLat.setText(String.format("Lat: %.5f", lat));
                    tLon.setText(String.format("Lon: %.5f", lon));

                    LatLng ubicacion = new LatLng(lat, lon);

                    if(marker == null) {
                        marker = mapa.addMarker(new MarkerOptions().position(ubicacion).title("Ubicación RealTime"));
                    } else {
                        marker.setPosition(ubicacion);
                    }

                    if (esPrimeraVez) {
                        mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));
                        esPrimeraVez = false;
                    } else {
                        mapa.animateCamera(CameraUpdateFactory.newLatLng(ubicacion));
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            iniciarRastreoGPS();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}