package com.example.netoreport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private EditText editEmail, editSenha;
    private Button btnLogin, btnCadastro;
    private Banco dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        // Verificar e solicitar permissões
        checkAndRequestPermissions();

        // Inicializar o banco de dados
        dbHelper = new Banco(this);

        // Vincular os elementos da interface
        editEmail = findViewById(R.id.Campo_Email);
        editSenha = findViewById(R.id.Campo_Senha);
        btnLogin = findViewById(R.id.Btn_Login);
        btnCadastro = findViewById(R.id.Btn_Cadastro);

        // Configurar o clique do botão Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fazerLogin();
            }
        });

        // Configurar o clique do botão Cadastro
        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirTelaCadastro();
            }
        });
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this,
                        "Algumas funcionalidades podem não funcionar sem as permissões",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fazerLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (validarCredenciais(email, senha)) {
            Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
            int usuarioId = dbHelper.obterIdUsuarioPorEmail(email);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("USUARIO_ID", usuarioId);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email ou senha incorretos!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validarCredenciais(String email, String senha) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {Banco.COLUMN_USUARIO_ID};
        String selection = Banco.COLUMN_USUARIO_EMAIL + " = ? AND " + Banco.COLUMN_USUARIO_SENHA + " = ?";
        String[] selectionArgs = {email, senha};

        Cursor cursor = db.query(
                Banco.TABLE_USUARIOS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    private void abrirTelaCadastro() {
        Intent intent = new Intent(this, Cadastro.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}