package com.example.instagramdashboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>{
    private Context mCtx;
    private List<RankInfo> rankInfoList;
    //private String[] colors = new String[] {"#181818","#202020","#282828","#303030","#383838","#404040","#484848","#505050","#585858","#606060"};

    public LeaderboardAdapter(Context mCtx, List<RankInfo> rankInfoList) {
        this.mCtx = mCtx;
        this.rankInfoList = rankInfoList;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =LayoutInflater.from(mCtx);
        View view =inflater.inflate(R.layout.leaderbord_list_item,  parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        RankInfo rankInfo = rankInfoList.get(position);
        //holder.cardView.setCardBackgroundColor(Color.parseColor(colors[position]));
        holder.index.setText(rankInfo.getIndex());
        holder.username.setText(rankInfo.getUsername());
        holder.rank.setText(rankInfo.getRank());
    }

    @Override
    public int getItemCount() {
        return rankInfoList.size();
    }

    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView index,username,rank;
        CardView cardView;
        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.li_index);
            username = itemView.findViewById(R.id.li_username);
            rank = itemView.findViewById(R.id.li_rank);
            cardView = itemView.findViewById(R.id.li_cv);
        }
    }
}
