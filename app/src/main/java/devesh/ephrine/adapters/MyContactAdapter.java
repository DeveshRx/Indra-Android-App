package devesh.ephrine.adapters;


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
import devesh.ephrine.MainActivity;
import devesh.ephrine.R;
import devesh.common.database.contactsdb.ContactUser;


public class MyContactAdapter extends RecyclerView.Adapter<MyContactAdapter.MyViewHolder> {

    private static final Random random = new Random();
    private final List<ContactUser> mDataset;
    public String TAG = String.valueOf(R.string.app_name);
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
                .inflate(R.layout.recycleview_contact_list_item, parent, false);
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
        holder.contactPhone.setText(mDataset.get(position).phone);

        if (mDataset.get(position).isAppUser == 1) {
            // User is on App
            holder.LLContact.setTag(mDataset.get(position).UID);
            holder.inviteButton.setVisibility(View.GONE);

            holder.LLAdd2Fav.setVisibility(View.VISIBLE);
            holder.LLAdd2Fav.setTag(position);

            Glide.with(mContext).load(mDataset.get(position).photo)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(holder.contactImage);


            if (mDataset.get(position).isFav == 0) {
                holder.isFavImg.setImageDrawable(mContext.getDrawable(R.drawable.ic_round_favorite_border_30));
            } else if (mDataset.get(position).isFav == 1) {
                holder.isFavImg.setImageDrawable(mContext.getDrawable(R.drawable.ic_round_favorite_30));
            }

        } else {

// User is not on App
            holder.inviteButton.setTag(mDataset.get(position).phone);
            holder.inviteButton.setVisibility(View.VISIBLE);
            holder.LLAdd2Fav.setVisibility(View.GONE);

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

        holder.inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: " + view.getTag());
                if (view.getTag() != null) {
                    String tag = view.getTag().toString();
                }

                /*Intent intent = new Intent(mContext, ProductActivity.class);
                intent.putExtra("productid", tag);
                mContext.startActivity(intent); */
            }
        });

        holder.LLAdd2Fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //view.getTag();
                Log.d(TAG, "onClick: " + view.getTag());
                if (view.getTag() != null) {
                    String tag = view.getTag().toString();
                    if (mContext instanceof MainActivity) {
                        int i = Integer.parseInt(tag);
                        if (mDataset.get(i).isFav == 0) {

                            ((MainActivity) mContext).Edit2Fav(mDataset.get(i).UID, 1);
                        } else if (mDataset.get(i).isFav == 1) {
                            ((MainActivity) mContext).Edit2Fav(mDataset.get(i).UID, 0);
                        } else {
                            ((MainActivity) mContext).Edit2Fav(mDataset.get(i).UID, 1);
                        }
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
        public TextView contactPhone;
        public CircleImageView contactImage;
        public LinearLayout LLContact;
        public Button inviteButton;
        public LinearLayout LLAdd2Fav;
        public ImageView isFavImg;

        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            contactName = v.findViewById(R.id.contactName);
            contactPhone = v.findViewById(R.id.contactPhone);
            LLContact = v.findViewById(R.id.LLContact);
            contactImage = v.findViewById(R.id.contactImage);
            inviteButton = v.findViewById(R.id.inviteButton);
            isFavImg = v.findViewById(R.id.isFavImg);
            LLAdd2Fav = v.findViewById(R.id.LLAdd2Fav);
        }
    }


}

