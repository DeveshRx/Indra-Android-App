package devesh.friendslist.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import devesh.common.database.contactsdb.ContactUser;
import devesh.friendslist.FriendsListActivity;
import devesh.friendslist.R;


public class MyContactAdapter extends RecyclerView.Adapter<MyContactAdapter.MyViewHolder> {

    private static final Random random = new Random();
    private final List<ContactUser> mDataset;
    public String TAG = String.valueOf(R.string.comm_app_name);
    //   public UserProfileManager mUser;
    public Context mContext;

    //Gson gson;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyContactAdapter(Context Context, List<ContactUser> myDataset) {

        // gson = new Gson();
        mDataset = myDataset;
        mContext = Context;
        Glide.get(mContext).clearMemory();

        // mUser.Download();
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        // TextView v = (TextView) LayoutInflater.from(parent.getContext())
        //       .inflate(R.layout.recycleview_books_list, parent, false);

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_contactlist_item, parent, false);
        // Give the view as it is
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: mDataset.get(position).phone: "+mDataset.get(position).phone);
        holder.contactName.setText(mDataset.get(position).DisplayName);
        holder.contactPhone.setText(mDataset.get(position).phone);

        holder.LLAddFriend.setTag(mDataset.get(position).UID);
       // holder.LLContact.setTag(mDataset.get(position).UID);

            Glide.with(mContext).load(mDataset.get(position).photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(holder.contactImage);




        holder.LLAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: " + view.getTag());
                if (view.getTag() != null) {
                    String tag = view.getTag().toString();

                    if (mContext instanceof FriendsListActivity) {
                        ((FriendsListActivity) mContext).SendFriendRequest(tag);
                    }
                }


            }
        });





    }

    public String getItemData(int pos) {
        if (mDataset.get(pos).UID != null) {
            return mDataset.get(pos).UID;
        } else {
            return null;
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if(mDataset!=null){
            return mDataset.size();
        }else{
            return 0;
        }

    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView contactName;
        public TextView contactPhone;
        public CircleImageView contactImage;
        public LinearLayout LLContact;

        public LinearLayout LLAddFriend;
      //  public ImageView isFavImg;

        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            contactName = v.findViewById(R.id.contactName);
            contactPhone = v.findViewById(R.id.contactPhone);
            LLContact = v.findViewById(R.id.LLContact);
            contactImage = v.findViewById(R.id.contactImage);

           // isFavImg = v.findViewById(R.id.isFavImg);
            LLAddFriend= v.findViewById(R.id.LLAddFriend);
        }
    }


}

