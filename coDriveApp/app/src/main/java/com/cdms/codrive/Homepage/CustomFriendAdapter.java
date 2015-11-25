package com.cdms.codrive.Homepage;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cdms.codrive.R;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomFriendAdapter extends RecyclerView.Adapter<CustomFriendAdapter.ViewHolder>
{
    private List<ParseUser> friends;
    Context context;

    List<String> colors=new ArrayList<String>(
            Arrays.asList("#009688","#9C27B0","#F44336","#8BC34A","#CDDC39","#FF9800","#9E9E9E"));


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
    public CustomFriendAdapter(List<ParseUser> friends, Context context)
    {
        this.friends = friends;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType)
    {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.friend_card_view, parent, false);

            ViewHolder vh = new ViewHolder((CardView) v);
            return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
            Log.d("postition", "post" + position);
            ((TextView) holder.cardView.findViewById(R.id.name)).setText(friends.get(position).getUsername());
            ((LinearLayout) holder.cardView.findViewById(R.id.header)).setBackgroundColor(Color.parseColor(getCardColor(position)));
            holder.cardView.setOnClickListener(new MyOnClickListener(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return friends.size();
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

        }
    }

    public String getCardColor(int position)
    {
        int i=0;
        i=position%colors.size();
        Log.d("color",i+" "+colors.get(i));
        return colors.get(i);
    }

}
