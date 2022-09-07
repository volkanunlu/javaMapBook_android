package com.volkanunlu.javamap.roomdb;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.volkanunlu.javamap.model.Place;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao   //Verilere erişeceğimiz arayüzümüz
public interface PlaceDao {

    @Query("SELECT * FROM Place" )
    Flowable <List<Place>> getAll();   //bana bir list döndürecek.


    @Insert  //ekleme işlemi
    Completable insert(Place  place);

    @Delete //silme işlemi
    Completable delete (Place place);





}
