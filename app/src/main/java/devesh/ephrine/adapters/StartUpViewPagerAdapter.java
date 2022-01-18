package devesh.ephrine.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.HashMap;

import devesh.ephrine.R;


public class StartUpViewPagerAdapter extends RecyclerView.Adapter<StartUpViewPagerAdapter.MyViewHolder>  {
    Context mContext ;
    ArrayList<HashMap<String,String>> mListScreen;

    public StartUpViewPagerAdapter(Context mContext, ArrayList<HashMap<String,String>> mListScreen) {
        this.mContext = mContext;
        this.mListScreen = mListScreen;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.viewpager_startup, parent, false);
        // Give the view as it is
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.title.setText(mListScreen.get(position).get("title"));
        holder.description.setText(mListScreen.get(position).get("desc"));
        String id=mListScreen.get(position).get("id");

        Resources res = mContext.getResources();
        Drawable drawable;
        if(position==0){
            drawable = ResourcesCompat.getDrawable(res, R.drawable.pic2, null);
            holder.ImgBanner.setImageDrawable(drawable);
holder.loginButton.setVisibility(View.GONE);
        }else if(position==1){
            drawable = ResourcesCompat.getDrawable(res, R.drawable.pic1, null);
            holder.ImgBanner.setImageDrawable(drawable);
            holder.loginButton.setVisibility(View.GONE);
        }
        else if(position==2){
             drawable = ResourcesCompat.getDrawable(res, R.drawable.screenshot_protection_hand_with_screen, null);
            holder.ImgBanner.setImageDrawable(drawable);
            holder.loginButton.setVisibility(View.GONE);
        }else if(position==3){
  /*          Bitmap mbitmap=((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.pic3,null)).getBitmap();
            Bitmap imageRounded=Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
            Canvas canvas=new Canvas(imageRounded);
            Paint mpaint=new Paint();
            mpaint.setAntiAlias(true);
            mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 100, 100, mpaint); // Round Image Corner 100 100 100 100
            holder.ImgBanner.setImageBitmap(mbitmap);
*/


            drawable = ResourcesCompat.getDrawable(res, R.drawable.pic3, null);

            Glide.with(mContext).load(drawable)
                    .transform(new RoundedCorners(90))
                    .into(holder.ImgBanner);
            holder.loginButton.setVisibility(View.VISIBLE);

        }

        // ImgBanner.setImageResource(mListScreen.get(position).getScreenImg());

       // container.addView(layoutScreen);

    }

    @Override
    public int getItemCount() {
        return mListScreen.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public ImageView ImgBanner;
        public TextView title;
        public TextView description;
public Button loginButton;

        public MyViewHolder(View v) {
            super(v);
             ImgBanner = v.findViewById(R.id.ImgBanner);
             title = v.findViewById(R.id.Heading);
             description = v.findViewById(R.id.SubHeading);
            loginButton=v.findViewById(R.id.GetStartedButton);
        }
    }

}
