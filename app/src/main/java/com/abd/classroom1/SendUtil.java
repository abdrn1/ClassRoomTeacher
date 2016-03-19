package com.abd.classroom1;

import android.util.Log;

import com.esotericsoftware.kryonet.Client;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

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

}
