package com.cdms.codrive.classes;

import com.parse.ParseException;
import com.parse.ParseUser;

public class User {
	
	protected ParseUser parseUser;

    @Override
    public boolean equals(Object o)
    {
        if(this.getEmail().equals(((User) o).getEmail()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.getEmail();
    }

    public void setParseUser(ParseUser parseUser) {
		this.parseUser = parseUser;
	}

	public String getEmail()
	{
		return this.parseUser.getEmail();
	}

    public String getObjectId()
    {
        return this.parseUser.getObjectId();
    }

	//null check for Activity
	public Exception login(String username, String password)
	{
		ParseUser user = ParseUser.getCurrentUser();
		if(user!=null)
		{
			this.parseUser = user;
			return null;
		}
		try
		{
			user = ParseUser.logIn(username, password);
			this.parseUser = user;
			return null;
		} catch (ParseException e) {
			return e;
		}
	}
	
	//null check for Activity
	public Exception signUp(String username, String password)
	{
		ParseUser user = new ParseUser();
		user.setUsername(username);
		user.setPassword(password);
		try {
			user.signUp();
			this.parseUser=user;
			return null;
		} catch (ParseException e) {
			return e;
		}
	}
	
}
