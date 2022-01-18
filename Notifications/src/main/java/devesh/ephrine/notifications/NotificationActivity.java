package devesh.ephrine.notifications;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

import devesh.ephrine.notifications.recycleview.NotiAdapter;
import devesh.ephrine.notifications.room.NotificationAppDatabase;
import devesh.ephrine.notifications.room.NotifictionData;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class NotificationActivity extends AppCompatActivity {

    NotificationAppDatabase NotiDB;
    RecyclerView recycleView;
    List<NotifictionData> nData;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    String TAG = "Notification Act:";
    NotiAdapter mAdapter;
    String AppFlavor;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.BG_Blank)));

        Intent intent = getIntent();
        AppFlavor = intent.getStringExtra("flavor");


        recycleView = findViewById(R.id.recycleview_notification);

        NotiDB = Room.databaseBuilder(this, NotificationAppDatabase.class, getString(R.string.DATABASE_NOTIFICATION))
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        nData = NotiDB.notificationDAO().getAllbyTime();


        recycleView.setHasFixedSize(true);
        //recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycleView.setLayoutManager(layoutManager);
        mAdapter = new NotiAdapter(this, nData, AppFlavor);
        recycleView.setAdapter(mAdapter);

        itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: ");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Row is swiped from recycler view
                // remove it from adapter
                final int position = viewHolder.getAdapterPosition();
                Log.d(TAG, "onSwiped: " + position);

                NotifictionData nd = mAdapter.getItemData(position);
                mAdapter.delete(position);
                NotiDB.notificationDAO().delete(nd);
                //mAdapter.notifyItemChanged(position);
                /*
                  switch (direction) {
                    case ItemTouchHelper.LEFT:
                        //Profile
                        NotiDB.notificationDAO().delete(nd);
                        break;

                    case ItemTouchHelper.RIGHT:
                        // Call
                        NotiDB.notificationDAO().delete(nd);
                        break;
                }
                */

            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // view the background view
                Log.d(TAG, "onChildDraw: dX:" + dX + " dY:" + dY);
                //  final View foregroundView = ((MyContactAdapter.MyViewHolder) viewHolder).viewForeground;

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        //   .addSwipeLeftBackgroundColor(ContextCompat.getColor(NotificationActivity.this, R.color.design_default_color_on_primary))
                        //   .addSwipeLeftActionIcon(R.drawable.ic_baseline_account_circle_30)
                        //.addSwipeLeftLabel("Profile")
                        // .setSwipeLeftLabelColor(R.color.white)
                        //  .addSwipeRightBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Call_Green))
                        //  .addSwipeRightActionIcon(R.drawable.ic_baseline_videocam_30)
                        //.addSwipeRightLabel("Video Call")
                        // .setSwipeRightLabelColor(R.color.white)
                        //.setSwipeLeftLabelTextSize(25,TypedValue.COMPLEX_UNIT_SP)
                        //.setActionIconTint(R.color.white)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };


        ItemTouchHelper ith = new ItemTouchHelper(itemTouchHelperCallback);
        ith.attachToRecyclerView(recycleView);


    }

    public void openWeb(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    public void OpenAppUpdate(View v) {
        String url = v.getTag().toString();
        if (AppFlavor.equals(getString(R.string.FLAVOR_PLAY_STORE))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "https://play.google.com/store/apps/details?id=devesh.ephrine.indra"));
            intent.setPackage("com.android.vending");
            startActivity(intent);

        } else if (AppFlavor.equals(getString(R.string.FLAVOR_GALAXY_STORE))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(
                    "https://galaxystore.samsung.com/detail/devesh.ephrine.indra"));
            startActivity(intent);

        } else if (AppFlavor.equals(getString(R.string.FLAVOR_INTERNAL))) {

            //  Toast.makeText(this, "Download & Install .apk to update", Toast.LENGTH_SHORT).show();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + "?flavor=internal"));
            startActivity(browserIntent);

        } else {
            //Toast.makeText(this, "Download & Install .apk to update", Toast.LENGTH_SHORT).show();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);

            /*
            AppUpdate appUpdate=new AppUpdate(this,NotificationActivity.this);
            try {
                appUpdate.DownloadUpdate(url);
            }catch (Exception e){
                Log.e(TAG, "OpenAppUpdate: ",e );
            }
            */

        }


    }

}

/*

https://firebasestorage.googleapis.com/v0/b/ephrinelab.appspot.com/o/public%2Fapp.apk?alt=media&token=35f6ace6-b20d-4b35-859e-8663b2e86711

  SpannableString FormattedText = new SpannableString(Html.fromHtml(ProductDesc));

        // ProductDescTextView.setText(ProductDesc);
        ProductDescTextView.setText(FormattedText, TextView.BufferType.SPANNABLE);


 */