package bus.ticketer.passenger;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link ShowTicketsFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link ShowTicketsFragment#newInstance} factory
 * method to create an instance of this fragment.
 * 
 */
@SuppressLint("NewApi")
public class ShowTicketsFragment extends Fragment {
	// TODO: Rename and change types and number of parameters
	public static ShowTicketsFragment newInstance() {
		ShowTicketsFragment fragment = new ShowTicketsFragment();
//		Bundle args = new Bundle();
//		args.putString(ARG_PARAM1, param1);
//		args.putString(ARG_PARAM2, param2);
//		fragment.setArguments(args);
		return fragment;
	}

	public ShowTicketsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
//		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_show_tickets, container,
				false);
	}



}