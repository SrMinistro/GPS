package com.example.info24.gpshawai;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    TextView mensaje1;
    TextView mensaje2;
    private String v1, v2;
    private String DIR;
    private Timer loopTimer = new Timer();
    Context context;
    Button enviar;

    private static final String LOG = "MainActivity";
    RequestQueue requestQueve;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        requestQueve = Volley.newRequestQueue(this);
        mensaje1 = (TextView) findViewById(R.id.mensaje_id);
        mensaje2 = (TextView) findViewById(R.id.mensaje_id2);
        enviar = (Button) findViewById(R.id.button);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            Log.d("Prueba ", "Prueba Build.VERSION.SDK_INT >= 23 ");
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                //intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }


        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);



//        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            Log.d(LOG,"Permission is granted");
//            //File write logic here
//            return ;
//        }

        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Actualizar();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        time time = new time();
//        time.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkDiskPermission ();
        validarPermisos();

    }




    LocationManager mlocManager;
    Localizacion Local;

    private void locationStart() {
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        //    mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
        mensaje1.setText("Localizacion agregada");
        mensaje2.setText("Buscando Direccion");
    }

    private void checkDiskPermission ()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No Permissions" , Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        else
        {
            Toast.makeText(this, "Has Permissions" , Toast.LENGTH_LONG).show();
        }
    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    String Text = "Mi direccion es: \n"
                            + DirCalle.getAddressLine(0);
                    mensaje2.setText(Text);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /* Aqui empieza la Clase Localizacion */
    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            loc.getLatitude();
            loc.getLongitude();
            v1 = ("" + loc.getLatitude());
            v2 = ("" + loc.getLongitude());

            Log.e(LOG, "lat" + loc.getLatitude() + " - lon" + loc.getLongitude());

            DIR = ("" + mensaje2.getText());
            Actualizar();
            String Text = "Mi ubicacion actual es: " + "\n Lat = "
                    + loc.getLatitude() + "\n Long = " + loc.getLongitude();
            mensaje1.setText(Text);
            this.mainActivity.setLocation(loc);

        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
            mensaje1.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
            mensaje1.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }


    //dejar tares en segundo plano
    public void Hilo()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void ejecutar()
    {

            time time = new time();
            time.execute();

    }
    public class time extends AsyncTask<Void,Integer,Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... voids) {
            for (int i=1; i<=21;i++)
            {
                Hilo();
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            ejecutar();
//            locationStart();
//            Actualizar();
            //Toast.makeText(getApplicationContext(),"ENVIAR DATOS",Toast.LENGTH_LONG).show();
        }
    }



    public void validarPermisos() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            int permissionCheckFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionCheckCoaeseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);


            if (permissionCheckFineLocation != PackageManager.PERMISSION_GRANTED &&
                    permissionCheckCoaeseLocation != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    // El permiso ya fue rechazado antes
                    // Mostrar alerta de explicacion

                } else {

                    //De lo contrario carga la ventana para autorizar el permiso
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);

                }
            }


        }






    }

    private void Actualizar() {

        String url = "http://127.0.0.1/phpconcord/inserbucaramanga.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                         map.put("latitude", v1.toString());
                map.put("longitude", v2.toString());
                            // map.put("direccion",mensaje2.getText().toString());
                return map;
            }
        };
        requestQueve.add(request);
    }





    }
