package com.cdms.codrive.classes;

import android.os.Environment;

import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants
{
	public static User user;
	public static User ownerUser;
	public static User keeperUser;
    public static ParseObject Friend;
    public static List<ParseUser> persons=new ArrayList<>();
    public static List<String> personName = new ArrayList<>();
    public static List<Interaction> notification = new ArrayList<>();
    public static List<Interaction> filelog = new ArrayList<>();

    public static final String SEPARATOR = "1";
    public static final String FOLDER = Environment.getExternalStorageDirectory().toString();


    public static void saveLog(String dataObjectId,String keeperId,String ownerId) throws ParseException
    {
        Map<String, Object> params=new HashMap<>();
        params.put("dataObjectId",dataObjectId);
        params.put("ownerId",ownerId);
        params.put("keeperId",keeperId);
        ParseCloud.callFunction("AddStoreRequest", params);
    }

    public static void RespondToStoreRequest(Boolean isAccepted,String InteractionId) throws ParseException
    {
        Map<String, Object> params=new HashMap<>();
        params.put("isAccepted",isAccepted);
        params.put("serverStatusId",InteractionId);
        ParseCloud.callFunction("RespondToStoreRequest", params);
    }

    public static void RespondToRetrieveRequest(Boolean isAccepted,String InteractionId) throws ParseException
    {
        Map<String, Object> params=new HashMap<>();
        params.put("isAccepted",isAccepted);
        params.put("serverStatusId",InteractionId);
        ParseCloud.callFunction("RespondToRetrieveRequest", params);
    }

    public static void getInteractions()
    {
        try {
            notification=Interaction.getServerStatus(Constants.user);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void storeUserData(User fromUser, Interaction.MyData data)
    {
        File f = new File(FOLDER,this.getFileNameForData(fromUser, data));
    }

    public String getFileNameForData(User fromUser, Interaction.MyData data)
    {
        String fileName="";
        fileName+=fromUser.parseUser.getObjectId();
        fileName+=SEPARATOR;
        fileName+=data.getName();
        return fileName;
    }

    //TODO change data types according to need
    public static void storeFile(ParseFile parseFile,String filename) throws ParseException, IOException
    {
        byte[] bytes = parseFile.getData();
        File f=new File(FOLDER+"/"+filename);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bytes);
        fos.close();
    }


}
