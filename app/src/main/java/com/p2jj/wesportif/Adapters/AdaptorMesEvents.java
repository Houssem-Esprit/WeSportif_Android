package com.p2jj.wesportif.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.like.LikeButton;
import com.p2jj.wesportif.Activities.DetailEvent;
import com.p2jj.wesportif.Data_Managers.RetrofitClient;
import com.p2jj.wesportif.Fragments.CommentFragment;
import com.p2jj.wesportif.Model.Event;
import com.p2jj.wesportif.Model.User;
import com.p2jj.wesportif.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptorMesEvents extends RecyclerView.Adapter<AdaptorMesEvents.myViewHolder> implements Filterable {

    Context mContext;
    List<Event> mData;
    Fragment CommentsFragment;
    List<Event> listBackup;
    Event e;
    User currentUser;

    public AdaptorMesEvents(Context mContext, List<Event> mData) {
        this.mContext = mContext;
        this.mData = mData;
        listBackup=new ArrayList<>(mData);

    }

    @Override
    public myViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater inflator= LayoutInflater.from(mContext);
        View v = inflator.inflate(R.layout.all_event,parent,false);
        CommentsFragment = new CommentFragment();
        return new myViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final myViewHolder holder, final int position) {


//Bitmap.createScaledBitmap(mData.get(position).getImgBmp(), 200, 250, false)
        String url="http://wessporitf.eu-4.evennode.com/uploads/events/"+mData.get(position).getImg_event();
        Glide.with(mContext).load(url).into(holder.event_img);

//        holder.event_img.setImageBitmap(mData.get(position).getImgBmp());

        holder.titleEvent.setText(mData.get(position).getTitre());
        holder.date.setText(mData.get(position).getDate_debut());
        holder.CategorieSport.setText(mData.get(position).getCategorieSport());
        CommentsFragment = new CommentFragment();
        final FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();

        holder.CommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsFragment = new CommentFragment();

                    if(holder.comment_Frame.getChildCount()<1){
                    int newContainerId = getUniqueId();
                    holder.comment_Frame.setId(newContainerId);
                    fragmentManager.beginTransaction().replace(newContainerId, CommentsFragment).commit();
                    }


            }

            public int getUniqueId(){
                return (int) SystemClock.currentThreadTimeMillis();
            }
        });
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event e=mData.get(position);
                Intent i=new Intent(mContext, DetailEvent.class);
                i.putExtra("eventId",e.getId());
                mContext.startActivity(i);
            }
        });
        e=mData.get(position);

        userDetails(holder);

        holder.likeBtn.setVisibility(View.GONE);

    }

    public final View.OnClickListener CommentButtonClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CommentsFragment = new CommentFragment();
            setFragment(CommentsFragment);

        }
    };

    @Override
    public int getItemCount() {
        return mData.size();
     }

    public class myViewHolder extends  RecyclerView.ViewHolder{
        ImageView event_img;
        TextView titleEvent, date , CategorieSport;
        ImageButton CommentButton;
        LinearLayout comment_Frame;
        View view;
        ImageView user_img;
        TextView user_name;
        LikeButton likeBtn;


        public myViewHolder( View itemView) {
            super(itemView);
            view=itemView;
            event_img = itemView.findViewById(R.id.allEvent_img);
            titleEvent= itemView.findViewById(R.id.allEvent_name);
            date =itemView.findViewById(R.id.detaileventDATE_input);
            CategorieSport=itemView.findViewById(R.id.detaileventCategorie_input);
            CommentButton = itemView.findViewById(R.id.AllEvent_CommentButton);
            comment_Frame =itemView.findViewById(R.id.comment_fram);
            CommentsFragment = new CommentFragment();

            user_img=itemView.findViewById(R.id.all_event_userimg_img);
            user_name=itemView.findViewById(R.id.all_event_username_name);

            likeBtn=itemView.findViewById(R.id.allEvent_fav);

        }
        public View getView()
        {
            return view;
        }
    }


    public void setFragment(Fragment fragment){

        FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.comment_fram,fragment);
        fragmentTransaction.commit();

    }
    @Override
    public Filter getFilter() {
        return listFilter;
    }

    private Filter listFilter=new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Event> filteredList=new ArrayList<>();
            if(constraint==null || constraint.length()==0)
            {
                filteredList.addAll(listBackup);
            }
            else
            {

                String filterPatern=constraint.toString().toLowerCase().trim();

                for(Event item:listBackup)
                {
                    if(item.getTitre().toLowerCase().contains(filterPatern) || item.getCategorieSport().toLowerCase().contains(filterPatern))
                    {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results=new FilterResults();
            results.values=filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mData.clear();
            mData.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
    public void userDetails(final myViewHolder holder)
    {
        Call call = RetrofitClient
                .getInstance()
                .getApi_service_node()
                .GetUserDetails(e.getUserCreator());
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {

                try {

                    JSONObject jsonResp=new JSONObject(new Gson().toJson(response.body()));
                    String msg=jsonResp.getString("userInformations");

                    if(msg.equals("User retrieved")){
                        JSONObject userJson=jsonResp.getJSONObject("user");

                        currentUser=new User();
                        try {
                            String cin = userJson.getString("cin");
                            String nom=userJson.getString("nom");
                            String prenom=userJson.getString("prenom");

                            String img=userJson.getString("img");


                            currentUser.setCin(cin);
                            currentUser.setNom(nom);
                            currentUser.setPrenom(prenom);

                            currentUser.setImg_user(img);

                            String username=currentUser.getPrenom()+" "+currentUser.getNom();
                            holder.user_name.setText(username);

                            String urlUser="http://wessporitf.eu-4.evennode.com/uploads/users/"+currentUser.getImg_user();
                            Glide.with(mContext).load(urlUser).into(holder.user_img);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });

    }

}
