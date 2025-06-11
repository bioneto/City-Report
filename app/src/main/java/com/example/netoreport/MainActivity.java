package com.example.netoreport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ProblemaAdapter.OnStatusChangedListener {
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    private GoogleMap mMap;
    private RecyclerView recyclerViewProblemas;
    private ProblemaAdapter problemaAdapter;
    private List<Problema> problemasList;
    private Banco dbHelper;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }

        usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);
        dbHelper = new Banco(this);

        Button buttonCadastrar = findViewById(R.id.buttonCadastrar);
        buttonCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Problemas.class);
            intent.putExtra("USUARIO_ID", usuarioId);
            startActivityForResult(intent, 1);
        });

        recyclerViewProblemas = findViewById(R.id.recyclerViewProblemas);
        recyclerViewProblemas.setLayoutManager(new LinearLayoutManager(this));
        problemasList = new ArrayList<>();
        problemaAdapter = new ProblemaAdapter(this, problemasList, dbHelper);
        problemaAdapter.setOnStatusChangedListener(this);
        recyclerViewProblemas.setAdapter(problemaAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        carregarProblemas();
    }

    private void carregarProblemas() {
        problemasList.clear();
        Cursor cursor = dbHelper.getTodosProblemasComCategoriaEFoto();

        if (cursor.moveToFirst()) {
            do {
                Problema problema = new Problema();
                problema.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_ID)));
                problema.setCategoriaId(cursor.getInt(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_CATEGORIA_ID)));
                problema.setUsuarioId(cursor.getInt(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_USUARIO_ID)));
                problema.setDescricao(cursor.getString(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_DESCRICAO)));
                problema.setFoto(cursor.getBlob(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_FOTO)));
                problema.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_LATITUDE)));
                problema.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_LONGITUDE)));
                problema.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_DATA_HORA)));
                problema.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(Banco.COLUMN_PROBLEMA_STATUS)));
                problema.setCategoriaNome(cursor.getString(cursor.getColumnIndexOrThrow(Banco.COLUMN_CATEGORIA_NOME)));

                problemasList.add(problema);
            } while (cursor.moveToNext());
        }
        cursor.close();
        problemaAdapter.notifyDataSetChanged();
        atualizarMapa();
    }

    private void atualizarMapa() {
        if (mMap != null) {
            mMap.clear();

            for (Problema problema : problemasList) {
                if (problema.getLatitude() != 0 && problema.getLongitude() != 0) {
                    LatLng local = new LatLng(problema.getLatitude(), problema.getLongitude());

                    float cor = BitmapDescriptorFactory.HUE_RED;
                    if ("Resolvido".equals(problema.getStatus())) {
                        cor = BitmapDescriptorFactory.HUE_GREEN;
                    } else if ("Em andamento".equals(problema.getStatus())) {
                        cor = BitmapDescriptorFactory.HUE_BLUE;
                    }

                    mMap.addMarker(new MarkerOptions()
                            .position(local)
                            .title(problema.getCategoriaNome())
                            .snippet(problema.getDescricao())
                            .icon(BitmapDescriptorFactory.defaultMarker(cor)));
                }
            }

            if (!problemasList.isEmpty() && problemasList.get(0).getLatitude() != 0) {
                LatLng primeiroLocal = new LatLng(
                        problemasList.get(0).getLatitude(),
                        problemasList.get(0).getLongitude()
                );
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(primeiroLocal, 15));
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        atualizarMapa();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            carregarProblemas();
        }
    }

    @Override
    public void onStatusChanged() {
        carregarProblemas();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Notificações desativadas", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}