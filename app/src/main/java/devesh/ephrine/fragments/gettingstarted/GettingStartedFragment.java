package devesh.ephrine.fragments.gettingstarted;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import devesh.ephrine.R;
import devesh.ephrine.databinding.FragmentGettingStartedBinding;


public class GettingStartedFragment extends Fragment {


FragmentGettingStartedBinding mBinding;

    public GettingStartedFragment() {
        // Required empty public constructor
    }





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentGettingStartedBinding.inflate(inflater, container, false);
        View view = mBinding.getRoot();
        return view;
        // Inflate the layout for this fragment
      //  return inflater.inflate(R.layout.fragment_getting_started, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
mBinding.QuickTutorialCardView.setOnClickListener(v -> {
    
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(Uri.parse(getString(R.string.URL_getting_started)));
    startActivity(intent);

});

    }
}