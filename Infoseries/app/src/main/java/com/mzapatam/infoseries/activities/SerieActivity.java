package com.mzapatam.infoseries.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mzapatam.infoseries.R;
import com.mzapatam.infoseries.glide.GlideApp;
import com.mzapatam.infoseries.models.Serie;
import com.mzapatam.infoseries.tools.DatabaseOperations;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SerieActivity extends AppCompatActivity {
    private Serie serie;
    private FirebaseStorage storage;
    private ImageView imagen;
    private TextView nombre;
    private TextView categorias;
    private TextView productora;
    private TextView fecha;
    private TextView temporadas;
    private TextView descripcion;
    private boolean isBookmarked;
    private String username;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference usuariosReference;
    private DatabaseOperations databaseOperations;
    private ImageButton botonDeMarcado;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_serie);
        firebaseDatabase = FirebaseDatabase.getInstance();
        usuariosReference = firebaseDatabase.getReference("usuarios");

        imagen = findViewById(R.id.imagen);
        nombre = findViewById(R.id.nombre);
        categorias = findViewById(R.id.categorias);
        productora = findViewById(R.id.productora);
        fecha = findViewById(R.id.fecha);
        temporadas = findViewById(R.id.temporadas);
        descripcion = findViewById(R.id.descripcion);
        botonDeMarcado = findViewById(R.id.bookmark);
        databaseOperations = new DatabaseOperations();
        isBookmarked = false;

        if (getIntent().hasExtra("username"))
            username = getIntent().getStringExtra("username");

        if (getIntent().hasExtra("Serie")) {
            serie = (Serie) getIntent().getSerializableExtra("Serie");
            storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl(serie.getImagen());
            GlideApp.with(imagen.getContext()).load(storageReference).into(imagen);
            nombre.setText(serie.getNombre());

            String categoriasString = "";
            for (String string: serie.getCategorias().split(":"))
                categoriasString += string + " ";
            categorias.setText("Categorías: " + categoriasString);

            productora.setText(serie.getProductora());

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date date = new Date(serie.getFecha());
            String fechaString = simpleDateFormat.format(date);
            fecha.setText("Fecha de estreno: " + fechaString);

            temporadas.setText("Temporadas: " + Integer.toString(serie.getTemporadas()));
            descripcion.setText("Descripción\n" + serie.getDescripcion());

            Query query = usuariosReference;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                            if (snapshot.child("usuario").getValue().equals(username)) {
                                for (DataSnapshot marcador: snapshot.child("marcadores").getChildren()) {
                                    if (marcador.child("nombre").getValue().equals(serie.getNombre()) && marcador.child("tipo").getValue().equals("Serie")) {
                                        isBookmarked = true;
                                        botonDeMarcado.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
                                    }
                                }
                            }
                        }
                    }
                    botonDeMarcado.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isBookmarked) {
                                botonDeMarcado.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
                                isBookmarked = false;
                                Toast.makeText(SerieActivity.this, "Bookmark deleted", Toast.LENGTH_SHORT).show();
                                databaseOperations.deleteBookmark(username, serie.getNombre(), "Serie");
                            } else {
                                botonDeMarcado.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
                                isBookmarked = true;
                                Toast.makeText(SerieActivity.this, "Bookmark added", Toast.LENGTH_SHORT).show();
                                databaseOperations.addBookmark(username, "Serie", serie.getNombre());
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
