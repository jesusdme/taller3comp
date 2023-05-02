package com.example.taller3comp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.taller3comp.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import org.json.JSONObject
import java.util.*
import kotlin.math.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var miLat: Double = 0.0
    var miLon: Double = 0.0
    private val PERMISSIONS_REQUEST_ACCESS_LOCATION = 5 //id localizacion

    var distancia = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback
    private val REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //si no tiene permiso preguntar
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_LOCATION
            )

        } else //si tiene permiso usar la ubicacion
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CODE
                )
            } else {


                binding = ActivityMapsBinding.inflate(layoutInflater)
                setContentView(binding.root)


            }
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

        mLocationRequest = createLocationRequest()


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                var ubicacion = locationResult.lastLocation
                Log.i("ubicacion", "--------------$ubicacion---------")
                if (ubicacion != null) {
                    showUserLocation()
                }
            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap



        requestLocationFunction()
        showUserLocation()

        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18F))

        //leer json
        marcadores()
    }




    private fun showUserLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    var ubicAc = LatLng(it.latitude, it.longitude)
                    distancia = abs(calcularDistancia(miLat, miLon, it.latitude, it.longitude))



                    if (distancia >= 30) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicAc))
                        mMap.addMarker(
                            MarkerOptions().position(ubicAc)
                                .title("UBICACION")
                                .snippet("") //Texto de Información
                                .alpha(0.5f)//Trasparencia
                        )
                        miLat = it.latitude
                        miLon = it.longitude



                    }

                }
            }


    }


    private fun requestLocationFunction() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        )
            return

        mFusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            showUserLocation()
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
    }

    private fun createLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            setInterval(25000)
            setFastestInterval(10000)
            setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }
    }
    private fun geoCoderSearchLatLang(latLng: LatLng): String? {
        val geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        var address = ""

        if (addresses != null && addresses.size > 0) {
            val returnedAddress = addresses[0]
            address = "${returnedAddress.thoroughfare}, ${returnedAddress.locality}"
        }
        return address
    }
    fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierra = 6371 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(
                dLon / 2
            ) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanciaEnKM = radioTierra * c
        val distanciaEnMetros = distanciaEnKM * 1000

        return distanciaEnMetros
    }

    fun marcadores() {
        val jsonString =
            application.assets.open("locations.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)

        // Obtenemos el objeto "locations"
        val locationsObject = jsonObject.getJSONObject("locations")

        // Obtenemos el array "locationsArray"
        val locationsArray = jsonObject.getJSONArray("locationsArray")

        // Iteramos sobre los elementos del array "locationsArray"
        for (i in 0 until locationsArray.length()) {
            val locationObject = locationsArray.getJSONObject(i)
            val lat = locationObject.getDouble("latitude")
            val lon = locationObject.getDouble("longitude")
            val name = locationObject.getString("name")
            // Haz lo que necesites con los valores
            var marc = LatLng(lat, lon)
            //añadir marcadores
            mMap.addMarker(
                MarkerOptions().position(marc)
                    .title(name)
                    .snippet(i.toString()) //Texto de Información
            )
        }
    }


}