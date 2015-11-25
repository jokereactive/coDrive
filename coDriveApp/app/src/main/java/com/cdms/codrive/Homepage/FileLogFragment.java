package com.cdms.codrive.Homepage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdms.codrive.R;
import com.cdms.codrive.classes.Constants;

/**
 * A placeholder fragment containing a simple view.
 */
public class FileLogFragment extends Fragment
{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
   	public void onActivityCreated(Bundle savedInstanceState) 
    {
   		super.onActivityCreated(savedInstanceState);
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.file_log_recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CustomFileLogAdapter(Constants.filelog,getActivity());
        mRecyclerView.setAdapter(mAdapter);

//        Button test1=(Button)getActivity().findViewById(R.id.test1);
//        test1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Map<String, Object> params=new HashMap<>();
//                params.put("serverStatusId","8ghVRErsp4");
//                try {
//                    ParseCloud.callFunction("CompleteStoreRequest", params);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        Button test2=(Button)getActivity().findViewById(R.id.test2);
//        test2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Map<String, Object> params=new HashMap<>();
//                params.put("serverStatusId","8ghVRErsp4");
//                try {
//                    ParseCloud.callFunction("AddRetrieveRequest", params);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        Button test3=(Button)getActivity().findViewById(R.id.test3);
//        test3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Map<String, Object> params=new HashMap<>();
//                params.put("isAccepted",true);
//                params.put("serverStatusId","8ghVRErsp4");
//                try {
//                    ParseCloud.callFunction("RespondToRetrieveRequest", params);
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
   	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.log_layout, container, false);
        return rootView;
    }
	 
}