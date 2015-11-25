package com.cdms.codrive.Startup;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.TextView;

import com.cdms.codrive.R;
import com.cdms.codrive.classes.Constants;
import com.cdms.codrive.classes.Interaction;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Danish Goel on 01-May-15.
 */
public class GetDataFragment extends Fragment
{
    TextView header_progress_bar;
    ParseObject remove_self_object;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.d("login", "running login fargment");

        header_progress_bar=(TextView)getActivity().findViewById(R.id.header_progess_bar);
        header_progress_bar.setText("Getting Data..");

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
            ParseQuery<ParseUser> queryUsers=new ParseQuery<>("_User");
            try {
                Constants.filelog=Interaction.getMyOwnerInteractions(Constants.user);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Constants.getInteractions();
            try
            {
                Constants.persons=queryUsers.find();
                for(ParseUser po:Constants.persons)
                {

                    if(!po.getEmail().equals(Constants.user.getEmail()))
                    {
                        Constants.personName.add(po.getString("username"));
                    }
                    else
                    {
                        remove_self_object=po;
                    }
                }
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            Log.d("values1",Constants.personName.toString());
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            Constants.persons.remove(remove_self_object);
            Intent i=new Intent("com.cdms.codrive.MainActivity");
            startActivity(i);
        }
    }
}
