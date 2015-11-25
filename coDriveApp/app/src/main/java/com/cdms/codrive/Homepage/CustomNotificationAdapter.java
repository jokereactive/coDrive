package com.cdms.codrive.Homepage;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cdms.codrive.R;
import com.cdms.codrive.classes.Constants;
import com.cdms.codrive.classes.Interaction;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomNotificationAdapter extends RecyclerView.Adapter<CustomNotificationAdapter.ViewHolder>
{
    private List<Interaction> notifications;
    Context context;
    public static final String FOLDER = Environment.getExternalStorageDirectory().toString();

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        // each data item is just a string in this case
        //public TextView mTextView;
        public CardView cardView;
        public ViewHolder(CardView v)
        {
            super(v);
            cardView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomNotificationAdapter(List<Interaction> notification, Context context)
    {
        this.notifications = notification;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType)
    {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_card_view, parent, false);

            ViewHolder vh = new ViewHolder((CardView) v);
            return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
            boolean entered=false;
            Log.d("postition", "post" + position);
            StringBuilder content=new StringBuilder();
            if(notifications.get(position).getType().equals(Interaction.Type.STORE) && notifications.get(position).getStatus().equals(Interaction.Status.REQUEST_SENT)
                    && !notifications.get(position).getFromUser().equals(Constants.user))
            {
                content.append(notifications.get(position).getFromUser());
                content.append(" wants to store a file on your device");
                holder.cardView.setOnClickListener(new MyOnClickListener(position));
                entered=true;
            }

            if(notifications.get(position).getType().equals(Interaction.Type.STORE) && notifications.get(position).getStatus().equals(Interaction.Status.REQUEST_ACCEPTED)
                && !notifications.get(position).getFromUser().equals(Constants.user))
            {
                content.append("File from ");
                content.append(notifications.get(position).getFromUser());
                content.append(" is ready to be downloaded");
                holder.cardView.setOnClickListener(new DownloadFileListener(position));
                entered=true;
            }

        if(notifications.get(position).getType().equals(Interaction.Type.RETRIEVE) && notifications.get(position).getStatus().equals(Interaction.Status.REQUEST_SENT)
                && !notifications.get(position).getFromUser().equals(Constants.user))
        {
            content.append(notifications.get(position).getFromUser().getEmail());
            content.append(" wants his file back");
            holder.cardView.setOnClickListener(new AcceptRetrieveListener(position));
            entered=true;
        }

        if(notifications.get(position).getType().equals(Interaction.Type.RETRIEVE) && notifications.get(position).getStatus().equals(Interaction.Status.REQUEST_ACCEPTED)
                && !notifications.get(position).getFromUser().equals(Constants.user))
        {
            content.append("Click to upload ");
            content.append(notifications.get(position).getDataParseObject().getString("name"));
            content.append(" for ");
            content.append(notifications.get(position).getFromUser().getEmail());
            holder.cardView.setOnClickListener(new FileRetrieveListener(position));
            entered=true;
        }

        if(notifications.get(position).getType().equals(Interaction.Type.RETRIEVE) && notifications.get(position).getStatus().equals(Interaction.Status.ON_SERVER)
        && !notifications.get(position).getToUser().equals(Constants.user))
        {
            content.append("Click to download your ");
            content.append(notifications.get(position).getDataParseObject().getString("name"));
            content.append(" file ");
            holder.cardView.setOnClickListener(new DownloadRetrieveListener(position));
            entered=true;
        }

         ((TextView) holder.cardView.findViewById(R.id.content)).setText(content.toString());
//        if(entered)
//        {
//
//        }
//        else
//        {
//            holder.cardView.setVisibility(View.INVISIBLE);
//        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return notifications.size();
    }

    class DownloadFileListener implements View.OnClickListener
    {

        int index;

        public DownloadFileListener(int position) {
            this.index = position;
        }

        @Override
        public void onClick(View v)
        {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialogbox_download);
            dialog.setTitle("Request");

            Button okbutton=(Button)dialog.findViewById(R.id.download);
            okbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    new DownloadFileTask(index).execute();
                    Log.d("parse cloud",v.getId()+"2");
                    dialog.dismiss();
                }
            });

            Button cancelbutton=(Button)dialog.findViewById(R.id.later);
            cancelbutton.setVisibility(View.INVISIBLE);
            dialog.show();
        }
    }

    class MyOnClickListener implements View.OnClickListener
    {

        int index;

        public MyOnClickListener(int position) {
            this.index = position;
        }

        @Override
        public void onClick(View v)
        {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialogbox);
            dialog.setTitle("Request");

            Button okbutton=(Button)dialog.findViewById(R.id.accept);
            okbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Toast.makeText(context,"accept",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    try {
                        Constants.RespondToStoreRequest(true,notifications.get(index).getServerStatus().getObjectId());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            Button cancelbutton=(Button)dialog.findViewById(R.id.reject);
            cancelbutton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "reject", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    try {
                        Constants.RespondToStoreRequest(false,notifications.get(index).getServerStatus().getObjectId());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    notifications.remove(index);
                }
            });

            dialog.show();
        }
    }

    public class DownloadFileTask extends AsyncTask<Void, Void, Void>
    {
        int index;

        DownloadFileTask(int position)
        {
            this.index=position;
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
//            ParseFile parseFile = data.getParseFile();
//            byte[] bytes = parseFile.getData();
//            FileOutputStream fos = new FileOutputStream(f);
//            fos.write(bytes);
//            fos.close();

            Map<String, Object> param=new HashMap<>();
            param.put("serverStatusId", notifications.get(index).getServerStatus().getObjectId());
            try {
                ParseCloud.callFunction("CompleteStoreRequest", param);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("parse cloud",e.getLocalizedMessage()+"4");
            }
            ParseFile pf= notifications.get(index).getData();
            ParseObject po=notifications.get(index).getDataParseObject();
            try {
                Constants.storeFile(pf,po.getString("name"));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
        }
    }

    class AcceptRetrieveListener implements View.OnClickListener
    {

        int index;

        public AcceptRetrieveListener(int position) {
            this.index = position;
        }

        @Override
        public void onClick(View v)
        {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialogbox_retrive_input);
            dialog.setTitle("Request");

            Button okbutton=(Button)dialog.findViewById(R.id.yesupload);
            okbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Toast.makeText(context,"accept",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    try {
                        Constants.RespondToRetrieveRequest(true, notifications.get(index).getServerStatus().getObjectId());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            Button cancelbutton=(Button)dialog.findViewById(R.id.notnow);
            cancelbutton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "reject", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    try {
                        Constants.RespondToRetrieveRequest(false, notifications.get(index).getServerStatus().getObjectId());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

            dialog.show();
        }
    }

    class FileRetrieveListener implements View.OnClickListener
    {

        int index;

        public FileRetrieveListener(int position) {
            this.index = position;
        }

        @Override
        public void onClick(View v)
        {

            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialogbox_retrieve_upload);
            dialog.setTitle("Request");

            Button okbutton=(Button)dialog.findViewById(R.id.yesuploadnow);
            okbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                    Map<String, Object> params=new HashMap<>();
                    params.put("isAccepted",true);
                    params.put("serverStatusId",notifications.get(index).getServerStatus().getObjectId());
                    try {
                        ParseCloud.callFunction("PrecompleteRetrieveRequest", params);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context,"Uploaded",Toast.LENGTH_SHORT).show();
                }
            });

            Button cancelbutton=(Button)dialog.findViewById(R.id.notuploadnow);
            cancelbutton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    }
            });

            dialog.show();

        }
    }

    class DownloadRetrieveListener implements View.OnClickListener
    {

        int index;

        public DownloadRetrieveListener(int position) {
            this.index = position;
        }

        @Override
        public void onClick(View v)
        {
            final Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_dialogbox_retrieval_download);
            dialog.setTitle("Request");

            Button okbutton=(Button)dialog.findViewById(R.id.yesdownload);
            okbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    new DownloadRetrievalFileTask(index).execute();
                    dialog.dismiss();
                }
            });

            Button cancelbutton=(Button)dialog.findViewById(R.id.notdownload);
            cancelbutton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

    public class DownloadRetrievalFileTask extends AsyncTask<Void, Void, Void>
    {
        int index;

        DownloadRetrievalFileTask(int position)
        {
            this.index=position;
        }
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            Map<String, Object> param=new HashMap<>();
            param.put("serverStatusId", notifications.get(index).getServerStatus().getObjectId());
            try {
                ParseCloud.callFunction("CompleteRetrieveRequest", param);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("parse cloud",e.getLocalizedMessage()+"4");
            }
            ParseFile pf= notifications.get(index).getData();
            ParseObject po=notifications.get(index).getDataParseObject();
            try {
                Constants.storeFile(pf,po.getString("name"));
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            Toast.makeText(context,"Downloaded",Toast.LENGTH_SHORT).show();
        }
    }


}
