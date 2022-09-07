package com.volkanunlu.javamap.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.volkanunlu.javamap.R;
import com.volkanunlu.javamap.adapter.PlaceAdapter;
import com.volkanunlu.javamap.databinding.ActivityMainBinding;
import com.volkanunlu.javamap.model.Place;
import com.volkanunlu.javamap.roomdb.PlaceDao;
import com.volkanunlu.javamap.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; //elementlere ulaşmak adına
    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    PlaceDatabase db;
    PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);

        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao= db.placeDao(); //dao'ya erişim sağlandı.

        compositeDisposable.add(placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MainActivity.this::handleResponse)
        );

    }

    private void handleResponse( List<Place> placeList){ //Maps activity gibi değil, burada bir list flowable var.Completable değil
        //parantez içerisinde belirtmem şart.

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //verilerimi göstermek için
        PlaceAdapter placeAdapter=new PlaceAdapter(placeList);  //verilerimi göstermek için
        binding.recyclerView.setAdapter(placeAdapter); //verilerimi göstermek için
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) { //menu bağlama işlemi

        MenuInflater menuInflater=getMenuInflater(); //xml ve kodu bağladığımız yapı
        menuInflater.inflate(R.menu.travel_menu,menu);
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //menüye tıklanırsa ne olacak?

        if(item.getItemId()==R.id.add_place){ //add place'e mi tıklandı kontrolü

            Intent intent=new Intent(MainActivity.this,MapsActivity.class);  //beni harita alanına yönlendirecek.
            intent.putExtra("info","new"); //yeni bir veriyi eklediğimi belirtiyorum.
            startActivity(intent); //aktivite başlat.
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}