package com.example.caucse.baseui.model;

public class Product {
    private int Id;
    private String name;
    private String Country;
    private String Flavor;
    private String Kind;
    private float IBU;
    private float Alcohol;
    private int Kcal;



    public Product(int Id, String name, String country, String flavor, String kind, float IBU, float alcohol, int kcal) {
        this.Id = Id;
        this.name = name;
        this.Country = country;
        this.Flavor = flavor;
        this.Kind = kind;
        this.IBU = IBU;
        this.Alcohol = alcohol;
        this.Kcal = kcal;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {        this.Id = id;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) { this.Country = country;}

    public String getFlavor() {
        return Flavor;
    }

    public void setFlavor(String flavor) {
        this.Flavor = flavor;
    }

    public String getKind() {
        return Kind;
    }

    public void setKind(String kind) {
        this.Kind = kind;
    }

    public float getIBU() {
        return IBU;
    }

    public void setIBU(float IBU) {
        this.IBU = IBU;
    }

    public float getAlcohol() {
        return Alcohol;
    }

    public void setAlcohol(float alcohol) {
        this.Alcohol = alcohol;
    }

    public int getKcal() {    return Kcal;    }

    public void setKcal(int kcal) {
        this.Kcal = kcal;
    }

}
