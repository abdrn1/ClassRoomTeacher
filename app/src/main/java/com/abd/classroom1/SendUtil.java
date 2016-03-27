package com.abd.classroom1;

import android.util.Log;

import com.esotericsoftware.kryonet.Client;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abd on 3/7/2016.
 */
public class SendUtil {

    public static void readAndSendFile(String path, Client client, UserLogin currentUser, String[] recivers, String fileType) throws IOException {
        //File f = new File(path) ;


        Log.d("INFO FIle Name", FilenameUtils.getName(path));
        Log.d("INFO","CONVERT TO ARRA");
        FileChunkMessageV2 fmsg = new FileChunkMessageV2();
        fmsg.setSenderID(currentUser.getUserID());
        fmsg.setSenderName(currentUser.getUserName());
        fmsg.setFileName(FilenameUtils.getName(path));
        fmsg.setFiletype(fileType);
        fmsg.setRecivers(recivers);
        FileSenderThreadV2 ftV2 = new FileSenderThreadV2(client,path,fmsg);
        ftV2.start();
        Log.d("INFO", "Start Thread");
    }


    public static void reConnect(Client cl, UserLogin iam) throws IOException {

        if ((cl != null) && (iam != null)) {
            if (!cl.isConnected()) {
                cl.reconnect();
                cl.sendTCP(iam);
                Log.d("con", "Try To Reconnect");
                Log.d("con", "Iam " + iam.getUserID());
                Log.d("con", "Iam " + iam.getUserType());

            }
        }

    }

    public static void convertTextMessageToChatMessageModl(TextMeesage tm, List<ChatMessageModel> cmlLsit) {
        String[] recivers = tm.getRecivers();
        if (recivers != null) {
            for (String rec : recivers) {
                ChatMessageModel temp = new ChatMessageModel();
                temp.setSenderID(rec);
                temp.setSenderName(tm.getSenderName());
                temp.setSimpleMessage(tm.getTextMessage());
                temp.setMessageType(tm.getMessageType());
                // TODO: 27/03/16 image and file message

                cmlLsit.add(temp);

            }
        }
    }

    public static void convertFileChunkToChatMessageModl(FileChunkMessageV2 tm, List<ChatMessageModel> cmlLsit) {
        String[] recivers = tm.getRecivers();
        if (recivers != null) {
          /*  for(String rec :recivers){
                ChatMessageModel temp = new ChatMessageModel();
                temp.setSenderID(rec);
                temp.setSenderName(tm.getSenderName());
                temp.setSimpleMessage(tm.getTextMessage());
                temp.setMessageType(tm.getFiletype());
                // TODO: 27/03/16 image and file message

                cmlLsit.add(temp);

            }*/
        }
    }

    public static boolean checkIfFileIsImage(String fileName) {

        String ext = null;
        int i = fileName.lastIndexOf('.');
        if (i > 0 && i < (fileName.length() - 1)) {
            ext = fileName.substring(i + 1).toLowerCase();
        }
        if (ext == null)
            return false;
        else if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png") && !ext.equals("gif"))
            return false;
        else
            return true;
    }


    public static List<ChatMessageModel> getClientUnreadMessages(String fromID, List<ChatMessageModel> cmlLsit) {
        List<ChatMessageModel> templ = new ArrayList<>();
        for (ChatMessageModel cml : cmlLsit) {
            if (cml.getSenderID().equals(fromID)) {
                templ.add(cml);
            }
        }

        return templ;
    }



}
