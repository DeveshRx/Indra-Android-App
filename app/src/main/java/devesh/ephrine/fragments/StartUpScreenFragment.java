package devesh.ephrine.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;

import devesh.ephrine.adapters.StartUpViewPagerAdapter;
import devesh.ephrine.databinding.FragmentStartUpScreenBinding;


public class StartUpScreenFragment extends Fragment {

    final static String TAG = "SUFrag";

    FragmentStartUpScreenBinding mBinding;
    View mView;
    StartUpViewPagerAdapter startUpViewPagerAdapter;
    int position = 0;


    public StartUpScreenFragment() {
        // Required empty public constructor
        //    super(R.layout.fragment_start_up_screen);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentStartUpScreenBinding.inflate(inflater, container, false);
        mView = mBinding.getRoot();
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        ArrayList<HashMap<String, String>> data = new ArrayList<>();

        HashMap<String, String> screen1 = new HashMap<>();
        screen1.put("title", "Indra Video Chat");
        screen1.put("desc", "Stay Connected with your Loved ones");
        screen1.put("img", "x");
        screen1.put("id", "1");
        data.add(screen1);

        HashMap<String, String> screen2 = new HashMap<>();
        screen2.put("title", "Why Indra is Unique?");
        screen2.put("desc", "Peer-to-Peer encrypted connection without any middle server as relay");
        screen2.put("img", "x");
        screen2.put("id", "2");
        data.add(screen2);

        HashMap<String, String> screen3 = new HashMap<>();
        screen3.put("title", "Screenshot Protection");
        screen3.put("desc", "Block screenshot & screen recording to protect you from bad actors and cyberbullying.");
        screen3.put("img", "x");
        screen3.put("id", "3");
        data.add(screen3);

        HashMap<String, String> screen4 = new HashMap<>();
        screen4.put("title", "Let's Get Started");
        screen4.put("desc", " ");
        screen4.put("img", "x");
        screen4.put("id", "4");
        data.add(screen4);


        startUpViewPagerAdapter = new StartUpViewPagerAdapter(mView.getContext(), data);
        mBinding.ViewPager.setAdapter(startUpViewPagerAdapter);

        new TabLayoutMediator(mBinding.PageIndicator, mBinding.ViewPager, (tab, position) -> {
            Log.d(TAG, "onStart: " + position);
            //  tab.setCustomView(react_with_any_emoji_tab)
            //        .setIcon(ThemeUtil.getThemedDrawable(requireContext(), viewModel.getCategoryIconAttr(position)));

        }).attach();

        mBinding.NextButton.setOnClickListener(view -> {
            position = mBinding.ViewPager.getCurrentItem();
            if (position < data.size()) {
                position++;
                mBinding.ViewPager.setCurrentItem(position);
            }

            if (position == data.size() - 1) { // when we rech to the last screen
                loaddLastScreen();
            }
        });


        mBinding.ViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position < data.size()) {
                    //  position++;
                    // mBinding.ViewPager.setCurrentItem(position);
                    mBinding.NextButton.setVisibility(View.VISIBLE);
                    mBinding.PageIndicator.setVisibility(View.VISIBLE);
                }

                if (position == data.size() - 1) {
                    // when we rech to the last screen
                    loaddLastScreen();

                }
                Log.e("Selected_Page", String.valueOf(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }


    void loaddLastScreen() {
        mBinding.NextButton.setVisibility(View.INVISIBLE);
        mBinding.PageIndicator.setVisibility(View.INVISIBLE);
    }

}