package com.example.habits.utils;

public class Tache {
    public int id;
    public String nom;
    public int mois;
    public int annee;

    public Tache(String nom, int id, int mois, int annee) {
        this.nom = nom;
        this.id = id;
        this.mois = mois;
        this.annee = annee;
    }
}
