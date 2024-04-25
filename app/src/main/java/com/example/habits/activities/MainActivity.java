package com.example.habits.activities;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.habits.BD.GestionBD;
import com.example.habits.R;
import com.example.habits.adapters.TachesAdapter;
import com.example.habits.enums.Mois;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lvTaches;
    public static ArrayList<String> listeJours;
    private TextView txtDate;
    private ImageButton btnMoisPrecedent;
    private ImageButton btnMoisSuivant;
    private ImageButton btnAjouterTache;
    private Calendar calendrier;
    private YearMonth yearMonth;
    public static int nbTaches;
    public static int nbJours;
    public static GestionBD gestionnaireBD;
    public static ArrayList<String> idTaches;

    //Date
    public static int mois;
    public static int annee;

    //BD
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NOM = "Taches.db";

    @SuppressLint({"ResourceType", "MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Créer la BD
        gestionnaireBD = new GestionBD(this);
        //getApplicationContext().deleteDatabase(DATABASE_NOM);
        gestionnaireBD.creerBD();

        //Chercher les boutons
        btnAjouterTache = findViewById(R.id.btnAjouterTache);
        btnMoisPrecedent = findViewById(R.id.btnMoisPrecedent);
        btnMoisSuivant = findViewById(R.id.btnMoisSuivant);

        lvTaches = findViewById(R.id.lvTaches);

        //Chercher le nombre de jours dans le mois ACTUEL
        calendrier = Calendar.getInstance();
        mois = calendrier.get(Calendar.MONTH) + 1;
        annee = calendrier.get(Calendar.YEAR);
        yearMonth = null;

        //Mettre 31 valeur par défaut
        nbJours = 31;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yearMonth = YearMonth.of(annee, mois);
            nbJours = yearMonth.lengthOfMonth();
        }

        listeJours = new ArrayList<>();

        //Afficher la liste des tâches et le nombre de tâches
        nbTaches = gestionnaireBD.getNbTachesMois(String.valueOf(mois), String.valueOf(annee));
        afficherJoursAdapter();

        //Ajouter une tâche
        int finalNbJours = nbJours;
        btnAjouterTache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ajouter une colonne
                gestionnaireBD.ajouterTacheMois(MainActivity.nbJours, String.valueOf(mois), String.valueOf(annee), "");
                nbTaches = gestionnaireBD.getNbTachesMois(String.valueOf(mois), String.valueOf(annee));

                //Recréer l'adapter pour afficher la colonne
                afficherJoursAdapter();

                //Recharger l'activité
                //recreate();
            }
        });

        //Passer au mois précédent
        btnMoisPrecedent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Décrémenter le mois et l'année si on est en janvier
                if (mois == 1) {
                    mois = 12;
                    annee--;
                } else
                    mois--;

                //Réafficher les tâches selon le mois
                nbTaches = gestionnaireBD.getNbTachesMois(String.valueOf(mois), String.valueOf(annee));
                afficherJoursAdapter();
            }
        });

        //Passer au mois suivant
        btnMoisSuivant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Décrémenter le mois et l'année si on est en janvier
                if (mois == 12) {
                    mois = 1;
                    annee++;
                } else
                    mois++;

                //Réafficher les tâches selon le mois
                nbTaches = gestionnaireBD.getNbTachesMois(String.valueOf(mois), String.valueOf(annee));
                afficherJoursAdapter();
            }
        });
    }


    public void afficherJoursAdapter() {
        listeJours.clear();
        //Ajouter une ligne au top pour mettre le titre des habitudes
        listeJours.add("0");

        //Afficher le nb de jours du mois
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yearMonth = YearMonth.of(annee, mois);
            nbJours = yearMonth.lengthOfMonth();
        }

        //Afficher la date
        txtDate = findViewById(R.id.txtDate);
        txtDate.setText(Mois.mois[mois - 1] + " " + annee);

        //Ajouter les jours à la liste des jours
        for(int i = 1; i <= nbJours; i++) {
            listeJours.add(String.valueOf(i));
        }

        //Chercher les composants visuels
        TachesAdapter adapter = new TachesAdapter(this,R.layout.layout_tache, listeJours);
        lvTaches.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}