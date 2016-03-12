package com.abd.classroom1;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.OnFragmentInteractionListener {

    // begin note : should be save later in the bundle;
    Client client;
    Kryo kryo;
    FragmentManager fm;
    FragmentTransaction ft;
    LoginFragment loginfrag;
    ActiveUsersFragment activeusersfragment;
    private UserLogin iam;


    ///// end note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fm = getFragmentManager();
        loginfrag = (LoginFragment) fm.findFragmentByTag("LOGIN");
        if (loginfrag == null) {
            loginfrag = new LoginFragment();
        }
        ft = fm.beginTransaction();
        ft.add(R.id.fragment_container, loginfrag, "LOGIN");
        ft.commit();
        // loginfrag.hideErrorMessage();


        //open the connection for the first TIME
        try {
            if (openConnection()) {
                Toast.makeText(MainActivity.this,
                        getResources().getText(R.string.server_found), Toast.LENGTH_LONG).show();
                loginfrag.setClient(client);
                loginfrag.hideErrorMessage();
            }

        } catch (Exception e) {
            Toast.makeText(MainActivity.this,
                    getResources().getText(R.string.unable_to_connect_server), Toast.LENGTH_LONG).show();
            e.printStackTrace();

            client = null;
            return;
        }


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /// ABd Add this code

    public boolean openConnection() throws Exception {
        client = new Client(16384,8192);
        kryo = client.getKryo();
        kryo.register(byte[].class);
        kryo.register(String[].class);
        kryo.register(UserLogin.class);
        kryo.register(TextMeesage.class);
        kryo.register(SimpleTextMessage.class);
        kryo.register(FileChunkMessageV2.class);
        client.start();
        InetAddress address = client.discoverHost(54777, 5000);
        //   Log.d("INFO",address.toString());
        client.connect(5000, address, 9995, 54777);

        client.addListener(new Listener() {
            public void received(Connection c, Object ob) {
                if (ob instanceof UserLogin) {
                    Log.d("INFO","New User Login Packet");
                    if (((UserLogin) ob).getUserType().equals("TEACHER")) {
                        System.out.println("Login Message Recived");
                        if (((UserLogin) ob).isLogin_Succesful()) {
                            setSuccessfulLogin(((UserLogin) ob));
                            fm = getFragmentManager();
                            ft = fm.beginTransaction();
                            //if(activeusersfragment ==null){
                            activeusersfragment = new ActiveUsersFragment();
                            // }
                            ft.replace(R.id.fragment_container, activeusersfragment, "ACTIVE");
                            activeusersfragment.setClient(client);
                            activeusersfragment.setUserlogin((UserLogin) ob);
                            ft.commit();
                            Log.d("INFO", "Succesfull Log IN");
                        }
                    }else if (((UserLogin) ob).getUserType().equals("STUDENT")) {
                        if(activeusersfragment!=null) {
                            final Object ot = ob;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activeusersfragment.addNewClient(((UserLogin) ot));
                                }
                            });

                        }
                    }
                }
            }
        });
        return true;


    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void setSuccessfulLogin(UserLogin ul) {
        this.iam = ul;
    }
}
