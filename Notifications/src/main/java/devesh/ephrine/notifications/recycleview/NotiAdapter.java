package devesh.ephrine.notifications.recycleview;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;

import devesh.ephrine.notifications.NotificationActivity;
import devesh.ephrine.notifications.room.NotifictionData;
import devesh.ephrine.notifications.R;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.MyViewHolder> {

    private static final Random random = new Random();
    private final List<NotifictionData> mDataset;
    public String TAG = "NotiAdapter";
    //   public UserProfileManager mUser;
    public Context mContext;
    //Gson gson;
    String AppFlavor;
    public NotiAdapter(Context Context, List<NotifictionData> myDataset,String App_Flavor) {

        // gson = new Gson();
        mDataset = myDataset;
        mContext = Context;
        Glide.get(mContext).clearMemory();
        AppFlavor=App_Flavor;
        // mUser.Download();
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_item, parent, false);
        // Give the view as it is
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {



        SpannableString FormattednLongSummary = new SpannableString(Html.fromHtml(mDataset.get(position).Long_Message));

        holder.nTitle.setText(mDataset.get(position).Title);
        holder.nLongSummary.setText(FormattednLongSummary, TextView.BufferType.SPANNABLE);
        holder.CardItem.setTag(mDataset.get(position).Url);


        if(mDataset.get(position).Type.equals(mContext.getString(R.string.NOTIFICATION_NORMAL))){
            holder.LLUpdateSec.setVisibility(View.GONE);
            holder.CardItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //view.getTag();
                    Log.d(TAG, "onClick: " + view.getTag());
                    if (view.getTag() != null) {
                        String tag = view.getTag().toString();
                        if(tag.equals("x") || tag.equals("X")){
                        }else{
                            if (mContext instanceof NotificationActivity) {
                                ((NotificationActivity) mContext).openWeb(tag);
                            }
                        }
                    }
                }
            });

        }else if(mDataset.get(position).Type.equals(mContext.getString(R.string.NOTIFICATION_APP_UPDATE))) {
            holder.LLUpdateSec.setVisibility(View.VISIBLE);
            holder.UpdateButton.setTag(mDataset.get(position).Url);

            /*          holder.CardItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //view.getTag();
                    Log.d(TAG, "onClick: " + view.getTag());
                    if (view.getTag() != null) {
                        String tag = view.getTag().toString();
                        if(tag.equals("x") || tag.equals("X")){

                        }else{
                            if (mContext instanceof NotificationActivity) {
                                ((NotificationActivity) mContext).OpenAppUpdate(tag);
                            }
                        }


                    }
                }
            });
*/
        }


    }

    public NotifictionData getItemData(int pos) {
        if (mDataset.get(pos) != null) {
            return mDataset.get(pos);
        } else {
            return null;
        }

    }

    public void delete(int position){
        mDataset.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());
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

        public TextView nTitle;
        public TextView nLongSummary;
        public CardView CardItem;
        public ImageView nIcon;
        public LinearLayout LLUpdateSec;
public Button UpdateButton;

        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            nTitle = v.findViewById(R.id.nTitle);
            nLongSummary = v.findViewById(R.id.nLongSummary);
            CardItem = v.findViewById(R.id.CardItem);
            nIcon = v.findViewById(R.id.nIcon);
            LLUpdateSec=v.findViewById(R.id.LLUpdateSec);
            UpdateButton=v.findViewById(R.id.UpdateButton);

        }
    }


}
