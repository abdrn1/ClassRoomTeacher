package com.abd.classroom1;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.widget.Toast;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import Decoder.BASE64Decoder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LoginFragment.OnFragmentInteractionListener, Runnable,
        ActiveUsersFragment.OnFragmentInteractionListener,
        MessageViewerFragment.OnFragmentInteractionListener,
        ExamResultViewerFragment.OnFragmentInteractionListener, MonitorFragment.OnFragmentInteractionListener,
        ClientListAdapter.OnClientListAdapterInteraction,
        AddRemoveSync {

    // begin note : should be save later in the bundle;
    private final int LOGINFRAG = 1;
    private final int ACTIVEUSERSFRAG = 2;
    private final int MESSSAGEVIWERFRAG = 3;
    private final int EXAMRESULTFRAG = 4;
    Client client;
    Kryo kryo;
    FragmentManager fm;
    FragmentTransaction ft;
    // the fragments
    LoginFragment loginfrag;
    ActiveUsersFragment activeusersfragment;
    MessageViewerFragment messageViewerFragment;
    ExamResultViewerFragment examResultViewerFragment;
    MonitorFragment monitorFragment;
    String[] clientStatus;
    ///
    private UserLogin iam =null;
    private List<ChatMessageModel> chatMessageModelList;
    private List<ClientModel> clientsList;
    private List<ExamResultModel> examResultModels;
    private Hashtable<String, List<ChatMessageModel>> allStudentsLists;
    private Thread checkServer;
    private int activeFragmentID = 1;
    private Hashtable<RecivedFileKey, BuildFileFromBytesV2> recivedFilesTable;
    private Handler handler;


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
        allStudentsLists = new Hashtable<>();
        recivedFilesTable = new Hashtable<>();
        examResultModels = new ArrayList<>(); // for saving exam result
        examResultModels.add(aa);
        clientsList = new ArrayList<>();// for saving active clients
        clientStatus = getResources().getStringArray(R.array.client_status); // String Array of clients status
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

        handler = new Handler();


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
            activeFragmentID = ACTIVEUSERSFRAG;
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
            if (activeFragmentID == EXAMRESULTFRAG) {
                examResultViewerFragment.updateExamResultContent();
            } else {
                if (iam != null && ft != null) {
                    examResultViewerFragment = ExamResultViewerFragment.newInstance(examResultModels);
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_container, examResultViewerFragment, "EXAMEXAM");
                    examResultViewerFragment.setL1(examResultModels);
                    activeFragmentID = EXAMRESULTFRAG;
                    ft.addToBackStack(null);
                    //examResultViewerFragment.set(iam);
                    ft.commit();


                }

            }
        } else if (id == R.id.nmi_active_users) {

            if (activeFragmentID == ACTIVEUSERSFRAG) {
                activeusersfragment.updateActiveListContent();
            } else {
                if (iam != null && ft != null) {
                    activeusersfragment = new ActiveUsersFragment();
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_container, activeusersfragment, "EXAMEXAM");
                    activeusersfragment.setClient(client);
                    activeusersfragment.setActiveUsersList(clientsList);
                    activeusersfragment.setUserlogin(iam);
                    activeFragmentID = ACTIVEUSERSFRAG;
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
        client = new Client((1024*1024), (1024*1024)/10);
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
        kryo.register(MonitorRequestMessage.class);
        kryo.register(ScreenshotMessage.class);
        kryo.register(BoardScreenshotMessage.class);
        kryo.register(CapturedImageMessage.class); // class to send captured image
        kryo.register(ShowOnBoardMessage.class);
        kryo.register(CommandsMessages.class);
    }

    public boolean openConnection() throws Exception {
        client.start();
        InetAddress address = client.discoverHost(54777, 5000);
        client.connect(5000, address, 9995, 54777);
        client.setKeepAliveUDP(7000);
        client.setKeepAliveTCP(7000);
        client.setTimeout(50000);

        client.addListener(new Listener() {
            public void received(Connection c, Object ob) {
                if (ob instanceof SimpleTextMessage) {
                    Log.d("SIMPLE", "New Simple Message Recived");
                    SimpleTextMessage stm = (SimpleTextMessage) ob;
                    if (stm.getMessageType().equals("TXT")){
                        dealWithSimpleTextMessage(stm);
                    }else if (stm.getMessageType().equals("OK")){
                       dealWithLikeMessage(stm);

                    }

                } else if (ob instanceof UserLogin) {
                    if (!((UserLogin) ob).isLogin_Succesful()) {
                        showInvalidUserNameOrPassword();
                        return;
                    }
                    Log.d("INFO", "New User Login Packet");
                    if (((UserLogin) ob).getUserType().equals("TEACHER") && (iam ==null)) {
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
                            iam = (UserLogin) ob;
                            activeusersfragment.setClient(client);
                            activeusersfragment.setUserlogin((UserLogin) ob);
                            activeusersfragment.setActiveUsersList(clientsList);
                            //activeusersfragment.setChatMessageModelList(chatMessageModelList);
                            activeusersfragment.setAllStudentsLists(allStudentsLists);
                            ft.commit();
                            //activeFragmentID = ACTIVEUSERSFRAG;
                            Log.d("INFO", "Succesfull Log IN");
                        } else {
                            loginfrag.showInvalidLoginMessage();
                        }
                    } else if (((UserLogin) ob).getUserType().equals("STUDENT")) {
                        addNewActiveClient((UserLogin) ob);
                        if (activeFragmentID == ACTIVEUSERSFRAG && activeusersfragment !=null ) {

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
                }else if(ob instanceof ScreenshotMessage){
                    if(monitorFragment != null)
                        monitorFragment.screenshotReceived((ScreenshotMessage) ob);
                }else if (ob instanceof FileChunkMessageV2) {
                    if (((FileChunkMessageV2) ob).getFiletype().equals(FileChunkMessageV2.FILE)) {
                        Log.d("FILE", "New File Recived");
                       dealWithFileMessage(((FileChunkMessageV2) ob));
                    }

                }else if (ob instanceof CapturedImageMessage){

                    try {
                        dealWithCapturedImageMessage((CapturedImageMessage) ob);
                    }catch (Exception ex1){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Failed to Load Captured Image", Toast.LENGTH_SHORT).show();
                            }
                        });

                        ex1.printStackTrace();
                    }



                }
            }
        });

        return true;


    }

    private synchronized  void dealWithCapturedImageMessage(CapturedImageMessage cim) throws Exception {

        Bitmap bm;
        ChatMessageModel chm = new ChatMessageModel(cim.getSenderName(),"","IMG","IMGE",false);
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] imageBytes = decoder.decodeBuffer(cim.getPicture());
        Log.d("DECODE","Phase 1");
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        bm = BitmapFactory.decodeStream(bis);
        Log.d("DECODE","Phase 2");
        chm.setImage(bm);
        chm.setSimpleMessage(cim.getFileName());
        chm.setMessageType("IMG");// Necessary  to display imagechatMessages.add(chm);
        String savepath = Environment.getExternalStorageDirectory().getPath();
        savepath =savepath + "/Classroom/pics/"+cim.getFileName();
        chm.setFilepath(savepath);
        allStudentsLists.get(cim.getSenderID()).add(chm);
        Log.d("DECODE","Phase 3");
        writeByteImageTofile(imageBytes,cim.getFileName());
        if ((activeFragmentID == MESSSAGEVIWERFRAG) && (messageViewerFragment!=null)) {
            messageViewerFragment.updateAdapterchanges();
        }else{
            increasetUnreadMessageCounter(cim.getSenderID());
            if (activeusersfragment != null) {
                activeusersfragment.updateActiveListContent();
            }
        }

    }

    private void writeByteImageTofile(byte[] imageBytes,String imagefileName) throws Exception{
        String savepath = Environment.getExternalStorageDirectory().getPath();
        savepath =savepath + "/Classroom/pics/";
        File destination = new File(savepath,imagefileName);
        FileOutputStream fo;

            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(imageBytes);
            fo.close();
            //Toast.makeText(getApplicationContext(), "Write IMGE DONEe", Toast.LENGTH_SHORT).show();

    }


    public synchronized void dealWithFileMessage(FileChunkMessageV2 fcmv2) {
        BuildFileFromBytesV2 buildfromBytesV2=null;
        ChatMessageModel icm;
        try {

            String savepath = Environment.getExternalStorageDirectory().getPath();
            Log.d("INFO", "File Chunk Recived");
            //recive the first packet from new file
            if (fcmv2.getChunkCounter() == 1L) {
              //  final FileChunkMessageV2 tfcmv2 = fcmv2;
                Log.d("INFO PAth=", savepath + "/Classrom");
                icm = new ChatMessageModel();
                icm.setSenderID(fcmv2.getSenderID());
                icm.setSenderName(fcmv2.getSenderName());
                icm.setFilepath(savepath + "/Classrom/"+fcmv2.getFileName());
                icm.setIsSelf(false);
                buildfromBytesV2 = new BuildFileFromBytesV2(savepath + "/Classrom/");
                buildfromBytesV2.setChatMessageModel(icm);
                buildfromBytesV2.constructFile(fcmv2);
                recivedFilesTable.put(new RecivedFileKey(fcmv2.senderID, fcmv2.getFileName()), buildfromBytesV2);

            } else {
                BuildFileFromBytesV2 bffb = recivedFilesTable.get(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()));
                if (bffb != null) {

                    Log.d("INFO", "Current File Chunk: " + Long.toString(fcmv2.getChunkCounter()));
                    if (bffb.constructFile(fcmv2)) {
                        recivedFilesTable.remove(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()));
                        icm = bffb.getChatMessageModel();
                        if (SendUtil.checkIfFileIsImage(fcmv2.getFileName())) {
                            // Bitmap bm = BitmapFactory.decodeFile(savepath + "/Classrom/" + fcmv2.getFileName());
                            String tempImagePath = savepath + "/Classrom/" + fcmv2.getFileName();
                            // Bitmap bm = ScalingUtilities.fitImageDecoder(tempImagePath,mDstWidth,mDstHeight);
                            Bitmap bm = ScalDownImage.decodeSampledBitmapFromResource(tempImagePath, 80, 80);
                            icm.setImage(bm);
                            icm.setMessageType("IMG");
                            icm.setSimpleMessage(fcmv2.getFileName());
                        } else {
                            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.filecompleteicon);
                            icm.setImage(bm);
                            icm.setSimpleMessage(fcmv2.getFileName());
                            icm.setMessageType("FLE");
                        }

                        allStudentsLists.get(fcmv2.getSenderID()).add(icm);
                        if((activeFragmentID==MESSSAGEVIWERFRAG) && (messageViewerFragment!=null) ){
                            messageViewerFragment.updateAdapterchanges();

                        }else{
                            increasetUnreadMessageCounter(icm.getSenderID());
                            if (activeusersfragment != null) {
                                activeusersfragment.updateActiveListContent();
                            }
                        }
                        Log.d("INFO", "EOF, FILE REcived Completely");
                    }
                    /// SendUtil.sendFileChunkToRecivers(clientTable, fcmv2, tRecivers);
                }
            }
        } catch (Exception ex) {
            recivedFilesTable.remove(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()));
            Toast.makeText(getApplicationContext(), "Error While Recive file from Student: "+fcmv2.getSenderName()+", "+fcmv2.getSenderID(), Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
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

    private void dealWithLikeMessage(SimpleTextMessage likem){
        if (likem.getMessageType().equals("OK")) {
            ChatMessageModel chm = new ChatMessageModel(likem.getSenderName(), "", "OK", likem.getTextMessage(), false);
            chm.setSenderID(likem.getSenderID());
            chatMessageModelList = allStudentsLists.get(likem.getSenderID());
            if (chatMessageModelList != null) {
                chatMessageModelList.add(chm);
            }

            // unread counter

            if ((activeFragmentID == MESSSAGEVIWERFRAG) && (messageViewerFragment != null)) {
                Log.d("info", " Display on Message Viewer");

                // messageViewerFragment.setMessagesList(chatMessageModelList);
                //messageViewerFragment.addNewMessage(simplem,false);
                messageViewerFragment.updateMessageListContent();

            } else {
                Log.d("info", "Display Message On Counter Only");
                increasetUnreadMessageCounter(likem.getSenderID());
                if (activeusersfragment != null) {
                    activeusersfragment.updateActiveListContent();
                }

            }

        }
    }

    public void dealWithSimpleTextMessage(SimpleTextMessage simplem) {
        if (simplem.getMessageType().equals("TXT")) {
            ChatMessageModel chm = new ChatMessageModel(simplem.getSenderName(), "", "TXT", simplem.getTextMessage(), false);
            chm.setSenderID(simplem.getSenderID());
            chatMessageModelList = allStudentsLists.get(simplem.getSenderID());
            if (chatMessageModelList != null) {
                chatMessageModelList.add(chm);
            }

        }
        if ((activeFragmentID == MESSSAGEVIWERFRAG) && (messageViewerFragment != null)) {
            Log.d("info", " Display on Message Viewer");

            // messageViewerFragment.setMessagesList(chatMessageModelList);
            //messageViewerFragment.addNewMessage(simplem,false);
            messageViewerFragment.updateMessageListContent();

        } else {
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
        if (activeFragmentID == ACTIVEUSERSFRAG && activeusersfragment != null) {
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
            allStudentsLists.put(ul.getUserID(), new ArrayList<ChatMessageModel>());
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

        return  false;
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
        if (temp !=null)
        temp.unreadMsgCounter++;
    }

    private void resetUnreadMessageCounter(String userId) {
        ClientModel temp = findCurrentUser(userId);
        if (temp !=null)
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
    public void addNewTextMessageFromMessageViewer(SimpleTextMessage sm) {
        // no longer needed
     /*   if (sm.getMessageType().equals("TXT")) {
            ChatMessageModel chm = new ChatMessageModel(sm.getSenderName(), "", "TXT", sm.getTextMessage(), false);
            chm.setSenderID(sm.getSenderID());
            chatMessageModelList.add(chm);
        }*/
    }

    @Override
    public void addNewChatModelMessage(ChatMessageModel cml) {

    }

    @Override
    public void showMonitor(String[] receivers) {
        ShowMonitorViewer(receivers);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        try {

            SendUtil.reConnect(client, iam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void ShowMonitorViewer(String[] receivers) {
        ft = fm.beginTransaction();
        if (monitorFragment == null) {
            monitorFragment = new MonitorFragment();
        }
        //monitorFragment
        monitorFragment.setUserLogin(iam);
        monitorFragment.setReceivers(receivers);
        monitorFragment.setClient(client);
        ft.replace(R.id.fragment_container, monitorFragment, "MONITOR");
        activeFragmentID = 4;
        ft.addToBackStack(null);
        ft.commit();

    }

    public void ShowMessagesViewer(String useriD) {
        ft = fm.beginTransaction();
        if (messageViewerFragment == null) {
            messageViewerFragment = new MessageViewerFragment();
        }
        messageViewerFragment.setUserlogin(iam);
        messageViewerFragment.setClient(client);
        messageViewerFragment.setReciverID(useriD);
        //  List<ChatMessageModel> temp = SendUtil.getClientUnreadMessages(useriD, chatMessageModelList);
        messageViewerFragment.setMessagesList(allStudentsLists.get(useriD));
        ft.replace(R.id.fragment_container, messageViewerFragment, "VIEWMSG");
        activeFragmentID = MESSSAGEVIWERFRAG;
        ft.addToBackStack(null);
        ft.commit();

    }

    @Override
    public synchronized void AddNewChatModel(ChatMessageModel newCml) {
        chatMessageModelList.add(newCml);

    }
}
