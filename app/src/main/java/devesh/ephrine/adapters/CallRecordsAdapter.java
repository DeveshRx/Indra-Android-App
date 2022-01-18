package devesh.ephrine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

import devesh.ephrine.R;
import devesh.common.utils.EpochLib;
import devesh.ephrine.rooms.CallHistory.CallRecord;

public class CallRecordsAdapter extends RecyclerView.Adapter<CallRecordsAdapter.MyViewHolder> {

    private static final Random random = new Random();
    private final List<CallRecord> mDataset;
    public String TAG = String.valueOf(R.string.app_name);
    //   public UserProfileManager mUser;
    public Context mContext;
    //Gson gson;
    EpochLib epochLib;

    // Provide a suitable constructor (depends on the kind of dataset)
    public CallRecordsAdapter(Context Context, List<CallRecord> myDataset) {

        // gson = new Gson();
        mDataset = myDataset;
        mContext = Context;
        epochLib = new EpochLib();
        // mUser.Download();
    }


    // Create new views (invoked by the layout manager)
    @Override
    public CallRecordsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        // TextView v = (TextView) LayoutInflater.from(parent.getContext())
        //       .inflate(R.layout.recycleview_books_list, parent, false);

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_callrecord_item, parent, false);
        // Give the view as it is
        CallRecordsAdapter.MyViewHolder vh = new CallRecordsAdapter.MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CallRecordsAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.textView.setText(mDataset.get(position).get("cs"));
        //   holder.ItemCardView.setTag(mDataset.get(position).get("bookid"));
        //    holder.AddToLibraryChip.setTag(mDataset.get(position).get("bookid"));
        long epoch = mDataset.get(position).time_epoch;

        String CRTime = epochLib.getEPOCHFormatted(epoch, "hh:mm a");
        String CRDate = epochLib.getEPOCHFormatted(epoch, "dd-MMM-yyyy");
        holder.CallRecordDateTime.setText(CRDate + " at " + CRTime);
        String CallStatus = null;
        if (mDataset.get(position).io.equals("i")) {
            holder.IOReceived.setVisibility(View.VISIBLE);
            holder.IOOutgoing.setVisibility(View.GONE);
            CallStatus = "Call Received";
        } else {
            holder.IOReceived.setVisibility(View.GONE);
            holder.IOOutgoing.setVisibility(View.VISIBLE);
            CallStatus = "Call Made";
        }
        holder.CallRecordIO.setText(CallStatus);


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

        public TextView CallRecordDateTime;
        public TextView CallRecordIO;

        public ImageView IOReceived;
        public ImageView IOOutgoing;

        public MyViewHolder(View v) {
            super(v);
            //     textView = v.findViewById(R.id.textView3);
            CallRecordDateTime = v.findViewById(R.id.CallDTtextView2);
            CallRecordIO = v.findViewById(R.id.CallIOtextView3);
            IOReceived = v.findViewById(R.id.IOimageViewReceived);
            IOOutgoing = v.findViewById(R.id.IOimageViewOutGoing);
        }
    }


}


