package com.example.habits.adapters;

import static com.example.habits.activities.MainActivity.annee;
import static com.example.habits.activities.MainActivity.gestionnaireBD;
import static com.example.habits.activities.MainActivity.mois;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.habits.BD.GestionBD;
import com.example.habits.R;
import com.example.habits.activities.MainActivity;
import com.example.habits.utils.Tache;

import java.util.ArrayList;
import java.util.List;

public class TachesAdapter extends ArrayAdapter<String> {
    private List<String> listeJours;
    private Context contexte;
    private int resource;
    private Resources ressources;
    public ArrayList<Tache> listeTachesMois;

    public TachesAdapter(@NonNull Context context, int resource, @NonNull List<String> listeTaches) {
        super(context, resource, listeTaches);
        this.contexte = context;
        this.resource = resource;
        this.listeJours = listeTaches;
        this.ressources = context.getResources();

        //Chercher la liste des tâches
        listeTachesMois = gestionnaireBD.getTachesMois(mois, annee);
    }

    @Override
    public int getCount() {
        return this.listeJours.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(contexte);
            convertView = inflater.inflate(resource, parent, false);
        }

        //Chercher le jour
        final String jour = this.listeJours.get(position);

        //Chercher le layout
        LinearLayout layoutTachesJour = convertView.findViewById(R.id.layoutTachesJour);
        layoutTachesJour.removeAllViews();

        //Créer les boutons selon le nb de taches
        for (int i = 0; i < MainActivity.nbTaches + 1; i++) {
            //Chercher les couleurs
            int vert = getContext().getResources().getColor(R.color.vert, null);
            int rouge = getContext().getResources().getColor(R.color.rouge, null);

            //Chercher ID de la tâche si on est dans une colonne de tâche
            int idTache;

            //La première ligne sert à afficher la description des tâches
            if(jour.equals("0") && i != 0) {
                ImageButton btnInfoTache = new ImageButton(getContext());
                btnInfoTache.setBackgroundColor(Color.WHITE);
                layoutTachesJour.addView(btnInfoTache);
                btnInfoTache.setImageResource(R.drawable.description);

                idTache = listeTachesMois.get(i - 1).id;

                //Ajouter l'écouteur d'événement sur les boutons pour quand on afficher le titre de la tâche
                btnInfoTache.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Afficher une popup pour les détails de la tâche
                        ouvrirMenu(idTache, position);
                    }
                });

                //Mettre la taille des boutons
                ViewGroup.LayoutParams params;
                params = btnInfoTache.getLayoutParams();
                params.width = 130;
                params.height = 130;

            }

            //Le reste des lignes sont les jours du mois et les tâches
            else {
                Button btnTache = new Button(getContext());
                layoutTachesJour.addView(btnTache);

                //Le premier bouton sert à indiquer la date
                if(i == 0) {
                    //Si ce n'est pas la 1ère ligne, on affiche le jour
                    if(!jour.equals("0")) {
                        btnTache.setText(jour);
                        btnTache.setTextColor(Color.WHITE);
                    }

                    //Chercher la police
                    btnTache.setTypeface(ResourcesCompat.getFont(getContext(), R.font.montserrat_bold));

                    //Désactiver le clic sur le bouton
                    btnTache.setBackgroundColor(Color.BLACK);
                    btnTache.setEnabled(false);
                }

                //Sinon, c'est un bouton de tâche
                else {
                    //CHERCHER SI LA TACHE EST FAITE OU PAS
                    idTache = listeTachesMois.get(i - 1).id;
                    int etat = gestionnaireBD.etatTache(jour, idTache);

                    //Mettre les couleurs pour chaque tâche selon l'état enregistré dans la BD
                    if(etat == 0)
                        btnTache.setBackgroundColor(rouge);
                    else
                        btnTache.setBackgroundColor(vert);

                    //Ajouter l'écouteur d'événement sur les boutons pour quand on veut changer la tâche
                    int finalIdTache = idTache;
                    btnTache.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            //Chercher la couleur
                            Drawable background = btnTache.getBackground();
                            if (background instanceof ColorDrawable) {
                                int color = ((ColorDrawable) background).getColor();

                                //Si la couleur est rouge, on la met verte et vice versa
                                if (color == vert) {
                                    btnTache.setBackgroundColor(rouge);
                                    MainActivity.gestionnaireBD.modifierEtatTache(finalIdTache, jour, false);
                                } else {
                                    btnTache.setBackgroundColor(vert);
                                    MainActivity.gestionnaireBD.modifierEtatTache(finalIdTache, jour, true);
                                }
                            }
                        }
                    });
                }

                //Mettre la taille des boutons
                ViewGroup.LayoutParams params;
                params = btnTache.getLayoutParams();
                params.width = 130;
                params.height = 130;
            }
        }

        return convertView;
    }

    public void ouvrirMenu(int idTache, int position) {
        //Créer le menu
        Dialog popupMenu = new Dialog(getContext());

        popupMenu.setContentView(R.layout.layout_info_tache);
        popupMenu.show();

        //Chercher les boutons DANS la popup
        EditText inputTache = popupMenu.findViewById(R.id.inputModifTache);
        TextView tvTitreTache = popupMenu.findViewById(R.id.titreTache);
        Button btnModifTache = popupMenu.findViewById(R.id.btnModifTache);
        Button btnSupprimerTache = popupMenu.findViewById(R.id.btnSupprimerTache);

        //Afficher le titre de la tâche selon ce qu'il y a dans la BD
        String titre = gestionnaireBD.getTitreTache(idTache);
        tvTitreTache.setText(titre);

        //Modifier le titre de la tâche
        btnModifTache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Afficher nouveau titre
                String nouveauTitre = inputTache.getText().toString();
                tvTitreTache.setText(nouveauTitre);

                //Vider texte
                inputTache.setText("");

                //METTRE LA TÂCHE DANS LA BD...
                gestionnaireBD.setTitreTache(idTache, nouveauTitre);
            }
        });

        //Supprimer la tâche
        btnSupprimerTache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gestionnaireBD.supprimerTache(idTache);

                notifyDataSetChanged();
            }
        });
    }
}
