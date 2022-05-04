package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var lastMarker: Marker


    private lateinit var map: GoogleMap

    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var title = ""

    private var isLocationSelected = false
    private var isMapAlreadyInitialized = false

    private val FINE_LOCATION_ACCESS_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_view) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.btnSavelocation.setOnClickListener {
            if (isLocationSelected) {
                onLocationSelected()
            } else {
                Toast.makeText(context, "Please choose a location.", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }


    private fun onLocationSelected() {
        _viewModel.latitude.value = lat
        _viewModel.longitude.value = long
        _viewModel.reminderSelectedLocationStr.value = title
        _viewModel.navigationCommand.postValue(NavigationCommand.Back)
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            addMarkerOnMap(poi.latLng, poi.name)
        }
    }

    private fun setLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLong ->
            addMarkerOnMap(latLong, getString(R.string.dropped_pin))
        }
    }

    private fun addMarkerOnMap(latLng: LatLng, name: String) {

        if (this::lastMarker.isInitialized) {
            lastMarker.remove()
        }

        lastMarker = map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(name)
        )
        lastMarker.showInfoWindow()

        lastMarker.showInfoWindow()
        lat = latLng.latitude
        long = latLng.longitude
        title = name
        isLocationSelected = true

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(p0: GoogleMap?) {
        Toast.makeText(context, "onMapReady", Toast.LENGTH_SHORT).show()
        isMapAlreadyInitialized = true
        map = p0!!
        enableClickListeners()
        enableUserLocation()

    }

    private fun enableClickListeners() {
        setPoiClick(map)
        setLongClick(map)
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED
    }

    // Your app needs the ACCESS_FINE_LOCATION permission for getting the user’s location details

    @SuppressLint("MissingPermission")
    private fun enableUserLocation() {

        if (this::lastMarker.isInitialized) {
            lastMarker.remove()
        }

        when (isLocationPermissionGranted()) {
            true -> {

                // You can use the API that requires the permission.
                map.isMyLocationEnabled = true

                // Based on https://developer.android.com/training/location/retrieve-current?authuser=1
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            lastMarker = map.addMarker(
                                MarkerOptions().position(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    )
                                ).title("Actual Position")
                            )
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        location.latitude,
                                        location.longitude
                                    ), 15f
                                )
                            )
                        }
                    }

            }
            false -> {
                val sydney = LatLng(-34.0, 151.0)
                lastMarker =
                    map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f))

                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    FINE_LOCATION_ACCESS_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            FINE_LOCATION_ACCESS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && (grantResults[0] == PERMISSION_GRANTED)) {
                    enableUserLocation()
                } else {
                    Toast.makeText(
                        context,
                        "Location permission was not granted.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }


}
