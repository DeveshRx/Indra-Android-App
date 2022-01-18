package devesh.ephrine.fragments;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import devesh.common.utils.CachePref;
import devesh.ephrine.MainActivity;
import devesh.ephrine.R;
import devesh.ephrine.databinding.FragmentUserStatusBinding;


public class StatusFragment extends Fragment {

    String TAG = "StatusFagment";
    //FragmentStatusBinding mBinding;
    FragmentUserStatusBinding mBinding;
    Drawable StatusGreenIMG;
    Drawable StatusYellowIMG;
    Drawable StatusRedIMG;
    CachePref PrefUserStatus;
    public StatusFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getActivity().getResources();
        PrefUserStatus= new CachePref(getActivity());
        StatusGreenIMG = ResourcesCompat.getDrawable(res, R.drawable.user_status_green, null);
        StatusYellowIMG = ResourcesCompat.getDrawable(res, R.drawable.user_status_yellow, null);
        StatusRedIMG = ResourcesCompat.getDrawable(res, R.drawable.user_status_red, null);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_status, container, false);
        mBinding = FragmentUserStatusBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
mBinding.spinner2.setSelection(0, false);
        mBinding.spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "onItemSelected: " + position);
                if (position == 0) {
                    //Available
                    mBinding.StatusImageView.setImageDrawable(StatusGreenIMG);
                    mBinding.StatusTextview.setVisibility(View.GONE);
                    UpdateStatus(getString(R.string.USER_STATUS_AVAILABLE));
                } else if (position == 1) {
                    //Busy
                    mBinding.StatusImageView.setImageDrawable(StatusYellowIMG);
                    mBinding.StatusTextview.setVisibility(View.VISIBLE);
                    mBinding.StatusTextview.setText("You will not receive calls");
                    mBinding.StatusTextview.setTextColor(Color.YELLOW);
                    UpdateStatus(getString(R.string.USER_STATUS_BUSY));
                } else if (position == 2) {
                    //DND
                    mBinding.StatusImageView.setImageDrawable(StatusRedIMG);
                    mBinding.StatusTextview.setVisibility(View.VISIBLE);
                    mBinding.StatusTextview.setText("You will not receive calls");
                    mBinding.StatusTextview.setTextColor(Color.RED);
                    UpdateStatus(getString(R.string.USER_STATUS_DND));
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getCurrentStatus();


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    void UpdateStatus(String status) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).SetUserStatus(status);
            PrefUserStatus.setString(getString(R.string.PREF_USER_CURRENT_STATUS),status);

        }
    }
    void getCurrentStatus(){

        try {
String cs=PrefUserStatus.getString(getString(R.string.PREF_USER_CURRENT_STATUS));
            if(cs.equals(getString(R.string.USER_STATUS_AVAILABLE))){
                mBinding.spinner2.setEnabled(true);
mBinding.spinner2.setSelection(0);
            } else if(cs.equals(getString(R.string.USER_STATUS_BUSY))){
                mBinding.spinner2.setEnabled(true);
                mBinding.spinner2.setSelection(1);
            } else if(cs.equals(getString(R.string.USER_STATUS_DND))){
                mBinding.spinner2.setEnabled(true);
                mBinding.spinner2.setSelection(2);
            }
            else if(cs.equals(getString(R.string.USER_STATUS_ON_CALL))){
                mBinding.spinner2.setEnabled(false);
            }


        }catch (Exception e){
            Log.e(TAG, "getCurrentStatus: "+e );
        }

    }
}