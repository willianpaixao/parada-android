package br.com.mossteam.parada;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import br.com.mossteam.parada.db.SyncManager;

public class ReportActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONObject report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_report);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {

            LoadReport loadReport = new LoadReport(ReportActivity.this);
            loadReport.execute(bundle.getString("_id"));
            try {
                report = loadReport.get();
            } catch (InterruptedException e) {
                Log.d("async", e.toString());
            } catch (ExecutionException e) {
                Log.d("async", e.toString());
            }
        }
        try {
            Date date = new Date(Long.valueOf(report.getString("date")));
            Locale.setDefault(new Locale("pt", "BR"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy " +
                    "HH:mm:ss", Locale.getDefault());
            TextView textView = (TextView) findViewById(R.id.date);
            textView.setText(dateFormat.format(date));
            textView = (TextView) findViewById(R.id.bus_code);
            textView.setText(report.getString("bus_code"));
        } catch (JSONException e) {
            Log.d("json", e.toString());
        }

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                break;
            default:
                // TODO: implement error handling
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: ask permission.
            return;
        }

        try {
            LatLng latLng = new LatLng(
                    Double.parseDouble(report.getJSONObject("location").getString("latitude")),
                    Double.parseDouble(report.getJSONObject("location").getString("longitude"))
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
            mMap.addMarker(new MarkerOptions().position(latLng));
        } catch (JSONException e) {
            Log.d("json", e.toString());
        }


    }
    private class LoadReport extends AsyncTask<String, Void, JSONObject> {

        Context context;

        public LoadReport(Context context) {
            this.context = context;
        }

        @Override
        protected JSONObject doInBackground(String... docID) {
            SyncManager s = new SyncManager(context);
            Document document = s.getDatabase().getExistingDocument(docID[0]);
            return new JSONObject(document.getProperties());
        }
    }
}
