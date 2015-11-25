package com.cdms.codrive;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.cdms.codrive.classes.Constants;
import com.cdms.codrive.classes.User;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SelectFriend extends Activity
{

    ListView listview;
    String filePath, responseString;
    ParseObject data;
    File selectedfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_friend);
        listview = (ListView) findViewById(R.id.listview);

        String[] values = Constants.personName.toArray(new String[Constants.personName.size()]);
        Log.d("values", values.toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectFriend.this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;
                String itemValue = (String) listview.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        .show();
                User keeper=new User();
                keeper.setParseUser(Constants.persons.get(position));
                Constants.keeperUser=keeper;
                showFileChooser();
            }

        });
    }


    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Get the path
                    String path = uri.getPath();//FileUtils.getPath(this, uri);
                    filePath = path;
                    // Get the file instance
                    File file = new File(path);
                    Toast.makeText(this, file.getName() + "," + file.getPath(),Toast.LENGTH_SHORT).show();
                    selectedfile=file;
                    new BgTask().execute();
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

        public class BgTask extends AsyncTask<Void, Void, Void>
        {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                String nm;
                InputStream inputStream = null;
                try
                {
                    inputStream = new FileInputStream(selectedfile);
                }
                catch (Exception e)
                {

                }

                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len = 0;
                try {
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }
                } catch (Exception e) {

                }
                byte[] bb = byteBuffer.toByteArray();
                ParseFile parsefile = new ParseFile(selectedfile.getName(), bb);
                data = new ParseObject("Data");
                data.put("file", parsefile);
                data.put("name", selectedfile.getName());
                int file_size = Integer.parseInt(String.valueOf(selectedfile.length() / 1024));
                data.put("size", file_size);
                data.saveInBackground(new SaveCallback()
                {

                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(SelectFriend.this, data.getObjectId(), Toast.LENGTH_LONG).show();
                        try {
                            Constants.saveLog(data.getObjectId(), Constants.keeperUser.getObjectId(), Constants.ownerUser.getObjectId());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

                return null;
            }

        }

    }