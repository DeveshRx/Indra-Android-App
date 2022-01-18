package devesh.ephrine.adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import devesh.ephrine.MainActivity;
import devesh.ephrine.R;
import devesh.common.database.contactsdb.ContactUser;


public class MyFavContactAdapter extends RecyclerView.Adapter<MyFavContactAdapter.MyViewHolder> {

    private static final Random random = new Random();
    private final List<ContactUser> mDataset;
    public String TAG = String.valueOf(R.string.app_name);
    //   public UserProfileManager mUser;
    public Context mContext;

    //Gson gson;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyFavContactAdapter(Context Context, List<ContactUser> myDataset) {

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
                .inflate(R.layout.recycleview_fav_contact_list_item, parent, false);
        // Give the view as it is
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.textView.setText(mDataset.get(position).get("cs"));
        //   holder.ItemCardView.setTag(mDataset.get(position).get("bookid"));
        //    holder.AddToLibraryChip.setTag(mDataset.get(position).get("bookid"));

        holder.contactName.setText(mDataset.get(position).DisplayName);

        if (mDataset.get(position).isAppUser == 1) {
            // User is on App
            holder.LLContact.setTag(mDataset.get(position).UID);
            Glide.with(mContext).load(mDataset.get(position).photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(holder.contactImage);


        } else {

// User is not on App

        }
        /*
        if(mDataset.get(position).UID!=null){
        }else {
        }*/


       /* try {
            Glide.with(mContext).load(mDataset.get(position).img_thumb).into(holder.contactImage);

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
            Glide.with(mContext).load("https://storage.googleapis.com/ephrinelab.appspot.com/public/baseline_wallpaper_black_48dp.png").into(holder.contactImage);

        }*/

        holder.LLContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: " + view.getTag());
                if (view.getTag() != null) {
                    String tag = view.getTag().toString();
                   /* if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).SelectContact2Call(tag);
                    }*/
                    if (mContext instanceof MainActivity) {

                        ((MainActivity) mContext).ContactUserFrag(tag);

                    }
                }

                /*Intent intent = new Intent(mContext, ProductActivity.class);
                intent.putExtra("productid", tag);
                mContext.startActivity(intent); */
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
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView contactName;
        //  public TextView contactPhone;
        public CircleImageView contactImage;
        public LinearLayout LLContact;
        //   public Button inviteButton;
        //  public LinearLayout LLAdd2Fav;
        //    public ImageView isFavImg;

        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            contactName = v.findViewById(R.id.contactName);
            LLContact = v.findViewById(R.id.LLContact);
            contactImage = v.findViewById(R.id.contactImage);
        }
    }


}

