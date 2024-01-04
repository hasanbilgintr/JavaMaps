package com.hasanbilgin.javamaps;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.Manifest;
import android.content.Context;
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
import com.hasanbilgin.javamaps.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLaouncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;

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

         sharedPreferences=MapsActivity.this.getSharedPreferences("com.hasanbilgin.javamaps",MODE_PRIVATE);
         info=false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    //haritada uzun basıldığında çalışan metod
    public void onMapLongClick(@NonNull LatLng latLng) {
        //latLng kordinattır
        //mMap.clear() koyulan marker ları temizler
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));





    }

    @Override
    //harita hazır olduğunda ne yapılcak metodu
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //uzun basılma için eklendi
        mMap.setOnMapLongClickListener(this);

//        // Add a marker in Sydney and move the camera
//        //latitude ->enlem //longitude-> boylam demektir
//        LatLng sydney = new LatLng(-34, 151);
//        //enlem boylam nerden bakılıyor bakılcak gösterilcek
//        //markır eklenmesi gerekiyormuş kordinat vermiş altta onu eklemiş (işaretçi eklemek)
//        //üstüne tıklanınca Marker in Sydney olucaktır
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        //haritayı oraya odaklanarak başlat
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //kordinatlarını bulmak için maps.google.com da  bi eyfel kulesinin markır(tıklamak) landığında linkte yer alır enlem48.85 boylam2.29 gibi markıra sağ tıklarsanda görürsün yada markıra what is here da verir kordiyi

        //konum objsini tanımladık  //sınıf lokasyon yöneticisidir
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //bir arayüzdür //konum güncellendiğinde çalışan bir arayüzdür
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                //daha konum alınmadı
                //System.out.println("location: " + location);
                //location.getAltitude()//yükseklikmiş
                info=sharedPreferences.getBoolean("info",false);

                if(!info){
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    sharedPreferences.edit().putBoolean("info",true).apply();
                }
            }
        };
        //manifestte izin alınmadıysa burda kontrol eder
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //android kendi kontrol zorunlu ise...
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.getRoot(), "Permission needed for maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //izin rica etmek //request permission
                        permissionLaouncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                });
            } else {
                //izin rica etmek //request permission
                permissionLaouncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            //ilk 0 demek 0sn de konum güncelle demek //1000 olsaydı 1sn bir güncellemek olurdu
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            //region en son konumu almak //hatta onLocationChanged metodu boş olursa en son konumu değiştirirsek tekrar çalıştırmada en son işaretlenen yeri veriri
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
            }
            //endregion
            //haritada mavi bir konumun orda olduğu gönstern bir simge gösterir
            mMap.setMyLocationEnabled(true);
        }
        //güncel lokasyonumuzu al
        //locationManager.getCurrentLocation();
        //konum güncellemelerini almaya başla
        //locationManager.requestLocationUpdates();
        //konum güncellemelerini bir kere başla
        //locationManager.requestSingleUpdate();


        //48.858840278297876, 2.294325501892498
        //bu kod sadece o tarafa yönelcektir markır koymucaktır (addmarker olmadığı zaman)
//        LatLng eiffel = new LatLng(48.858840278297876, 2.294325501892498);
//        mMap.addMarker(new MarkerOptions().position(eiffel).title("Eiffel Tower"));
//        //mMap.moveCamera(CameraUpdateFactory.newLatLng(eiffel));
//        //zoomlu yönlendirebiliriiz onun için
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 15));//zoom aralığı 0 ile 25 arasındadır
    }

    private void registerLauncher() {
        permissionLaouncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    //permissin granted // izin verildi
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //illa tekrar ifi istedi mecbur yazdık
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                        //region en son konumu almak //hatta onLocationChanged metodu boş olursa en son konumu değiştirirsek tekrar çalıştırmada en son işaretlenen yeri veriri
                        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null) {
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }
                        //endregion
                    }

                } else {
                    Toast.makeText(MapsActivity.this, "Permission Needed!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


}

