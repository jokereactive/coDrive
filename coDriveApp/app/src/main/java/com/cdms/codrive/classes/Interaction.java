package com.cdms.codrive.classes;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Interaction {
	
	private ParseObject po;
    private ParseObject serverStatus;
	private User fromUser;
	private User toUser;
	private MyData data;
	private static final String TAG = "Interaction";
	private static final String FIELD_FROM_USER = "from";
	private static final String FIELD_TO_USER = "to";
	private static final String FIELD_DATA = "data";
	private static final String FIELD_STATUS = "status"; //TODO to be looked into
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_LOG = "log";
	private static final String TABLE_INTERACTION = "Interaction";
	
	public Interaction() {
		this.po = new ParseObject(TABLE_INTERACTION);
		this.fromUser = null;
		this.toUser = null;
		this.data = null;
        this.serverStatus=null;
	}

    public ParseObject getServerStatus() {
        return serverStatus;
    }

    @Override
    public String toString() {
        return this.getFromUser().toString()+this.getToUser().toString()+this.getStatus().toString()+this.getType().toString();
    }

    public String getObjectId() {
        return this.po.getObjectId();
    }

    public enum Status
	{
        REQUEST_SENT,ON_SERVER,REQUEST_ACCEPTED, REQUEST_COMPLETED, REQUEST_REJECTED
	}
	
	public enum Type
	{
		STORE, RETRIEVE
	}

	public Type getType() {
		return Type.valueOf(this.po.getString(FIELD_TYPE).toUpperCase());
	}

	public void setType(Type type) {
		this.po.put(FIELD_TYPE, type.toString().toLowerCase());
	}

	public Status getStatus() {
		return Status.valueOf(this.po.getString(FIELD_STATUS).toUpperCase());
	}

	public void setStatus(Status status) {
		this.po.put(FIELD_STATUS, status.toString().toLowerCase());
	}
	
	public User getFromUser() {
		return fromUser;
	}

	public void setFromUser(User fromUser) {
		this.po.put(FIELD_FROM_USER, fromUser.parseUser);
		this.fromUser = fromUser;
	}

	public User getToUser() {
		return toUser;
	}

	public void setToUser(User toUser) {
		this.po.put(FIELD_TO_USER, toUser.parseUser);
		this.toUser = toUser;
	}

	public void setFile(String path,ContentResolver contentResolver) throws ParseException
	{
		File file = new File(path);
		InputStream inputStream = null;
		try {
			inputStream = contentResolver.openInputStream(Uri.fromFile(file));
		} catch (Exception e) {
			Log.e(TAG,e.getMessage(),e);
		}
          ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

          // this is storage overwritten on each iteration with bytes
          int bufferSize = 1024;
          byte[] buffer = new byte[bufferSize];

          // we need to know how may bytes were read to write them to the byteBuffer
          int len = 0;
          try {
			while ((len = inputStream.read(buffer)) != -1) {
			    byteBuffer.write(buffer, 0, len);
			  }
		} catch (Exception e) {
			Log.e(TAG,e.getMessage(),e);
		}

         // and then we can return your byte array.
         byte[] byteArray=byteBuffer.toByteArray();
         ParseFile parseFile = new ParseFile(file.getName(), byteArray);
         parseFile.save();
         //convert to MyData object
         MyData data = new MyData(file,parseFile);
         //save all objects
         data.save();
         this.po.put(FIELD_DATA, data.po);
         this.po.save();
	}
	
	public String getFileLink()
	{
		return this.data.getFileLink();
	}
	
	public MyData getFileMetaData()
	{
		return this.data;
	}
	
	public void removeFile()
	{
		this.po.remove(FIELD_DATA);
	}
	
	public void save() throws ParseException
	{
		this.po.save();
	}

    public static List<Interaction> getServerStatus(User user) throws ParseException {
        final String KEY_KEEPER = "keeper";
        final String KEY_OWNER = "owner";
        final String TABLE_INTERACTION = "Interaction";
        final String TABLE_SERVER_STATUS = "ServerStatus";
        final String TABLE_DATA = "Data";
        //user query to tell server whose queries we need
        ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
        queryUser.whereEqualTo("objectId", user.parseUser.getObjectId());
        //Interaction query parts to tell we need objects where either the from or to is our user
        List<ParseQuery<ParseObject>> listQueries = new ArrayList<>();
        ParseQuery<ParseObject> querySS1 = new ParseQuery<>(TABLE_SERVER_STATUS);
        querySS1.whereMatchesQuery(KEY_KEEPER, queryUser);
        listQueries.add(querySS1);
        ParseQuery<ParseObject> querySS2 = new ParseQuery<>(TABLE_SERVER_STATUS);
        querySS2.whereMatchesQuery(KEY_OWNER, queryUser);
        listQueries.add(querySS2);
        //constructing final query
        ParseQuery<ParseObject> querySS = ParseQuery.or(listQueries);
        querySS.setLimit(1000);
        querySS.include(TABLE_INTERACTION.toLowerCase());
        querySS.include(TABLE_INTERACTION.toLowerCase() + "." + FIELD_FROM_USER);
        querySS.include(TABLE_INTERACTION.toLowerCase() + "." + FIELD_TO_USER);
        querySS.include(TABLE_INTERACTION.toLowerCase() + "." + FIELD_DATA); //need to add the same line to parts if need be
        //downloading and converting parse objects
        List<ParseObject> listPo = querySS.find();
        List<Interaction> listInteractions = new ArrayList<Interaction>();
        for(ParseObject each2 : listPo)
        {
            ParseObject each = each2.getParseObject(TABLE_INTERACTION.toLowerCase());
            Interaction interaction = new Interaction();
            // TODO add from to user values if added later
            User fromUser = new User();
            fromUser.parseUser = each.getParseUser(FIELD_FROM_USER);
            interaction.fromUser = fromUser;
            User toUser = new User();
            toUser.parseUser = each.getParseUser(FIELD_TO_USER);
            interaction.toUser = toUser;
            //assigning parse object for other values to be fetched
            interaction.po = each;
            //assigning data objects
            interaction.data = interaction.new MyData(each.getParseObject(FIELD_DATA));
            interaction.serverStatus=each2;
            listInteractions.add(interaction);
        }
        return listInteractions;
    }

    public static List<Interaction> getMyOwnerInteractions(User userOwner) throws ParseException {
        //final String KEY_KEEPER = "keeper";
        final String KEY_OWNER = "owner";
        final String TABLE_INTERACTION = "Interaction";
        final String TABLE_SERVER_STATUS = "ServerStatus";
        final String TABLE_DATA = "Data";
        //user query to tell server whose queries we need
        ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
        queryUser.whereEqualTo("objectId", userOwner.parseUser.getObjectId());
        //Interaction query parts to tell we need objects where user is the owner
        ParseQuery<ParseObject> querySS = new ParseQuery<>(TABLE_SERVER_STATUS);
        querySS.whereMatchesQuery(KEY_OWNER, queryUser);;
        querySS.setLimit(1000);
        querySS.include(TABLE_INTERACTION.toLowerCase());
        querySS.include(TABLE_INTERACTION.toLowerCase() + "." + FIELD_FROM_USER);
        querySS.include(TABLE_INTERACTION.toLowerCase() + "." + FIELD_TO_USER);
        querySS.include(TABLE_INTERACTION.toLowerCase() + "." + FIELD_DATA); //need to add the same line to parts if need be
        //downloading and converting parse objects
        List<ParseObject> listPo = querySS.find();
        List<Interaction> listInteractions = new ArrayList<Interaction>();
        for(ParseObject each2 : listPo)
        {
            ParseObject each = each2.getParseObject(TABLE_INTERACTION.toLowerCase());
            Interaction interaction = new Interaction();
            // TODO add from to user values if added later
            User fromUser = new User();
            fromUser.parseUser = each.getParseUser(FIELD_FROM_USER);
            interaction.fromUser = fromUser;
            User toUser = new User();
            toUser.parseUser = each.getParseUser(FIELD_TO_USER);
            interaction.toUser = toUser;
            //assigning parse object for other values to be fetched
            interaction.po = each;
            //assigning data objects
            interaction.data = interaction.new MyData(each.getParseObject(FIELD_DATA));
            interaction.serverStatus=each2;
            listInteractions.add(interaction);
        }
        return listInteractions;
    }
	
	public static List<Interaction> getInteractions(User user, int limit) throws ParseException
	{
		//user query to tell server whose queries we need
		ParseQuery<ParseUser> queryUser = ParseUser.getQuery();
		queryUser.whereEqualTo("objectId", user.parseUser.getObjectId());
		//Interaction query parts to tell we need objects where either the from or to is our user
		List<ParseQuery<ParseObject>> listQueries = new ArrayList<>();
		ParseQuery<ParseObject> queryInteraction1 = new ParseQuery<>("Interaction");
		queryInteraction1.whereMatchesQuery(FIELD_FROM_USER, queryUser);
		listQueries.add(queryInteraction1);
		ParseQuery<ParseObject> queryInteraction2 = new ParseQuery<>("Interaction");
		queryInteraction2.whereMatchesQuery(FIELD_TO_USER, queryUser);
		listQueries.add(queryInteraction2);
		//constructing final query
		ParseQuery<ParseObject> queryInteraction = ParseQuery.or(listQueries);
		queryInteraction.setLimit(limit);
		queryInteraction.include(FIELD_FROM_USER);
		queryInteraction.include(FIELD_TO_USER);
		queryInteraction.include(FIELD_DATA); //need to add the same line to parts if need be
		//downloading and converting parse objects
		List<ParseObject> listPo = queryInteraction.find();
		List<Interaction> listInteractions = new ArrayList<Interaction>();
		for(ParseObject each : listPo)
		{
			Interaction interaction = new Interaction();
			// TODO add from to user values if added later
			User fromUser = new User();
			fromUser.parseUser = each.getParseUser(FIELD_FROM_USER);
			interaction.fromUser = fromUser;
			User toUser = new User();
			toUser.parseUser = each.getParseUser(FIELD_TO_USER);
			interaction.toUser = toUser;
			//assigning parse object for other values to be fetched
			interaction.po = each;
			//assigning data objects
			interaction.data = interaction.new MyData(each.getParseObject(FIELD_DATA));
			listInteractions.add(interaction);
		}
		return listInteractions;
	}
	
	public static List<Interaction> getAllInteractions2(User user) throws ParseException
	{
		return getInteractions(user, 1000);
	}
	
	public List<InteractionLog> getLog() throws ParseException
	{
		//init log list
		List<InteractionLog> listLog = new ArrayList<>();
		//getting log from server
		ParseQuery<ParseObject> queryLog = this.po.getRelation(FIELD_LOG).getQuery();
		List<ParseObject> lPo = queryLog.find();
		//traversing and adding to list
		for(ParseObject each : lPo)
		{
			InteractionLog log = new InteractionLog();
			log.date=each.getCreatedAt();
			log.status=Status.valueOf(each.getString(FIELD_STATUS).toUpperCase());
			listLog.add(log);
		}
		return listLog;
	}

    public String getDataObjectId()
    {
        return this.data.getObjectId();
    }

    public ParseObject getDataParseObject()
    {
        return this.data.po;
    }

	public ParseFile getData()
    {
        return this.data.getParseFile();
    }
	public class MyData
	{
		private ParseFile pf;
		private ParseObject po;
		public static final String FIELD_FILE = "file"; 
		public static final String FIELD_NAME = "name";
		public static final String FIELD_SIZE = "size";
		public static final String FIELD_TYPE = "type"; //TODO whats this?
		public static final String TABLE_DATA = "Data"; 
		
		protected MyData(ParseObject po)
		{
			this.po = po;
			this.pf = po.getParseFile(FIELD_FILE);
		}
		
		protected MyData() {
			po=new ParseObject(TABLE_DATA);
			this.pf=null;
		}
		
//		public MyData(String name, long size, String type) {
//			this();
//			po.put(FIELD_NAME, name);
//			po.put(FIELD_SIZE, size);
//			po.put(FIELD_TYPE, type);
//		}
		

        protected String getObjectId()
        {
            return this.po==null?null:this.po.getObjectId();
        }

		protected MyData(File file)
		{
			this();
			po.put(FIELD_NAME, file.getName());
			po.put(FIELD_SIZE, file.length());
			po.put(FIELD_TYPE, "text/plain"); //TODO do we need this? (if giving url to download)
		}
		
		protected MyData(File file, ParseFile pf)
		{
			this(file);
			this.pf = pf;
		}
		
		public String getName() {
			return po.getString(FIELD_NAME);
		}
		
//		public void setName(String name) {
//			po.put(FIELD_NAME, name);
//		}
		
		public long getSize() {
			return po.getLong(FIELD_SIZE);
		}
		
//		public void setSize(long size) {
//			po.put(FIELD_SIZE, size);
//		}
		
		public String getType() {
			return po.getString(FIELD_TYPE);
		}
		
		protected void setFile(ParseFile pf)
		{
			this.pf = pf;
		}
		
		protected String getFileLink()
		{
			return this.pf==null?null:this.pf.getUrl();
		}
		
		protected void save() throws ParseException {
			this.po.save();
		}
		
		protected ParseFile getParseFile()
		{
			return this.pf;
		}
		
//		public void setType(String type) {
//			po.put(FIELD_TYPE, type);
//		}
		
	}
	
	
}
