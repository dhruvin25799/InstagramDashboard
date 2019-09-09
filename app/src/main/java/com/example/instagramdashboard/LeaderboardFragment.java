package com.example.instagramdashboard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LeaderboardFragment extends Fragment {
    RecyclerView recyclerView;
    List<RankInfo> rankInfoList;
    LeaderboardAdapter adapter;
    LeaderboardAdapter nadapter;
    FirebaseFirestore db;
    private AlertDialog dialog;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.leaderboard_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.l_rv);
        db = FirebaseFirestore.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setView(R.layout.loading_layout);
        dialog = builder.create();
        final Handler h1 = new Handler();
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.show();
                h1.removeCallbacks(this);
            }
        },500);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rankInfoList = new ArrayList<>();
        rankInfoList.add(new RankInfo(
                "default",
                "28765",
                "0"
        ));
        adapter = new LeaderboardAdapter(getContext(),rankInfoList);
        recyclerView.setAdapter(adapter);
        new getleaderboard().execute();

    }

    class getleaderboard extends AsyncTask<Void,Void,Void> {
        Object data;
        String username,rank;
        int i=1;
        @Override
        protected Void doInBackground(Void... voids) {
            db.collection("Leaderboard").orderBy("rank", Query.Direction.DESCENDING).limit(10).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            Log.i("TAGGG", document.getId() + " => " + document.getData());
                            data = document.getData();
                            username = ((Map) data).get("username").toString();
                            rank = ((Map) data).get("rank").toString();
                            rankInfoList.add(new RankInfo(
                                    username,
                                    rank,
                                    String.valueOf(i)
                            ));
                            i++;

                        }
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            rankInfoList.remove(0);
            nadapter = new LeaderboardAdapter(getContext(),rankInfoList);
            updateadapter();
            super.onPostExecute(aVoid);
        }
    }

    void updateadapter(){
        Handler h1 = new Handler();
        h1.postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(nadapter);
                dialog.dismiss();
            }
        },3000);
    }
}
