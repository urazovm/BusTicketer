package bus.ticketer.fragments;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;
import bus.ticketer.connection.ConnectionThread;
import bus.ticketer.passenger.R;
import bus.ticketer.utils.Method;

public class CentralFragment extends Fragment {
	public static final String ARG_OBJECT = "object";
	private View rootView;
	
	
	@SuppressLint("HandlerLeak")
	private Handler threadConnectionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			System.out.println(msg.obj.toString());
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Bundle args = getArguments();
		if (args.getInt(ARG_OBJECT) == 1) {
			rootView = inflater.inflate(R.layout.fragment_show_tickets,
					container, false);

			showTicketsHandler();

			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			BasicNameValuePair one = new BasicNameValuePair("name",
					"derpus");
			BasicNameValuePair two = new BasicNameValuePair("nib", "123456");
			BasicNameValuePair three = new BasicNameValuePair("pass",
					"asdsad21iuo46tb");
			params.add(one);
			params.add(two);
			params.add(three);

			ConnectionThread dataThread = new ConnectionThread(
					"http://192.168.0.136:81/client/create/", Method.POST,
					params, threadConnectionHandler);
			dataThread.start();
			
		} else if (args.getInt(ARG_OBJECT) == 2) {
			rootView = inflater.inflate(R.layout.fragment_buy_tickets,
					container, false);

			buyTicketsHandler();
		} else {
			rootView = inflater.inflate(R.layout.fragment_history_tickets,
					container, false);
		}

		return rootView;
	}

	public void showTicketsHandler() {
		RadioGroup radioGroup = (RadioGroup) rootView
				.findViewById(R.id.ticket_radio);
		radioGroup.check(R.id.t1_radio);
		final TextView ticketsText = (TextView) rootView
				.findViewById(R.id.show_ticket_amount);
		ticketsText.setText("T1 Tickets");
		radioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group,
							int checkedId) {
						switch (checkedId) {
						case R.id.t1_radio:
							ticketsText.setText("T1 Tickets");
							break;
						case R.id.t2_radio:
							ticketsText.setText("T2 Tickets");
							break;
						case R.id.t3_radio:
							ticketsText.setText("T3 Tickets");
							break;
						default:
							break;
						}

					}
				});
	}

	public void buyTicketsHandler() {
		Button t1Minus = (Button) rootView
				.findViewById(R.id.t1_ticket_minus);
		Button t2Minus = (Button) rootView
				.findViewById(R.id.t2_ticket_minus);
		Button t3Minus = (Button) rootView
				.findViewById(R.id.t3_ticket_minus);
		Button t1Plus = (Button) rootView.findViewById(R.id.t1_ticket_plus);
		Button t2Plus = (Button) rootView.findViewById(R.id.t2_ticket_plus);
		Button t3Plus = (Button) rootView.findViewById(R.id.t3_ticket_plus);
		final TextView t1Tickets = (TextView) rootView
				.findViewById(R.id.t1_ticket_quantity_buy);
		final TextView t2Tickets = (TextView) rootView
				.findViewById(R.id.t2_ticket_quantity_buy);
		final TextView t3Tickets = (TextView) rootView
				.findViewById(R.id.t3_ticket_quantity_buy);

		t1Minus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int current = Integer.parseInt(t1Tickets.getText()
						.toString());

				if (current == 0)
					return;

				current--;
				t1Tickets.setText(current + "");
			}

		});

		t2Minus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int current = Integer.parseInt(t2Tickets.getText()
						.toString());

				if (current == 0)
					return;

				current--;
				t2Tickets.setText(current + "");
			}

		});

		t3Minus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int current = Integer.parseInt(t3Tickets.getText()
						.toString());

				if (current == 0)
					return;

				current--;
				t3Tickets.setText(current + "");
			}

		});

		t1Plus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int current = Integer.parseInt(t1Tickets.getText()
						.toString());
				current++;
				t1Tickets.setText(current + "");
			}

		});

		t2Plus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int current = Integer.parseInt(t2Tickets.getText()
						.toString());
				current++;
				t2Tickets.setText(current + "");
			}

		});

		t3Plus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int current = Integer.parseInt(t3Tickets.getText()
						.toString());
				current++;
				t3Tickets.setText(current + "");
			}

		});
	}
}