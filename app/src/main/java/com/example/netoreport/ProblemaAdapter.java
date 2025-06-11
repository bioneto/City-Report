package com.example.netoreport;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProblemaAdapter extends RecyclerView.Adapter<ProblemaAdapter.ProblemaViewHolder> {

    public interface OnStatusChangedListener {
        void onStatusChanged();
    }

    private Context context;
    private List<Problema> problemasList;
    private Banco dbHelper;
    private OnStatusChangedListener statusChangedListener;

    public ProblemaAdapter(Context context, List<Problema> problemasList, Banco dbHelper) {
        this.context = context;
        this.problemasList = problemasList;
        this.dbHelper = dbHelper;
    }

    public void setOnStatusChangedListener(OnStatusChangedListener listener) {
        this.statusChangedListener = listener;
    }

    @NonNull
    @Override
    public ProblemaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_problema, parent, false);
        return new ProblemaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProblemaViewHolder holder, int position) {
        Problema problema = problemasList.get(position);

        // Configurar imagem do problema
        try {
            if (problema.getFoto() != null && problema.getFoto().length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        problema.getFoto(),
                        0,
                        problema.getFoto().length
                );
                if (bitmap != null) {
                    holder.imageProblema.setImageBitmap(bitmap);
                    holder.imageProblema.setVisibility(View.VISIBLE);
                } else {
                    holder.imageProblema.setVisibility(View.GONE);
                }
            } else {
                holder.imageProblema.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            holder.imageProblema.setVisibility(View.GONE);
            e.printStackTrace();
        }

        holder.textCategoria.setText(problema.getCategoriaNome());
        holder.textDescricao.setText(problema.getDescricao());
        holder.textData.setText(problema.getDataHora());
        holder.textStatus.setText(problema.getStatus());

        int color;
        switch (problema.getStatus()) {
            case "Resolvido":
                color = android.graphics.Color.GREEN;
                break;
            case "Em andamento":
                color = android.graphics.Color.BLUE;
                break;
            default:
                color = android.graphics.Color.RED;
                break;
        }
        holder.textStatus.setTextColor(color);

        holder.btnPendente.setOnClickListener(v -> {
            atualizarStatus(problema.getId(), "Pendente", position);
            if (statusChangedListener != null) {
                statusChangedListener.onStatusChanged();
            }
        });

        holder.btnAndamento.setOnClickListener(v -> {
            atualizarStatus(problema.getId(), "Em andamento", position);
            if (statusChangedListener != null) {
                statusChangedListener.onStatusChanged();
            }
        });

        holder.btnConcluido.setOnClickListener(v -> {
            atualizarStatus(problema.getId(), "Resolvido", position);
            if (statusChangedListener != null) {
                statusChangedListener.onStatusChanged();
            }
        });
    }

    private void atualizarStatus(int problemaId, String novoStatus, int position) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Banco.COLUMN_PROBLEMA_STATUS, novoStatus);

        if (novoStatus.equals("Resolvido")) {
            db.delete(Banco.TABLE_PROBLEMAS, Banco.COLUMN_PROBLEMA_ID + " = ?",
                    new String[]{String.valueOf(problemaId)});
            problemasList.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Problema resolvido e removido", Toast.LENGTH_SHORT).show();
        } else {
            db.update(Banco.TABLE_PROBLEMAS, values, Banco.COLUMN_PROBLEMA_ID + " = ?",
                    new String[]{String.valueOf(problemaId)});
            Toast.makeText(context, "Status atualizado para " + novoStatus, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return problemasList.size();
    }

    public static class ProblemaViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProblema;
        TextView textCategoria, textDescricao, textData, textStatus;
        Button btnPendente, btnAndamento, btnConcluido;

        public ProblemaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProblema = itemView.findViewById(R.id.imageProblema);
            imageProblema.setScaleType(ImageView.ScaleType.CENTER_CROP);
            textCategoria = itemView.findViewById(R.id.textCategoria);
            textDescricao = itemView.findViewById(R.id.textDescricao);
            textData = itemView.findViewById(R.id.textData);
            textStatus = itemView.findViewById(R.id.textStatus);
            btnPendente = itemView.findViewById(R.id.btnPendente);
            btnAndamento = itemView.findViewById(R.id.btnAndamento);
            btnConcluido = itemView.findViewById(R.id.btnConcluido);
        }
    }
}