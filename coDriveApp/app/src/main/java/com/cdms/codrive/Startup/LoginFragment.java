package com.cdms.codrive.Startup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.cdms.codrive.R;
import com.cdms.codrive.classes.Constants;
import com.cdms.codrive.classes.User;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class LoginFragment extends Fragment
{

    TextView header_progress_bar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d("login", "running login fargment");

        header_progress_bar=(TextView)getActivity().findViewById(R.id.header_progess_bar);
        header_progress_bar.setText("Logging in..");
        new BgTask().execute();
    }

	public class BgTask extends AsyncTask<Void, Void, Void>
	{
	    @Override
	    protected void onPreExecute()
	    {
	        super.onPreExecute();
	    }

	    @Override
	    protected Void doInBackground(Void... params)
	    {
	        ParseUser user=checkAndGetUser();
	        User newuser=new User();
	        newuser.setParseUser(user);
	        Constants.user=newuser;
	        Constants.ownerUser=newuser;
	        return null;
	    }
	    @Override
	    protected void onPostExecute(Void result)
	    {
            Fragment mFragment = new GetDataFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.view_fragment_container, mFragment).commit();
	    }
	}
	private ParseUser checkAndGetUser()
	{
		//fetch googleId
		String googleId = "";
		try 
		{
			Account[] accounts = AccountManager.get(getActivity()).getAccountsByType("com.google");
			googleId+=accounts[0].name;
		} 
		catch (Exception e) 
		{
			
		}
		
		//ParseUser.logOut();
		ParseUser user= ParseUser.getCurrentUser();
//		Toast.makeText(this, user.toString(), 0).show();
		if(user!=null)
		{
			Log.d("asdf","user not null");
            Log.d("asdf",user.getEmail());
			if(user.getEmail()==null)
			{
				if(user.getEmail()==null && googleId!=null)
					user.setEmail(googleId);
				try
                {
					user.save();
                    parseInstallation(user);
				} 
				catch (ParseException e) 
				{	
					
				}
			}
			//Log.d("asdf","1");
			return user;
		}
		
		//check existence
		try 
		{
			Log.d("asdf","starting sign in");
			user = ParseUser.logIn(googleId, "password");
			Log.d("asdf","sign in done");
			if(user.getEmail()==null)
			{
				if(user.getEmail()==null && googleId!=null)
					user.setEmail(googleId);
				try {
					user.save();
                    parseInstallation(user);
				} catch (ParseException e) {	}
			}
			//Log.d("asdf","2");
			return user;
		} 
		catch (ParseException e1) {
			String debugStr="";
			try{
                Log.d("asdf","sign up");
				user = new ParseUser();
				user.setUsername(googleId);
				user.setEmail(googleId);
				user.setPassword("password");
				user.signUp(); debugStr+="2";
                parseInstallation(user);
				return user;
			}
			catch(Exception excep)
			{
			}
		}
		
		return null;
	}

    public void parseInstallation(ParseUser parseUser) throws ParseException {
        ParseInstallation pi = ParseInstallation.getCurrentInstallation();
//        pi.put("user",parseUser);
        pi.put("user", ParseObject.createWithoutData("_User",parseUser.getObjectId()));
        pi.save();

    }
	
}


