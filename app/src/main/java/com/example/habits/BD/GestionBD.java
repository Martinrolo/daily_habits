package com.example.habits.BD;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.habits.activities.MainActivity;
import com.example.habits.utils.Tache;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class GestionBD extends SQLiteOpenHelper {
    //Données de la BD
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NOM = "Taches.db";
    private static final String TABLE_TACHE = "Tache";
    private static final String COLONNE_ID_TACHE = "id";
    private static final String COLONNE_MOIS = "mois";
    private static final String COLONNE_ANNEE = "annee";
    private static final String COLONNE_NOM_TACHE = "nomTache";

    private static final String TABLE_TACHE_JOUR = "TacheJour";
    private static final String COLONNE_ID_TACHE_JOUR = "id";
    private static final String COLONNE_REF_ID_TACHE = "idTache";
    private static final String COLONNE_JOUR = "jour";
    private static final String COLONNE_DONE = "done";

    //Créer instance de gestion de BD
    public GestionBD(Context context) {
        super(context, DATABASE_NOM, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.e("ON CRÉE LA BD", "ON CRÉE LA BD");

        // Création de la table de tâche
        String query = "CREATE TABLE " + TABLE_TACHE + "(" +
                COLONNE_ID_TACHE + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLONNE_MOIS + " TEXT, " +
                COLONNE_ANNEE + " TEXT, " +
                COLONNE_NOM_TACHE + " TEXT);";
        db.execSQL(query);

        query = "CREATE TABLE " + TABLE_TACHE_JOUR + "(" +
                COLONNE_ID_TACHE_JOUR + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLONNE_JOUR + " TEXT, " +
                COLONNE_DONE + " BOOLEAN, " +
                COLONNE_REF_ID_TACHE + " INTEGER, " + "FOREIGN KEY (" +
                COLONNE_ID_TACHE + ") REFERENCES " + TABLE_TACHE + "(" + COLONNE_ID_TACHE + "));";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // détruire la BD si elle existe
        String query = "DROP TABLE IF EXISTS " + DATABASE_NOM;
        db.execSQL(query);
        // recréer la BD
        onCreate(db);
    }

    public void creerBD() {
        SQLiteDatabase db = getWritableDatabase();
    }

    @SuppressLint("Range")
    public int getNbTachesMois(String mois, String annee) {
        int nbTaches = 0;

        //Faire la requete
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT COUNT(*) FROM "+ TABLE_TACHE + " WHERE " + COLONNE_MOIS + "=" +
                mois + " AND " + COLONNE_ANNEE + "=" + annee + ";";
        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){
            nbTaches = cursor.getInt(cursor.getColumnIndex("COUNT(*)"));
        }

        cursor.close();
        return nbTaches;
    }

    @SuppressLint("Range")
    public ArrayList<Tache> getTachesMois(int mois, int annee) {
        ArrayList<Tache> listeTaches = new ArrayList<>();

        String nom;
        int id;

        //Faire la requete
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COLONNE_ID_TACHE + ", " + COLONNE_NOM_TACHE + " FROM " +
                TABLE_TACHE + " WHERE " + COLONNE_MOIS + " = " + mois + " AND " + COLONNE_ANNEE +
                " = " + annee + ";";
        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndex(COLONNE_ID_TACHE));
            nom = cursor.getString(cursor.getColumnIndex(COLONNE_NOM_TACHE));

            listeTaches.add(new Tache(nom, id, mois, annee));
        }

        cursor.close();
        return listeTaches;
    }

    @SuppressLint("Range")
    public void ajouterTacheMois(int nbJours, String mois, String annee, String nomTache) {
        ContentValues valeurs = new ContentValues();

        //Créer la tâche
        valeurs.put(COLONNE_MOIS, mois);
        valeurs.put(COLONNE_ANNEE, annee);
        valeurs.put(COLONNE_NOM_TACHE, nomTache);

        //L'ajouter à la BD
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_TACHE, null, valeurs);

        //Chercher l'ID de la tâche
        int idTacheMois = 0;

        String query = "SELECT " + COLONNE_ID_TACHE + " FROM " + TABLE_TACHE;
        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){
            idTacheMois = cursor.getInt(cursor.getColumnIndex(COLONNE_ID_TACHE));
        }

        //Ajouter chaque jour de la tâche
        ContentValues valeursJour = new ContentValues();

        for (int i = 0; i < nbJours; i++) {
            //Créer la tâche
            valeursJour.put(COLONNE_JOUR, i+1);
            valeursJour.put(COLONNE_REF_ID_TACHE, idTacheMois);
            valeursJour.put(COLONNE_DONE, "false");

            //L'ajouter à la BD
            db.insert(TABLE_TACHE_JOUR, null, valeursJour);
        }
    }

    public void modifierEtatTache(int idTache, String jour, Boolean nouvelEtat) {
        SQLiteDatabase db = getWritableDatabase();

        String query = "UPDATE " + TABLE_TACHE_JOUR + " SET " + COLONNE_DONE + " = " + nouvelEtat +
                " WHERE " + COLONNE_REF_ID_TACHE + " = " + idTache + " AND " + COLONNE_JOUR + "=" + jour + ";";

        db.execSQL(query);
    }

    @SuppressLint("Range")
    public int etatTache(String jour, int idTache) {
        int reponse = 0;

        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT " + COLONNE_DONE + " FROM " + TABLE_TACHE_JOUR + " WHERE " +
                COLONNE_REF_ID_TACHE + " = " + idTache + " AND " + COLONNE_JOUR + " = " + jour + ";";

        Cursor cursor = db.rawQuery(query,null);

        while (cursor.moveToNext()){
                reponse = cursor.getInt(cursor.getColumnIndex(COLONNE_DONE));
        }

        return reponse;
    }


    @SuppressLint("Range")
    public String getTitreTache(int idTache) {
        String titre = "";

        SQLiteDatabase db = getWritableDatabase();

        //Chercher le nom
        String query = "SELECT " + COLONNE_NOM_TACHE + " FROM " + TABLE_TACHE + " WHERE " +
                COLONNE_ID_TACHE + " = " + idTache + ";";

        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            titre = cursor.getString(cursor.getColumnIndex(COLONNE_NOM_TACHE));
        }

        return titre;
    }

    public void setTitreTache(int idTache, String titre) {
        SQLiteDatabase db = getWritableDatabase();

        //Chercher le nom
        String query = "UPDATE " + TABLE_TACHE + " SET " + COLONNE_NOM_TACHE + " = '" + titre + "' WHERE " +
                COLONNE_ID_TACHE + " = " + idTache + ";";
        db.execSQL(query);
    }


    public void supprimerTache(int idTache) {
        SQLiteDatabase db = getWritableDatabase();

        //Chercher le nom
        String query = "DELETE FROM " + TABLE_TACHE + " WHERE " +
                COLONNE_ID_TACHE + " = " + idTache + ";";
        db.execSQL(query);
    }
}
