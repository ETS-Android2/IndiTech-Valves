package com.example.android.inditechvalves;

public class CastingMaterial {
    public String heatNumber;
    public String size;
    public String castingClass;
    public String typeOfCasting;
    public String materialOfConstruction;
    public CastingMaterial(){}
    public CastingMaterial(String hno,String sz,String cclass,String toc,String moc){
        heatNumber=hno;
        size=sz;
        castingClass=cclass;
        typeOfCasting=toc;
        materialOfConstruction=moc;
    }
}
