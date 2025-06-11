package com.example.netoreport;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Banco extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "netoreport.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_USUARIO_ID = "id";
    public static final String COLUMN_USUARIO_NOME = "nome";
    public static final String COLUMN_USUARIO_EMAIL = "email";
    public static final String COLUMN_USUARIO_SENHA = "senha";

    public static final String TABLE_CATEGORIAS = "categorias";
    public static final String COLUMN_CATEGORIA_ID = "id";
    public static final String COLUMN_CATEGORIA_NOME = "nome";
    public static final String COLUMN_CATEGORIA_DESCRICAO = "descricao";

    public static final String TABLE_PROBLEMAS = "problemas";
    public static final String COLUMN_PROBLEMA_ID = "id";
    public static final String COLUMN_PROBLEMA_CATEGORIA_ID = "categoria_id";
    public static final String COLUMN_PROBLEMA_USUARIO_ID = "usuario_id";
    public static final String COLUMN_PROBLEMA_DESCRICAO = "descricao";
    public static final String COLUMN_PROBLEMA_FOTO = "foto";
    public static final String COLUMN_PROBLEMA_LATITUDE = "latitude";
    public static final String COLUMN_PROBLEMA_LONGITUDE = "longitude";
    public static final String COLUMN_PROBLEMA_DATA_HORA = "data_hora";
    public static final String COLUMN_PROBLEMA_STATUS = "status";

    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + "(" +
                    COLUMN_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USUARIO_NOME + " TEXT NOT NULL, " +
                    COLUMN_USUARIO_EMAIL + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_USUARIO_SENHA + " TEXT NOT NULL" +
                    ")";

    private static final String CREATE_TABLE_CATEGORIAS =
            "CREATE TABLE " + TABLE_CATEGORIAS + "(" +
                    COLUMN_CATEGORIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORIA_NOME + " TEXT NOT NULL, " +
                    COLUMN_CATEGORIA_DESCRICAO + " TEXT" +
                    ")";

    private static final String CREATE_TABLE_PROBLEMAS =
            "CREATE TABLE " + TABLE_PROBLEMAS + "(" +
                    COLUMN_PROBLEMA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PROBLEMA_CATEGORIA_ID + " INTEGER NOT NULL, " +
                    COLUMN_PROBLEMA_USUARIO_ID + " INTEGER NOT NULL, " +
                    COLUMN_PROBLEMA_DESCRICAO + " TEXT NOT NULL, " +
                    COLUMN_PROBLEMA_FOTO + " BLOB, " +
                    COLUMN_PROBLEMA_LATITUDE + " REAL, " +
                    COLUMN_PROBLEMA_LONGITUDE + " REAL, " +
                    COLUMN_PROBLEMA_DATA_HORA + " TEXT NOT NULL, " +
                    COLUMN_PROBLEMA_STATUS + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COLUMN_PROBLEMA_CATEGORIA_ID + ") REFERENCES " +
                    TABLE_CATEGORIAS + "(" + COLUMN_CATEGORIA_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_PROBLEMA_USUARIO_ID + ") REFERENCES " +
                    TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + ")" +
                    ")";

    public Banco(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIOS);
        db.execSQL(CREATE_TABLE_CATEGORIAS);
        db.execSQL(CREATE_TABLE_PROBLEMAS);

        inserirCategoriasIniciais(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROBLEMAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }

    private void inserirCategoriasIniciais(SQLiteDatabase db) {
        String[][] categorias = {
                {"Iluminação Pública", "Problemas com postes de luz e iluminação"},
                {"Buraco na Via", "Buraco em ruas e avenidas"},
                {"Coleta de Lixo", "Problemas com coleta de resíduos"},
                {"Água e Esgoto", "Problemas com abastecimento de água ou esgoto"}
        };

        ContentValues values = new ContentValues();
        for (String[] categoria : categorias) {
            values.clear();
            values.put(COLUMN_CATEGORIA_NOME, categoria[0]);
            values.put(COLUMN_CATEGORIA_DESCRICAO, categoria[1]);
            db.insert(TABLE_CATEGORIAS, null, values);
        }
    }

    public long cadastrarUsuario(String nome, String email, String senha) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USUARIO_NOME, nome);
        values.put(COLUMN_USUARIO_EMAIL, email);
        values.put(COLUMN_USUARIO_SENHA, senha);

        long id = db.insert(TABLE_USUARIOS, null, values);
        db.close();
        return id;
    }

    public boolean verificarEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USUARIOS + " WHERE " + COLUMN_USUARIO_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    public long cadastrarProblema(int categoriaId, int usuarioId, String descricao,
                                  byte[] foto, double latitude, double longitude,
                                  String dataHora, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PROBLEMA_CATEGORIA_ID, categoriaId);
        values.put(COLUMN_PROBLEMA_USUARIO_ID, usuarioId);
        values.put(COLUMN_PROBLEMA_DESCRICAO, descricao);
        values.put(COLUMN_PROBLEMA_FOTO, foto);
        values.put(COLUMN_PROBLEMA_LATITUDE, latitude);
        values.put(COLUMN_PROBLEMA_LONGITUDE, longitude);
        values.put(COLUMN_PROBLEMA_DATA_HORA, dataHora);
        values.put(COLUMN_PROBLEMA_STATUS, status);

        long id = db.insert(TABLE_PROBLEMAS, null, values);
        db.close();
        return id;
    }


    public Cursor getTodasCategorias() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIAS,
                new String[]{COLUMN_CATEGORIA_ID, COLUMN_CATEGORIA_NOME, COLUMN_CATEGORIA_DESCRICAO},
                null, null, null, null, COLUMN_CATEGORIA_NOME + " ASC");
    }
    public int obterIdUsuarioPorEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USUARIOS,
                new String[]{COLUMN_USUARIO_ID},
                COLUMN_USUARIO_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public Cursor getTodosProblemasComCategoriaEFoto() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p.*, c." + COLUMN_CATEGORIA_NOME +
                " FROM " + TABLE_PROBLEMAS + " p" +
                " INNER JOIN " + TABLE_CATEGORIAS + " c ON p." + COLUMN_PROBLEMA_CATEGORIA_ID + " = c." + COLUMN_CATEGORIA_ID;
        return db.rawQuery(query, null);
    }

}