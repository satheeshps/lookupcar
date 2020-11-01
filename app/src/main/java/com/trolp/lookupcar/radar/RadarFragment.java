package com.trolp.lookupcar.radar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.trolp.lookupcar.MessageListener;
import com.trolp.lookupcar.R;

public class RadarFragment extends Fragment {
    private RadarView view = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.radar_fragment, container, false);
        view = (RadarView) root.findViewById(R.id.canvas);
        if(view != null)
            view.setListener(new MessageListener() {
                @Override
                public void onNewMessage(String title, String desc) {
                    TextView msgTitle = (TextView) getActivity().findViewById(R.id.parkItemTxt);
                    TextView msgDetail = (TextView) getActivity().findViewById(R.id.parkTimeItemTxt);
                    msgTitle.setText(title);
                    msgDetail.setText(desc);
                }
            });
		return root;
	}

    @Override
    public void onStop() {
        super.onStop();
        view.pause();
    }

    @Override
    public void onPause() {
        super.onPause();
        view.pause();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if(menuVisible) {
            if (view != null)
                view.resume();
        } else {
            if (view != null)
                view.pause();
        }
    }
}
