package com.cdms.codrive.Homepage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cdms.codrive.R;
import com.cdms.codrive.classes.Constants;
import com.software.shell.fab.ActionButton;

public class FriendsFragment extends Fragment
{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        ActionButton actionButton = (ActionButton)getActivity().findViewById(R.id.action_button);
        actionButton.getShowAnimation();
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(),"Click",Toast.LENGTH_SHORT).show();
                Intent i=new Intent("com.cdms.codrive.SelectFriend");
                startActivity(i);
            }
        });

        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.my_recycler_view);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CustomFriendAdapter(Constants.persons,getActivity());
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.friends_layout, container, false);
        return rootView;
    }

}