package com.example.netoreport;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Cadastro extends AppCompatActivity {

    private EditText editNome, editEmail, editSenha, editConfirmarSenha;
    private Button btnCadastrar, btnVoltar;
    private Banco dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cadastro_main);

        dbHelper = new Banco(this);

        editNome = findViewById(R.id.Campo_Cadastrar_Nome);
        editEmail = findViewById(R.id.Campo_Cadastrar_Email);
        editSenha = findViewById(R.id.Campo_Cadastro_Senha);
        editConfirmarSenha = findViewById(R.id.Campo_Verificar_Senha);
        btnCadastrar = findViewById(R.id.Btn_Cadastrar);
        btnVoltar = findViewById(R.id.Btn_voltar_Cadastro);

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });

        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cadastrarUsuario() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.verificarEmail(email)) {
            Toast.makeText(this, "Este email já está cadastrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.cadastrarUsuario(nome, email, senha);

        if (id != -1) {
            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao cadastrar usuário!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}