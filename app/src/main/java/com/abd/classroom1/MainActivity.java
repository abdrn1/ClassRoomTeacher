package com.abd.classroom1;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.OnFragmentInteractionListener, Runnable,
        ActiveUsersFragment.OnFragmentInteractionListener,
        MessageViewerFragment.OnFragmentInteractionListener,
        ExamResultViewerFragment.OnFragmentInteractionListener,
        ClientListAdapter.OnClientListAdapterInteraction,
        AddRemoveSync {

    // begin note : should be save later in the bundle;
    Client client;
    Kryo kryo;
    FragmentManager fm;
    FragmentTransaction ft;
    // the fragments
    LoginFragment loginfrag;
    ActiveUsersFragment activeusersfragment;
    MessageViewerFragment messageViewerFragment;
    ExamResultViewerFragment examResultViewerFragment;

    ///
    private UserLogin iam;
    private List<ChatMessageModel> chatMessageModelList;
    private List<ClientModel> clientsList;
    private List<ExamResultModel> examResultModels;
    private Thread checkServer;
    private int activeFragmentID = 1;
    String[] clientStatus;


    ///// end note

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ExamResultModel aa = new ExamResultModel();
        aa.setClientID("105");
        aa.setClientName("Radwan");
        aa.setClientImage(R.drawable.u27);
        aa.setStudentMark(25);
        aa.setExamMark(50);
        chatMessageModelList = Collections.synchronizedList(new ArrayList<ChatMessageModel>());

        // for saving conversation
        examResultModels = new ArrayList<>(); // for saving exam result
        examResultModels.add(aa);
        clientsList = new ArrayList<>();// for saving active clients
        clientStatus = getResources().getStringArray(R.array.client_status); // String Array of clients status

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
        prepareConnection();
        checkServer = new Thread(this);
        checkServer.start();


        //open the connection for the first TIME


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            Log.d("Info", "Acrive frag ID = :" + Integer.toString(activeFragmentID));
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

        if (id == R.id.mi_exam_result) {
            Log.d("frag", "Curren Active = " + Integer.toString(activeFragmentID));
            // Handle the camera action
            if (activeFragmentID == 4) {
                examResultViewerFragment.updateExamResultContent();
            } else {
                if (iam != null && ft != null) {
                    examResultViewerFragment = ExamResultViewerFragment.newInstance(examResultModels);
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_container, examResultViewerFragment, "EXAMEXAM");
                    examResultViewerFragment.setL1(examResultModels);
                    activeFragmentID = 4;
                    ft.addToBackStack(null);
                    //examResultViewerFragment.set(iam);
                    ft.commit();


                }

            }
        } else if (id == R.id.nmi_active_users) {

            if (activeFragmentID == 2) {
                activeusersfragment.updateActiveListContent();
            } else {
                if (iam != null && ft != null) {
                    activeusersfragment = new ActiveUsersFragment();
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_container, activeusersfragment, "EXAMEXAM");
                    activeusersfragment.setClient(client);
                    activeusersfragment.setActiveUsersList(clientsList);
                    activeusersfragment.setUserlogin(iam);
                    activeFragmentID = 2;
                    ft.commit();


                }
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /// ABd Add this code
    private void prepareConnection() {
        client = new Client(16384, 8192);
        kryo = client.getKryo();
        kryo.register(byte[].class);
        kryo.register(String[].class);
        kryo.register(UserLogin.class);
        kryo.register(TextMeesage.class);
        kryo.register(SimpleTextMessage.class);
        kryo.register(FileChunkMessageV2.class);
        kryo.register(LockMessage.class);
        kryo.register(StatusMessage.class);
        kryo.register(ExamResultMessage.class);
    }

    public boolean openConnection() throws Exception {
        client.start();
        InetAddress address = client.discoverHost(54777, 5000);
        client.connect(5000, address, 9995, 54777);
        client.setKeepAliveUDP(8000);

        client.addListener(new Listener() {
            public void received(Connection c, Object ob) {
                if (ob instanceof SimpleTextMessage) {
                    Log.d("SIMPLE", "New Simple Message Recived");
                    dealWithSimpleTextMessage((SimpleTextMessage) ob);

                } else if (ob instanceof UserLogin) {
                    if (!((UserLogin) ob).isLogin_Succesful()) {
                        showInvalidUserNameOrPassword();
                        return;
                    }
                    Log.d("INFO", "New User Login Packet");
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

                            // for Test Only
                            ClientModel tempcl = new ClientModel("29", "محمود هاشم", R.drawable.u29);
                            tempcl.setLastStatus("متوفر");

                            clientsList.add(tempcl);
                            tempcl = new ClientModel("32", "محمود هاشم", R.drawable.u30);
                            tempcl.setLastStatus("متوفر");
                            clientsList.add(tempcl);
                           /* clientsList.add(new ClientModel("27", "Hassan", R.drawable.unknown));
                            clientsList.add(new ClientModel("28", "Hassan", R.drawable.unknown));*/
                            activeusersfragment.setClient(client);
                            activeusersfragment.setUserlogin((UserLogin) ob);
                            activeusersfragment.setActiveUsersList(clientsList);
                            activeusersfragment.setChatMessageModelList(chatMessageModelList);
                            ft.commit();
                            Log.d("INFO", "Succesfull Log IN");
                        } else {
                            loginfrag.showInvalidLoginMessage();
                        }
                    } else if (((UserLogin) ob).getUserType().equals("STUDENT")) {
                        addNewActiveClient((UserLogin) ob);

                        if (activeFragmentID == 2) {

                            activeusersfragment.setActiveUsersList(clientsList);

                            activeusersfragment.updateActiveListContent();

                        } else {
                            showInvalidUserNameOrPassword();
                        }
                    }
                } else if (ob instanceof StatusMessage) {
                    dealWithStatusMessage((StatusMessage) ob);
                } else if (ob instanceof ExamResultMessage) {
                    Log.d("INFO", "Exam Result Message Recived");
                    dealWithExamResultMessage((ExamResultMessage) ob);
                }
            }
        });

        return true;


    }

    public void dealWithExamResultMessage(ExamResultMessage erm) {
        ExamResultModel temp = new ExamResultModel();
        temp.setClientID(erm.getSenderID());
        temp.setClientName(erm.getSenderName());
        int resourceID = getResourseId("u" + erm.getSenderID(), "drawable", getPackageName());
        if (resourceID == -1) {
            resourceID = R.drawable.unknown;
        }
        temp.setClientImage(resourceID);
        temp.setExamMark(erm.getExamresult());
        temp.setStudentMark(erm.getStudentresult());

        // TODO: 26/03/16 we should update row if it is exist
        examResultModels.add(temp);
        if (activeFragmentID == 4) {
            examResultViewerFragment.updateExamResultContent();
        } else {
            // Toast toast = Toast.makeText(this.getApplicationContext(), temp.getClientName() + " Finish The Exam", Toast.LENGTH_SHORT);
            // toast.show();
        }

    }

    public void dealWithSimpleTextMessage(SimpleTextMessage simplem) {
        if (simplem.getMessageType().equals("TXT")) {
            ChatMessageModel chm = new ChatMessageModel(simplem.getSenderName(), "", "TXT", simplem.getTextMessage(), false);
            chm.setSenderID(simplem.getSenderID());
            chatMessageModelList.add(chm);
        }
        if ((activeFragmentID == 3) && (messageViewerFragment != null)) {
            Log.d("info", " Display on Message Viewer");

            messageViewerFragment.setMessagesList(chatMessageModelList);
            messageViewerFragment.updateMessageListContent();

        }
        if (activeFragmentID == 2) {
            Log.d("info", "Display Message On Counter Only");
            increasetUnreadMessageCounter(simplem.getSenderID());
            if (activeusersfragment != null) {
                activeusersfragment.updateActiveListContent();
            }

        }


    }


    private void dealWithStatusMessage(StatusMessage currSm) {
        ClientModel cm = findCurrentUser(currSm.getUserID());
        cm.setLastStatus(clientStatus[currSm.getStatus()]);
        cm.setStatus(currSm.getStatus());
        if (activeFragmentID == 2 && activeusersfragment != null) {
            activeusersfragment.updateActiveListContent();
        }

    }

    public void addNewActiveClient(UserLogin ul) {
        int resourceID = getResourseId("u" + ul.getUserID(), "drawable", getPackageName());
        if (resourceID == -1) {
            resourceID = R.drawable.unknown;
        }
        ClientModel t = new ClientModel(ul.getUserID(), ul.getUserName(), resourceID);
        t.setLastStatus(clientStatus[0]);
        t.setStatus(0);
        System.out.println();
        if (!(ifUserExistUpdate(ul))) {
            clientsList.add(t);
        }
    }

    private boolean ifUserExistUpdate(UserLogin curr) {
        for (ClientModel ul1 : clientsList) {
            if (curr.getUserID().equals(ul1.getClientID())) {
                ul1.setClientName(curr.getUserName());
                int resourceID = getResourseId("u" + curr.getUserID(), "drawable", getPackageName());
                if (resourceID == -1) {
                    resourceID = R.drawable.unknown;
                }
                // TODO: 25/03/16 re edit this after solve profile image problem
                ul1.setClientImage(resourceID);
                return true;
            }
        }

        return false;
    }

    private boolean ifUserExistUpdate(String uID) {
        for (ClientModel ul1 : clientsList) {
            if (uID.equals(ul1.getClientID())) {
                return true;
            }
        }

        return false;
    }

    private ClientModel findCurrentUser(String cuserID) {
        for (ClientModel ul1 : clientsList) {
            if (cuserID.equals(ul1.getClientID())) {
                return ul1;
            }
        }

        return null;
    }

    private void increasetUnreadMessageCounter(String userId) {
        ClientModel temp = findCurrentUser(userId);
        temp.unreadMsgCounter++;
    }

    private void resetUnreadMessageCounter(String userId) {
        ClientModel temp = findCurrentUser(userId);
        temp.unreadMsgCounter = 0;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    @Override
    public void setSuccessfulLogin(UserLogin ul) {
        this.iam = ul;
    }

    @Override
    public void researchforServer() {
        checkServer.start();

    }

    private void showInvalidUserNameOrPassword() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginfrag.showInvalidLoginMessage();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //  Log.d("LIFE", "Activity pause");
    }

    @Override
    public void run() {
        boolean flag = true;
        while (flag) {
            Log.d("INFO", "hello thread");

            try {
                Thread.sleep(100);
                if (openConnection()) {
                    Log.d("Info", "Connectione done");
                    loginfrag.setClient(client);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginfrag.hideErrorMessage();
                        }
                    });
                    ;
                    flag = false;
                }

            } catch (Exception e) {
                //  Toast.makeText(MainActivity.this,
                // getResources().getText(R.string.unable_to_connect_server), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }


        }


    }

    public int getResourseId(String pVariableName, String pResourcename, String pPackageName) {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    @Override
    public void onFragmentInteraction(int fragmentID) {
        this.activeFragmentID = fragmentID;

    }

    @Override
    public void addNewChatModelMessage(ChatMessageModel cml) {

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        try {

            SendUtil.reConnect(client, iam);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void ShowMessagesViewer(String useriD) {
        ft = fm.beginTransaction();
        if (messageViewerFragment == null) {
            messageViewerFragment = new MessageViewerFragment();
        }
        messageViewerFragment.setUserlogin(iam);
        messageViewerFragment.setClient(client);
        messageViewerFragment.setReciverID(useriD);
        List<ChatMessageModel> temp = SendUtil.getClientUnreadMessages(useriD, chatMessageModelList);
        messageViewerFragment.setMessagesList(temp);
        ft.replace(R.id.fragment_container, messageViewerFragment, "VIEWMSG");
        activeFragmentID = 3;
        ft.addToBackStack(null);
        ft.commit();

    }

    @Override
    public synchronized void AddNewChatModel(ChatMessageModel newCml) {
        chatMessageModelList.add(newCml);

    }
}
