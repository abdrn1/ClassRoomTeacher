package com.abd.classroom1.service;

/**
 * Created by Abd on 5/26/2016.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.abd.classroom1.BuildFileFromBytesV2;
import com.abd.classroom1.ChatMessageModel;
import com.abd.classroom1.ClientModel;
import com.abd.classroom1.ExamResultMessage;
import com.abd.classroom1.ExamResultModel;
import com.abd.classroom1.FileChunkMessageV2;
import com.abd.classroom1.MainActivity;
import com.abd.classroom1.OnServiceInteractionListener;
import com.abd.classroom1.QuestionItem;
import com.abd.classroom1.R;
import com.abd.classroom1.RecivedFileKey;
import com.abd.classroom1.ScalDownImage;
import com.abd.classroom1.SendUtil;
import com.abd.classroom1.SimpleTextMessage;
import com.abd.classroom1.StatusMessage;
import com.abd.classroom1.UserLogin;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class MessageListener extends Listener {
    public static final int NOTIFICATION_ID = 1337;
    private static final String NOTIFY_KEYWORD = "snicklefritz";
    private static final int POLL_PERIOD = 60000;
    private List<ChatMessageModel> chatMessageModelList;
    private Service ownerService;
    private Hashtable<String, List<ChatMessageModel>> allStudentsLists;
    boolean sBounded = false;
    protected OnServiceInteractionListener activity;
    private Hashtable<RecivedFileKey, BuildFileFromBytesV2> recivedFilesTable;
    List<QuestionItem> tQuestionsList;
    public boolean newExam = false;
    private List<ClientModel> clientsList;
    private UserLogin iam;
    public List<ExamResultModel> examResultModels;
    String[] clientStatus;

    public MessageListener(Hashtable<String, List<ChatMessageModel>> allStudentsL, Service s) {
        this.allStudentsLists = allStudentsL;
        this.ownerService = s;
        recivedFilesTable = new Hashtable<>();
        examResultModels = new ArrayList<>();
        ExamResultModel aa = new ExamResultModel();
        aa.setClientID("105");
        aa.setClientName("Radwan");
        aa.setClientImage(R.drawable.u27);
        aa.setStudentMark(25);
        aa.setExamMark(50);
        examResultModels.add(aa);
        clientStatus = ownerService.getResources().getStringArray(R.array.client_status);
    }

    public UserLogin getIam() {
        return iam;
    }

    public void setIam(UserLogin iam) {
        this.iam = iam;
    }

    public List<ClientModel> getClientsList() {
        return clientsList;
    }

    public void setClientsList(List<ClientModel> clientsList) {
        this.clientsList = clientsList;
    }

    public boolean issBounded() {
        return sBounded;
    }

    public void setsBounded(boolean sBounded) {
        this.sBounded = sBounded;
    }

    public List<ChatMessageModel> getChatMessageModelList() {
        return chatMessageModelList;
    }

    public void setChatMessageModelList(List<ChatMessageModel> chatMessageModelList) {
        this.chatMessageModelList = chatMessageModelList;
    }

    public Hashtable<String, List<ChatMessageModel>> getAllStudentsLists() {
        return allStudentsLists;
    }

    public void setAllStudentsLists(Hashtable<String, List<ChatMessageModel>> allStudentsLists) {
        this.allStudentsLists = allStudentsLists;
    }

    @Override
    public void received(Connection connection, Object ob) {
        String title;
        String content;
        if (ob instanceof SimpleTextMessage) {
            SimpleTextMessage simplem = (SimpleTextMessage) ob;
            // if (simplem.getMessageType().equals("TXT")) {

            SimpleTextMessage stm = (SimpleTextMessage) ob;
            dealWithSimpleTextMessage(stm);
            // Show Notification
            Log.i("newMSG", "New Simple Text Message Recived");
            title = ownerService.getResources().getString(R.string.message_from);
            title = title + " " + simplem.getSenderName();
            content = simplem.getTextMessage();
            showNotification(title, content);
            //  }
        } else if (ob instanceof FileChunkMessageV2) {
            if (((FileChunkMessageV2) ob).getFiletype().equals(FileChunkMessageV2.FILE)) {
                Log.d("INFO", "New File Recived");
                dealWithFileMessage(((FileChunkMessageV2) ob));
            }
        } else if (ob instanceof UserLogin) {
            if (!((UserLogin) ob).isLogin_Succesful()) {
                return;
            } else if (iam != null) {
                addNewActiveClient((UserLogin) ob);

            }
        } else if (ob instanceof ExamResultMessage) {
            Log.d("INFO", "Exam Result Message Recived");
            dealWithExamResultMessage((ExamResultMessage) ob);
        } else if (ob instanceof StatusMessage) {
            //dealWithStatusMessage((StatusMessage) ob);
            StatusMessage mm = (StatusMessage) ob;
           // showNotification("Status",mm.getUserID()+ ",,,"+ mm.getStatus());
            dealWithStatusMessage(mm);
        }
    }

    public void dealWithExamResultMessage(ExamResultMessage erm) {
        ExamResultModel temp = new ExamResultModel();
        temp.setClientID(erm.getSenderID());
        temp.setClientName(erm.getSenderName());
        int resourceID = getResourseId("u" + erm.getSenderID(), "drawable", ownerService.getPackageName());
        if (resourceID == -1) {
            resourceID = R.drawable.unknown;
        }
        temp.setClientImage(resourceID);
        temp.setExamMark(erm.getExamresult());
        temp.setStudentMark(erm.getStudentresult());
        temp.setExamFileName(erm.getExamFileName());
        ExamResultModel aa = new ExamResultModel();
        aa.setClientID("ID");
        aa.setClientName("درجة الطالب");
        aa.setClientImage(R.drawable.u27);
        aa.setStudentMark(25);
        aa.setExamMark(50);


        // TODO: 26/03/16 we should update row if it is exist
        examResultModels.add(temp);
        showNotification("Quiz Result","Result From: "+erm.getSenderName());


    }

    private void dealWithStatusMessage(StatusMessage currSm) {
        try {

            ClientModel cm = findCurrentUser(currSm.getUserID());
            if (cm != null) {
                ///showNotification("Status",currSm.getUserID()+ ",,,"+ clientStatus[currSm.getStatus()]);

                if (cm.getStatus() != currSm.getStatus()) {
                    cm.setLastStatus(clientStatus[currSm.getStatus()]);
                    cm.setStatus(currSm.getStatus());
                    if (sBounded) {
                        activity.updateClientsList();
                    }
                }
            }
        } catch (Exception ex) {

        }
    }


    public void addNewActiveClient(UserLogin ul) {
        Log.d("newclient", "NEw Client Here");
        if (!(ul.getUserID().equals(iam.getUserID()))) {

            String clientStatus[] = ownerService.getResources().getStringArray(R.array.client_status);

            int resourceID = getResourseId("u" + ul.getUserID(), "drawable", ownerService.getPackageName());
            Log.d("IMGID","Image ID :" +  resourceID);
            if ((resourceID == -1) || (resourceID == 0)) {
                resourceID = R.drawable.unknown;
            }
            ClientModel t = new ClientModel(ul.getUserID(), ul.getUserName(), resourceID);
            t.setLastStatus(clientStatus[ul.getMyStatus()]);
            t.setStatus(ul.getMyStatus());
           // t.setLastStatus(clientStatus[0]);


            if (!(ifUserExistUpdate(ul))) {
                clientsList.add(t);
                allStudentsLists.put(ul.getUserID(), new ArrayList<ChatMessageModel>());
            }

            if (sBounded) {
                activity.updateClientsList();
            }
        }
        showNotification("NEW CLient :", ul.getUserID());


    }

    private boolean ifUserExistUpdate(UserLogin curr) {
        for (ClientModel ul1 : clientsList) {
            if (curr.getUserID().equals(ul1.getClientID())) {
                // i will stop update of user
                /*ul1.setClientName(curr.getUserName());
                int resourceID = getResourseId("u" + curr.getUserID(), "drawable", ownerService.getPackageName());
                if (resourceID == -1) {
                    resourceID = R.drawable.unknown;
                }
                // TODO: 25/03/16 re edit this after solve profile image problem
                ul1.setClientImage(resourceID);*/
                ul1.setLastStatus(clientStatus[0]);
                ul1.setStatus(0);
                return true;
            }

        }

        return false;
    }

    public int getResourseId(String pVariableName, String pResourcename, String pPackageName) {
        try {
            return ownerService.getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    public void dealWithFileMessage(FileChunkMessageV2 fcmv2) {
        BuildFileFromBytesV2 buildfromBytesV2;
        ChatMessageModel icm;
        try {

            String savepath = Environment.getExternalStorageDirectory().getPath();
            Log.d("INFO", "File Chunk Recived");
            //recive the first packet from new file
            if (fcmv2.getChunkCounter() == 1L) {
                //  final FileChunkMessageV2 tfcmv2 = fcmv2;
                Log.d("INFO PAth=", savepath + "/Classroom");
                icm = new ChatMessageModel();
                icm.setSenderID(fcmv2.getSenderID());
                icm.setSenderName(fcmv2.getSenderName());
                icm.setFilepath(savepath + "/Classroom/" + fcmv2.getFileName());
                icm.setIsSelf(false);
                buildfromBytesV2 = new BuildFileFromBytesV2(savepath + "/Classroom/");
                buildfromBytesV2.setChatMessageModel(icm);
                // buildfromBytesV2.constructFile(fcmv2);
                recivedFilesTable.put(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()), buildfromBytesV2);

            } else {
                buildfromBytesV2 = recivedFilesTable.get(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()));
            }
            if (buildfromBytesV2 != null) {

                Log.d("INFO", "Current File Chunk: " + Long.toString(fcmv2.getChunkCounter()));
                if (buildfromBytesV2.constructFile(fcmv2)) {
                    recivedFilesTable.remove(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()));
                    icm = buildfromBytesV2.getChatMessageModel();
                    if (SendUtil.checkIfFileIsImage(fcmv2.getFileName())) {
                        // Bitmap bm = BitmapFactory.decodeFile(savepath + "/Classrom/" + fcmv2.getFileName());
                        String tempImagePath = savepath + "/Classroom/" + fcmv2.getFileName();
                        // Bitmap bm = ScalingUtilities.fitImageDecoder(tempImagePath,mDstWidth,mDstHeight);
                        Bitmap bm = ScalDownImage.decodeSampledBitmapFromResource(tempImagePath, 80, 80);
                        icm.setImage(bm);
                        icm.setMessageType("IMG");
                        icm.setSimpleMessage(fcmv2.getFileName());
                    } else {
                        Bitmap bm = BitmapFactory.decodeResource(ownerService.getResources(), R.drawable.filecompleteicon);
                        icm.setImage(bm);
                        icm.setSimpleMessage(fcmv2.getFileName());
                        icm.setMessageType("FLE");
                    }

                    allStudentsLists.get(fcmv2.getSenderID()).add(icm);
                    String title = ownerService.getResources().getString(R.string.message_from);
                    title = title + " " + fcmv2.getSenderName();
                    String content = "File : " + fcmv2.getFileName();
                    showNotification(title, content);
                    if (sBounded) {
                        activity.updateMessageViewer(fcmv2.getSenderID());
                    } else {
                        increasetUnreadMessageCounter(fcmv2.getSenderID());

                    }
                    Log.d("INFO", "EOF, FILE REcived Completely");
                }
                /// SendUtil.sendFileChunkToRecivers(clientTable, fcmv2, tRecivers);
            }

        } catch (Exception ex) {
            recivedFilesTable.remove(new RecivedFileKey(fcmv2.getSenderID(), fcmv2.getFileName()));
            ex.printStackTrace();
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

        } else if (simplem.getMessageType().equals("OK")) {
            ChatMessageModel chm = new ChatMessageModel(simplem.getSenderName(), "", "OK", simplem.getTextMessage(), false);
            chm.setSenderID(simplem.getSenderID());
            chatMessageModelList = allStudentsLists.get(simplem.getSenderID());
            if (chatMessageModelList != null) {
                chatMessageModelList.add(chm);
            }
        }
        if (sBounded) {
            activity.updateMessageViewer(simplem.getSenderID());
        } else {
            increasetUnreadMessageCounter(simplem.getSenderID());
        }

    }

    private void increasetUnreadMessageCounter(String userId) {
        ClientModel temp = findCurrentUser(userId);
        if (temp != null)
            temp.unreadMsgCounter++;
    }

    private ClientModel findCurrentUser(String cuserID) {
        for (ClientModel ul1 : clientsList) {
            if (cuserID.equals(ul1.getClientID())) {
                return ul1;
            }
        }

        return null;
    }


    public void showNotification(String title, String content) {

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        //Intent resultIntent = new Intent(ownerService, MainActivity.class);
        //PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0


        Intent resultIntent = new Intent(ownerService, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.setAction("android.intent.action.MAIN");
        resultIntent.addCategory("android.intent.category.LAUNCHER");

        int mId = 2525;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ownerService)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(soundUri)
                .setAutoCancel(true);
        // .addAction(0, "Remind", pIntent)

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ownerService);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(ownerService, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


       /* PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );*/
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) ownerService.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());

    }


}
