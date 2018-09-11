package com.ravisharma.findfriend.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ravisharma.findfriend.Adapters.UserList;
import com.ravisharma.findfriend.Models.UserInfo;
import com.ravisharma.findfriend.Models.User_location;
import com.ravisharma.findfriend.Provider;
import com.ravisharma.findfriend.R;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements LocationListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    FirebaseAuth auth;
    DatabaseReference userinfo, online_check, online, currentUserRef;

    TextView profile;
    ListView list;
    List<UserInfo> userlist, mainList, list_for_listview;
    List<User_location> online_list;

    private Location curLoc;
    private LocationRequest request;
    private GoogleApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        userinfo = FirebaseDatabase.getInstance().getReference("UserInfo");
        online_check = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        online = FirebaseDatabase.getInstance().getReference("Online");
        currentUserRef = online.child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        list = findViewById(R.id.list);
        profile = findViewById(R.id.textView);

        userlist = new ArrayList<>();
        mainList = new ArrayList<>();
        list_for_listview = new ArrayList<>();

        String number = "Welcome " + auth.getCurrentUser().getPhoneNumber();
        profile.setText(number);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                displayLocationSettingsRequest();

                /*
                 * portal_certi@123
                 * */
            }
        } else {
            displayLocationSettingsRequest();
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ProfileActivity.this, MapsActivity.class);
                i.putExtra("user", list_for_listview.get(position).getUid());
                i.putExtra("number", list_for_listview.get(position).getPhone());
                startActivity(i);
            }
        });
    }

    private void setData() {
        userinfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userlist.clear();
                mainList.clear();

                for (DataSnapshot sn : dataSnapshot.getChildren()) {
                    UserInfo i = sn.getValue(UserInfo.class);
                    userlist.add(i);
                }

                abc:
                for (int i = 0; i < userlist.size(); i++) {
                    for (int j = 0; j < Provider.contacts.size(); j++) {
                        if ((Provider.contacts.get(j).getPhone().contains(userlist.get(i).getPhone()))) {
                            mainList.add(userlist.get(i));
                            continue abc;
                        }
                    }
                }

                checkOnline(mainList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void checkOnline(final List<UserInfo> mainList) {

        online_list = new ArrayList<>();

        online_check.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().removeValue();

                    online.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(new User_location(String.valueOf(curLoc.getLongitude()),
                                    String.valueOf(curLoc.getLatitude()), "online",
                                    FirebaseAuth.getInstance().getCurrentUser().getUid()));

                    online.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            online_list.clear();
                            list_for_listview.clear();

                            for(DataSnapshot sn : dataSnapshot.getChildren()) {
                                User_location l = sn.getValue(User_location.class);
                                online_list.add(l);
                            }

                            abc:
                            for (int i = 0; i < mainList.size(); i++) {
                                for (int j = 0; j < online_list.size(); j++) {
                                    if (mainList.get(i).getUid().equals(online_list.get(j).getUid())) {
                                        list_for_listview.add(mainList.get(i));
                                        continue abc;
                                    }
                                }
                            }

                            setListView(list_for_listview);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setListView(List<UserInfo> list_for_listview) {
        list.setAdapter(new UserList(ProfileActivity.this, list_for_listview));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.signout) {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    //Location Methods
    private void displayLocationSettingsRequest() {
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        apiClient.connect();

        request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        request.setInterval(10000);
//        request.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(apiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d("tag", "All location settings are satisfied.");
                        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, ProfileActivity.this);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
//                        Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(ProfileActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
//                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("tag", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        System.exit(0);
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, request, this);
        } else {
            displayLocationSettingsRequest();
        }
    }

    //LocationListener method
    @Override
    public void onLocationChanged(Location location) {
        curLoc = location;
        setData();
//        User_location u = new User_location(String.valueOf(curLoc.getLongitude()), String.valueOf(curLoc.getLatitude()), "online");
//        locations.child(auth.getCurrentUser().getUid()).setValue(u);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (apiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient, this);
//            locations.child(auth.getCurrentUser().getUid()).removeValue();
        }
    }
}
