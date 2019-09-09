package com.example.instagramdashboard;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DashboardFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AlertDialog dialog;
    private SharedPreferences pref;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dashboard_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        pref = StaticContext.getAppContext().getSharedPreferences("MyPrefs", 0);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setView(R.layout.loading_layout);
        dialog = builder.create();
        dialog.show();
        long diff = System.currentTimeMillis() - pref.getLong("TimeStamp",0);
        final long prevupdate = pref.getLong("TimeStamp",0);
        if(diff>=24*36000 || prevupdate==0){
            DocumentReference docRef = db.collection("Users").document(Objects.requireNonNull(mAuth.getUid()));
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                            new getinfo().execute(document.get("Username").toString(),Long.toString(prevupdate));
                        } else {
                            Log.d("TAG", "No such document");
                        }

                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        } else {
            new useinfo().execute();
        }

        super.onViewCreated(view, savedInstanceState);
    }



    private class getinfo extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            int flag = 0;
            String username = strings[0];
            if (strings[1].equals("0")) {
                flag = 1;
            }
            String s = "";
            String s_url = "https://www.instagram.com/" + username + "/?__a=1";
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            JSONObject obj = null;
            try {
                URL url = new URL(s_url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    //Log.d("Response: ", "> " + line);

                }
                obj = new JSONObject(buffer.toString());
                obj = obj.getJSONObject("graphql");
                obj = obj.getJSONObject("user");
                JSONObject followers = obj.getJSONObject("edge_followed_by");
                JSONObject following = obj.getJSONObject("edge_follow");
                JSONObject posts = obj.getJSONObject("edge_owner_to_timeline_media");
                int currfollowers, currfollowing;
                float ranks = (float) ((0.2*Integer.parseInt(posts.getString("count"))) +(0.6*Integer.parseInt(followers.getString("count"))) + (0.2*Integer.parseInt(following.getString("count"))));
                int rank = (int)ranks;
                currfollowers = pref.getInt("Followers", 0);
                currfollowing = pref.getInt("Following", 0);
                currfollowers = Integer.parseInt(followers.getString("count")) - currfollowers;
                currfollowing = Integer.parseInt(following.getString("count")) - currfollowing;
                SharedPreferences.Editor edit = pref.edit();
                edit.putInt("Posts", Integer.parseInt(posts.getString("count")));
                edit.putInt("Followers", Integer.parseInt(followers.getString("count")));
                edit.putInt("Following", Integer.parseInt(following.getString("count")));
                edit.putString("DP", obj.getString("profile_pic_url"));
                edit.putFloat("Rank",rank);
                Map<String, Object> up_rank = new HashMap<>();
                up_rank.put("rank",rank);
                up_rank.put("username",username);
                db.collection("Leaderboard").document(username)
                        .set(up_rank)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "DocumentSnapshot successfully written!");

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error writing document", e);
                            }
                        });
                if (flag == 1) {
                    edit.putInt("FollowersDiff", 0);
                    edit.putInt("FollowingDiff", 0);
                    edit.putLong("TimeStamp", System.currentTimeMillis());
                    edit.apply();
                    s = posts.getString("count") + " " + followers.getString("count") + " " + following.getString("count") + " " + obj.getString("profile_pic_url") + " " + "0" + " " + "0"+" "+rank;

                } else {
                    edit.putInt("FollowersDiff", currfollowers);
                    edit.putInt("FollowingDiff", currfollowing);
                    edit.putLong("TimeStamp", System.currentTimeMillis());
                    edit.apply();
                    s = posts.getString("count") + " " + followers.getString("count") + " " + following.getString("count") + " " + obj.getString("profile_pic_url") + " " + currfollowers + " " + currfollowing+" "+rank;

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return s;
        }





        @Override
        protected void onPostExecute(String s) {

            String[] info;
            info = s.split(" ");
            TextView posts, followers, following, followerdiff, followingdiff,d_rank,t_posts,t_followers,t_following;
            CircularImageView dp;
            posts = Objects.requireNonNull(getActivity()).findViewById(R.id.d_vposts);
            followers = getActivity().findViewById(R.id.d_vfollowers);
            following = getActivity().findViewById(R.id.d_vfollowing);
            followerdiff = getActivity().findViewById(R.id.d_followerdiff);
            followingdiff = getActivity().findViewById(R.id.d_followingdiff);
            d_rank = getActivity().findViewById(R.id.d_rank);
            dp = getActivity().findViewById(R.id.d_dp);
            t_posts = getActivity().findViewById(R.id.d_posts);
            t_followers = getActivity().findViewById(R.id.d_followers);
            t_following = getActivity().findViewById(R.id.d_following);
            posts.setText(info[0]);
            followers.setText(info[1]);
            following.setText(info[2]);
            followerdiff.setText(info[4]);
            followingdiff.setText(info[5]);
            d_rank.setText(info[6]);
            Glide.with(getContext()).load(info[3]).override(100, 100).into(dp);
            dialog.dismiss();
            AnimationSet img = new AnimationSet(true);
            RotateAnimation rotate1 = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f , Animation.RELATIVE_TO_SELF,0.5f );
            rotate1.setStartOffset(50);
            rotate1.setDuration(2000);
            img.addAnimation(rotate1);
            TranslateAnimation trans1 =  new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.0f);
            trans1.setDuration(2000);
            img.addAnimation(trans1);
            ScaleAnimation scale1 = new ScaleAnimation(2.0f,1.0f,2.0f,1.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            scale1.setDuration(2000);
            img.addAnimation(scale1);
            dp.setAnimation(img);
            AnimationSet tview_anim = new AnimationSet(true);
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f,1.0f);
            fadeIn.setStartOffset(2000);
            fadeIn.setDuration(500);
            tview_anim.addAnimation(fadeIn);
            posts.setAnimation(tview_anim);
            followers.setAnimation(tview_anim);
            following.setAnimation(tview_anim);
            followerdiff.setAnimation(tview_anim);
            followingdiff.setAnimation(tview_anim);
            d_rank.setAnimation(tview_anim);
            t_posts.setAnimation(tview_anim);
            t_followers.setAnimation(tview_anim);
            t_following.setAnimation(tview_anim);
            super.onPostExecute(null);
        }
    }
    private class useinfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            TextView posts,followers,following,followerdiff,followingdiff,d_rank,t_posts,t_followers,t_following;
            CircularImageView dp;
            posts = getActivity().findViewById(R.id.d_vposts);
            followers = getActivity().findViewById(R.id.d_vfollowers);
            following = getActivity().findViewById(R.id.d_vfollowing);
            followerdiff = getActivity().findViewById(R.id.d_followerdiff);
            followingdiff = getActivity().findViewById(R.id.d_followingdiff);
            d_rank = getActivity().findViewById(R.id.d_rank);
            dp = getActivity().findViewById(R.id.d_dp);
            t_posts = getActivity().findViewById(R.id.d_posts);
            t_followers = getActivity().findViewById(R.id.d_followers);
            t_following = getActivity().findViewById(R.id.d_following);
            posts.setText(String.valueOf(pref.getInt("Posts", 0)));
            followers.setText(String.valueOf(pref.getInt("Followers",0)));
            following.setText(String.valueOf(pref.getInt("Following", 0)));
            followerdiff.setText(String.valueOf(pref.getInt("FollowersDiff", 0)));
            followingdiff.setText(String.valueOf(pref.getInt("FollowingDiff", 0)));
            d_rank.setText(String.valueOf(pref.getFloat("Rank", 0)));
            Glide.with(getContext()).load(pref.getString("DP","")).override(100,100).into(dp);
            dialog.dismiss();
            AnimationSet img = new AnimationSet(true);
            RotateAnimation rotate1 = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f , Animation.RELATIVE_TO_SELF,0.5f );
            rotate1.setStartOffset(50);
            rotate1.setDuration(2000);
            img.addAnimation(rotate1);
            TranslateAnimation trans1 =  new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.0f);
            trans1.setDuration(2000);
            img.addAnimation(trans1);
            ScaleAnimation scale1 = new ScaleAnimation(2.0f,1.0f,2.0f,1.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            scale1.setDuration(2000);
            img.addAnimation(scale1);
            dp.setAnimation(img);
            AnimationSet tview_anim = new AnimationSet(true);
            AlphaAnimation fadeIn = new AlphaAnimation(0.0f,1.0f);
            fadeIn.setStartOffset(2000);
            fadeIn.setDuration(500);
            tview_anim.addAnimation(fadeIn);
            posts.setAnimation(tview_anim);
            followers.setAnimation(tview_anim);
            following.setAnimation(tview_anim);
            followerdiff.setAnimation(tview_anim);
            followingdiff.setAnimation(tview_anim);
            d_rank.setAnimation(tview_anim);
            t_posts.setAnimation(tview_anim);
            t_followers.setAnimation(tview_anim);
            t_following.setAnimation(tview_anim);
            super.onPostExecute(aVoid);
        }
    }


}
