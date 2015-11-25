package com.cdms.codrive.Homepage;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
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

public class CustomFileLogAdapter extends RecyclerView.Adapter<CustomFileLogAdapter.ViewHolder>
{
    private List<Interaction> filelogs;
    Context context;

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
    public CustomFileLogAdapter(List<Interaction> notification, Context context)
    {
        this.filelogs = notification;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType)
    {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.file_log_card_view, parent, false);

            ViewHolder vh = new ViewHolder((CardView) v);
            return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
            Log.d("postition", "post" + position);
            StringBuilder content=new StringBuilder();
//            if(filelogs.get(position).getType().equals(Interaction.Type.STORE) && filelogs.get(position).getStatus().equals(Interaction.Status.REQUEST_SENT)
//                    && !filelogs.get(position).getFromUser().equals(Constants.user))
//            {
//                content.append(filelogs.get(position).getFromUser());
//                content.append(" wants to store a file on your device");
//                holder.cardView.setOnClickListener(new MyOnClickListener(position));
//            }
//
//            if(filelogs.get(position).getType().equals(Interaction.Type.STORE) && filelogs.get(position).getStatus().equals(Interaction.Status.REQUEST_ACCEPTED)
//                && !filelogs.get(position).getFromUser().equals(Constants.user))
//            {
//                content.append("File from ");
//                content.append(filelogs.get(position).getFromUser());
//                content.append(" is ready to be downloaded");
//                holder.cardView.setOnClickListener(new DownloadFileListener(position));
//            }
            content.append("Your file ");
            content.append(filelogs.get(position).getDataParseObject().getString("name"));
            content.append(" is with ");
            content.append(filelogs.get(position).getToUser().getEmail());
            ((TextView) holder.cardView.findViewById(R.id.log)).setText(content.toString());
            holder.cardView.setOnClickListener(new FileRetrieveListener(position));



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return filelogs.size();
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
            dialog.setContentView(R.layout.custom_dialogbox_retriev_request);
            dialog.setTitle("Request");

            Button okbutton=(Button)dialog.findViewById(R.id.yes);
            okbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    Map<String, Object> param=new HashMap<>();
                    param.put("serverStatusId", filelogs.get(index).getServerStatus().getObjectId());
                    try {
                        ParseCloud.callFunction("AddRetrieveRequest", param);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.d("parse cloud",e.getLocalizedMessage()+"4");
                    }
                    dialog.dismiss();
                }
            });

            Button cancelbutton=(Button)dialog.findViewById(R.id.no);
            cancelbutton.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
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
                        Constants.RespondToStoreRequest(true, filelogs.get(index).getServerStatus().getObjectId());
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
                    filelogs.remove(index);
                    dialog.dismiss();
                    try {
                        Constants.RespondToStoreRequest(false, filelogs.get(index).getServerStatus().getObjectId());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
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
            param.put("serverStatusId", filelogs.get(index).getServerStatus().getObjectId());
            try {
                ParseCloud.callFunction("CompleteStoreRequest", param);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("parse cloud",e.getLocalizedMessage()+"4");
            }
            ParseFile pf= filelogs.get(index).getData();
            ParseObject po= filelogs.get(index).getDataParseObject();
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

}
