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
    Double selectedLatitude; //seçilen enlem ve boylam adına aldık save metodunda laazım
    Double selectedLongitude; //seçilen enlem ve boylam adına aldık save metodunda laazım
    private CompositeDisposable compositeDisposable=new CompositeDisposable(); //kullan at öğesi bizim için.
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

        //database build ettik. context : bulunduğun alan, çalıacak alan sınıfı , database ismi
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();

        placeDao= db.placeDao(); //dao'ya erişim sağlandı.

        selectedLatitude=0.0;
        selectedLongitude=0.0;
        binding.saveButton.setEnabled(false); //kullanıcı bir yeri seçmediyse false olsun. Kayıt yapamasın.


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);


        Intent intent=getIntent(); //intenti karşıla
        String intentInfo=intent.getStringExtra("info"); //infonun içindeki stringe çevir.

        if(intentInfo.equals("new")){  //yeni bir şey eklenecekse işlemlerimi buraya aktardım

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE); //buton ghost moda geçiyor diğer görünümler ekranı kaplıyor.

            //casting kullanarak Location Manager üzerinde lokasyonu alalım.

            locationManager=(LocationManager) this.getSystemService((Context.LOCATION_SERVICE));
            //casting işlemi --> location manager için kullanıyorum diyorum.

            locationListener=new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    info=sharedPreferences.getBoolean("info",false);

                    if(!info){ //info==false  //bu şekilde sürekli map üzerine odaklamayı kaldırdık 1 defa ile sınırladık bu algoritma ile.

                        LatLng userLocation=new LatLng(location.getLatitude(),location.getLongitude()); //kullanının konumunu Latlng türüne çevirdik
                        //enlem ve boylam bilgilerini verdik.
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15)); //Ekranda kamerayı konuma odakladık.
                        sharedPreferences.edit().putBoolean("info",true).apply();

                    }
                }
            };


            //kullanıcıdan izin alıcaz.ContextCompat ile oluşturuyoruz bu iznin isteğini.
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                    //kullanıcıya bir mantık göstermek istiyorsam kullandığım araç, shouldshowrequest

                    //snackbar ile bu mantığı çalıştırıyorum, kullanıcıya gösteriyorum, izin ver butonu ile listener yazarak izin işlemini gerçekleştireceğim.
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

            }else {  //zaten izin daha önce verildi ise gerçekleşecek alan

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);

                Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation!=null){
                    LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                }
                mMap.setMyLocationEnabled(true); //Benim konumumum etkin olduğundan emin ol adına ekledik mavi location symbol çıktı.
            }


        }
        else{

            mMap.clear();
            selectedPlace=(Place) intent.getSerializableExtra("place"); //casting işlemi uyguladık seçilen yeri aldık.
            LatLng latLng=new LatLng(selectedPlace.latitude,selectedPlace.longitude); //Latlng ile enlem ve boylamı aldık.

            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name)); //marker ekledik, latlng'ı içine ekledik.
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15)); //Camerayı zoomladık

            binding.placeNameText.setText(selectedPlace.name); //yerin ismini seçilen yerin adı olarak aldık.
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }







    }

    private void registerLauncher(){  //izin alma olayının metodu //bu metodu oncreate altında çağıracağız.!
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override                                   
            public void onActivityResult(Boolean result) {

                if(result){
                    //permission granted //izin verildi

                    if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
                    {
                        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,0,0,locationListener);
                        //sadece bu haliyle kabul etmiyor, izin almamı kontrol şartına bağlamamı istiyor. şartı yukarda verdim.İzin alındıysa şartını koyarak.

                        Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation!=null){
                            LatLng lastUserLocation=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                        }
                    }
                }
                else{  //izin verilmedi ise toast mesajı vericez //permission denied
                    Toast.makeText(MapsActivity.this, "Permission Needed!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

        mMap.clear(); //önceki koyulan markerı temizle, kaldır.
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude=latLng.latitude;
        selectedLongitude=latLng.longitude;
        binding.saveButton.setEnabled(true); //kullanıcı seçtiyse bir yeri aktif olsun. Kayıt yapabilsin.

    }

    public void save(View view){
                                //yeni bir yer eklerken binding ile ismini aldık, enlem ve boylamı verdik.
        Place place=new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);

        //Threading --> Main(UI) Ana işlemlerin yapıldığı yer, fazla işlem yapılması önerilmez app çöker.
        // , Default (CPU Intensive), Arka planda çalışan, arka arkaya yapılan işlemler. Cpu yoran işlemler bir listeyi dizmek gibi yoğun işlemler.
        // IO(newtork,database)   Veritabanı operasyonları , internetten bir veri isteme gibi işlemler


        //  placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); //kolay yol ama tercih edileni disposable (kullan at)

        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())  //hangi threadde çalışsın arkaplanda çalıştırdık burada
                .observeOn(AndroidSchedulers.mainThread()) //hangi threadda bana sonucu göstersin
                .subscribe(MapsActivity.this::handleResponse) //metoda referans veriyorum, benim için çalıştır demek.

        );



    }

    private void handleResponse(){ //save işlemi bittikten sonra ne yapılacağını tanımlıyoruz.

        Intent intent=new Intent(MapsActivity.this,MainActivity.class); //olduğum alan , gideceğim yer
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //her şeyi temizlemek adına yapıyorum
        startActivity(intent);

    }

    public void  delete(View view){  //save metodunun aynı benzeri sadece placeDao'da delete'i çağırdık.

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

        compositeDisposable.clear(); //daha önce yaptığım bütün kollar yok edilecek.Hafızamda yer tutmayacak
    }
}