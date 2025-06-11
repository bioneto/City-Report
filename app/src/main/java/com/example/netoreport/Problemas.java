package com.example.netoreport;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Problemas extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    private Spinner spinnerCategorias;
    private EditText editDescricao;
    private Button btnFoto, btnReportar;
    private ImageView imageViewFoto;
    private Bitmap fotoBitmap;
    private Banco dbHelper;
    private int usuarioId;

    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.problemas_main);

        dbHelper = new Banco(this);
        usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        editDescricao = findViewById(R.id.Campo_Descricao);
        btnFoto = findViewById(R.id.Btn_foto);
        btnReportar = findViewById(R.id.Btn_Reportar);
        imageViewFoto = findViewById(R.id.imageViewFoto);

        carregarCategorias();

        btnFoto.setOnClickListener(v -> verificarPermissaoCamera());
        btnReportar.setOnClickListener(v -> obterLocalizacaoEReportar());
    }

    private void verificarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void obterLocalizacaoEReportar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            obterLocalizacaoAtual();
        }
    }

    private void obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            reportarProblema(); // Tenta reportar sem localização
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                    reportarProblema();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                } else {
                    Toast.makeText(this, "Permissão da câmera negada. Não é possível tirar fotos.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    obterLocalizacaoAtual();
                } else {
                    Toast.makeText(this, "Localização não permitida. Problema será salvo sem localização.", Toast.LENGTH_LONG).show();
                    reportarProblema();
                }
                break;
        }
    }

    private void carregarCategorias() {
        Cursor cursor = dbHelper.getTodasCategorias();
        List<String> categorias = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                categorias.add(cursor.getString(1)); // Nome da categoria
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorias.setAdapter(adapter);
    }

    private void reportarProblema() {
        String categoria = spinnerCategorias.getSelectedItem().toString();
        String descricao = editDescricao.getText().toString().trim();

        if (descricao.isEmpty()) {
            Toast.makeText(this, "Preencha a descrição do problema!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fotoBitmap == null) {
            Toast.makeText(this, "Tire uma foto do problema!", Toast.LENGTH_SHORT).show();
            return;
        }

        int categoriaId = obterIdCategoria(categoria);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        fotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] fotoBytes = stream.toByteArray();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dataHora = sdf.format(new Date());

        long id = dbHelper.cadastrarProblema(
                categoriaId,
                usuarioId,
                descricao,
                fotoBytes,
                latitude,
                longitude,
                dataHora,
                "Pendente"
        );

        if (id != -1) {
            Toast.makeText(this, "Problema reportado com sucesso!", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Erro ao reportar problema!", Toast.LENGTH_SHORT).show();
        }
    }

    private int obterIdCategoria(String nomeCategoria) {
        Cursor cursor = dbHelper.getTodasCategorias();
        int id = 1; // Default

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(1).equals(nomeCategoria)) {
                    id = cursor.getInt(0);
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return id;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            fotoBitmap = (Bitmap) extras.get("data");
            imageViewFoto.setImageBitmap(fotoBitmap);
            imageViewFoto.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}