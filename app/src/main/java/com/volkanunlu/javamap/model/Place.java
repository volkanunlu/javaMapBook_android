package com.volkanunlu.javamap.model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity  //room database kullanmam için gerekli yoksa görmüyor.
public class Place implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "latitude")
    public Double latitude;

    @ColumnInfo(name = "longitude")
    public Double longitude;


    public Place(String name, Double latitude, Double longitude){ //tanımlamaları daha kolay yapmak adına constructor metodumuz.

        this.name=name;
        this.latitude=latitude;
        this.longitude=longitude;

    }

}
