package com.volkanunlu.javamap.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.volkanunlu.javamap.R;
import com.volkanunlu.javamap.databinding.ActivityMapsBinding;
import com.volkanunlu.javamap.model.Place;
import com.volkanunlu.javamap.roomdb.PlaceDao;
import com.volkanunlu.javamap.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude; //se??ilen enlem ve boylam ad??na ald??k save metodunda laaz??m
    Double selectedLongitude; //se??ilen enlem ve boylam ad??na ald??k save metodunda laaz??m
    private CompositeDisposable compositeDisposable=new CompositeDisposable(); //kullan at ????esi bizim i??in.
    Place selectedPlace;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();
        sharedPreferences=this.getSharedPreferences("com.volkanunlu.javamap",MODE_PRIVATE);
        info=false;

        //database build ettik. context : bulundu??un alan, ??al??acak alan s??n??f?? , database ismi
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();

        placeDao= db.placeDao(); //dao'ya eri??im sa??land??.

        selectedLatitude=0.0;
        selectedLongitude=0.0;
        binding.saveButton.setEnabled(false); //kullan??c?? bir yeri se??mediyse false olsun. Kay??t yapamas??n.


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);


        Intent intent=getIntent(); //intenti kar????la
        String intentInfo=intent.getStringExtra("info"); //infonun i??indeki stringe ??evir.

        if(intentInfo.equals("new")){  //yeni bir ??ey eklenecekse i??lemlerimi buraya aktard??m

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE); //buton ghost moda ge??iyor di??er g??r??n??mler ekran?? kapl??yor.

            //casting kullanarak Location Manager ??zerinde lokasyonu alal??m.

            locationManager=(LocationManager) this.getSystemService((Context.LOCATION_SERVICE));
            //casting i??lemi --> location manager i??in kullan??yorum diyorum.

            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    info=sharedPreferences.getBoolean("info",false);

                    if(!info){ //info==false  //bu ??ekilde s??rekli map ??zerine odaklamay?? kald??rd??k 1 defa ile s??n??rlad??k bu algoritma ile.

                        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude()); //kullan??n??n konumunu Latlng t??r??ne ??evirdik
                        //enlem ve boylam bilgilerini verdik.
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15)); //Ekranda kameray?? konuma odaklad??k.
                        sharedPreferences.edit().putBoolean("info",true).apply();

                    }
                }
            };


            //kullan??c??dan izin al??caz.ContextCompat ile olu??turuyoruz bu iznin iste??ini.
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //kullan??c??ya bir mant??k g??stermek istiyorsam kulland??????m ara??, shouldshowrequest

                    //snackbar ile bu mant?????? ??al????t??r??yorum, kullan??c??ya g??steriyorum, izin ver butonu ile listener yazarak izin i??lemini ger??ekle??tirece??im.
                    Snackbar.make(binding.getRoot(),"Permission needed for maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            //request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION); //izin istedik

                        }
                    }).show();

                }else{  //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION); //izin istedik

                }

            }else {  //zaten izin daha ??nce verildi ise ger??ekle??ecek alan

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation!=null){
                    LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
                mMap.setMyLocationEnabled(true); //Benim konumumum etkin oldu??undan emin ol ad??na ekledik mavi location symbol ????kt??.
            }


        }
        else{

            mMap.clear();
            selectedPlace=(Place) intent.getSerializableExtra("place"); //casting i??lemi uygulad??k se??ilen yeri ald??k.
            LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude); //Latlng ile enlem ve boylam?? ald??k.

            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name)); //marker ekledik, latlng'?? i??ine ekledik.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15)); //Cameray?? zoomlad??k

            binding.placeNameText.setText(selectedPlace.name); //yerin ismini se??ilen yerin ad?? olarak ald??k.
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }







    }

    private void registerLauncher(){  //izin alma olay??n??n metodu //bu metodu oncreate alt??nda ??a????raca????z.!
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override                                   
            public void onActivityResult(Boolean result) {

                if(result){
                    //permission granted //izin verildi

                    if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);
                        //sadece bu haliyle kabul etmiyor, izin almam?? kontrol ??art??na ba??lamam?? istiyor. ??art?? yukarda verdim.??zin al??nd??ysa ??art??n?? koyarak.

                        Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation!=null){
                            LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }
                    }
                }
                else{  //izin verilmedi ise toast mesaj?? vericez //permission denied
                    Toast.makeText(MapsActivity.this, "Permission Needed!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

        mMap.clear(); //??nceki koyulan marker?? temizle, kald??r.
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude=latLng.latitude;
        selectedLongitude=latLng.longitude;
        binding.saveButton.setEnabled(true); //kullan??c?? se??tiyse bir yeri aktif olsun. Kay??t yapabilsin.

    }

    public void save(View view){
                                //yeni bir yer eklerken binding ile ismini ald??k, enlem ve boylam?? verdik.
        Place place=new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);

        //Threading --> Main(UI) Ana i??lemlerin yap??ld?????? yer, fazla i??lem yap??lmas?? ??nerilmez app ????ker.
        // , Default (CPU Intensive), Arka planda ??al????an, arka arkaya yap??lan i??lemler. Cpu yoran i??lemler bir listeyi dizmek gibi yo??un i??lemler.
        // IO(newtork,database)   Veritaban?? operasyonlar?? , internetten bir veri isteme gibi i??lemler


        //  placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); //kolay yol ama tercih edileni disposable (kullan at)

        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())  //hangi threadde ??al????s??n arkaplanda ??al????t??rd??k burada
                .observeOn(AndroidSchedulers.mainThread()) //hangi threadda bana sonucu g??stersin
                .subscribe(MapsActivity.this::handleResponse) //metoda referans veriyorum, benim i??in ??al????t??r demek.

        );



    }

    private void handleResponse(){ //save i??lemi bittikten sonra ne yap??laca????n?? tan??ml??yoruz.

        Intent intent=new Intent(MapsActivity.this,MainActivity.class); //oldu??um alan , gidece??im yer
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //her ??eyi temizlemek ad??na yap??yorum
        startActivity(intent);

    }

    public void  delete(View view){  //save metodunun ayn?? benzeri sadece placeDao'da delete'i ??a????rd??k.

        if(selectedPlace!=null){

            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse));
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        compositeDisposable.clear(); //daha ??nce yapt??????m b??t??n kollar yok edilecek.Haf??zamda yer tutmayacak
    }
}