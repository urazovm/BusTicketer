package bus.ticketer.fragments;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import bus.ticketer.connection.ConnectionThread;
import bus.ticketer.listeners.TicketPurchaseListener;
import bus.ticketer.objects.Ticket;
import bus.ticketer.passenger.BusTicketer;
import bus.ticketer.passenger.R;
import bus.ticketer.utils.FileHandler;
import bus.ticketer.utils.Method;
import bus.ticketer.utils.PDFWriter;
import bus.ticketer.utils.RESTFunction;

public class BuyTicketsFragment extends Fragment {
	public static final String ARG_OBJECT = "object";
	private View rootView;
	private RESTFunction currentFunction;
	private SparseArray<ArrayList<Ticket>> tickets;
	private int t1Bought, t2Bought, t3Bought, transactionCost;
	private String confirmationToken = "";
	private String IPAddress = "";

	@SuppressLint("HandlerLeak")
	private Handler threadConnectionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (currentFunction) {
				case BUY_CLIENT_TICKETS:
					tickets = ((BusTicketer) getActivity().getApplicationContext()).getTickets();
					break;			
				case BUY_CLIENT_TICKETS_CLICK:
					handlePurchase(msg);
					break;
				case BUY_CONFIRMATION_CLIENT:
					handleBuyPayload(msg);
					break;
				default:
					break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_buy_tickets,
					container, false);

		IPAddress = ((BusTicketer) getActivity().getApplication()).getIPAddress();
		getTicketInfo();
		buyTicketsHandler();

		return rootView;
	}
	
	public void refresh() {
		getTicketInfo();
		buyTicketsHandler();	
	}
	
	public void getTicketInfo() {
		FileHandler fHandler = new FileHandler(((BusTicketer) getActivity().getApplication()).getClientFilename(), "");
		ArrayList<String> fileContents = fHandler.readFromFile();

		
		if(!((BusTicketer) getActivity().getApplication()).isNetworkAvailable()) {
			SparseArray<ArrayList<Ticket>> tickets = FileHandler.getTicketCount();
			((BusTicketer) getActivity().getApplicationContext()).setTickets(tickets);
			quantityHandler();			
		}
		else {
			currentFunction = RESTFunction.BUY_CLIENT_TICKETS;
			
			ConnectionThread dataThread = new ConnectionThread(
					IPAddress+"list/" + fileContents.get(2),
					Method.GET, null, threadConnectionHandler, null,
					currentFunction, rootView, getActivity());
			dataThread.start();
		}
	}

	public void buyTicketsHandler() {

		Button t1Minus = (Button) rootView.findViewById(R.id.t1_ticket_minus);
		Button t2Minus = (Button) rootView.findViewById(R.id.t2_ticket_minus);
		Button t3Minus = (Button) rootView.findViewById(R.id.t3_ticket_minus);
		Button t1Plus = (Button) rootView.findViewById(R.id.t1_ticket_plus);
		Button t2Plus = (Button) rootView.findViewById(R.id.t2_ticket_plus);
		Button t3Plus = (Button) rootView.findViewById(R.id.t3_ticket_plus);

		Button buyTickets = (Button) rootView
				.findViewById(R.id.buy_tickets_button);

		final TextView t1Tickets = (TextView) rootView
				.findViewById(R.id.t1_ticket_quantity_buy);
		final TextView t2Tickets = (TextView) rootView
				.findViewById(R.id.t2_ticket_quantity_buy);
		final TextView t3Tickets = (TextView) rootView
				.findViewById(R.id.t3_ticket_quantity_buy);
		
		t1Tickets.setText("0");
		t2Tickets.setText("0");
		t3Tickets.setText("0");

		t1Minus.setOnClickListener(new TicketPurchaseListener("Minus", t1Tickets));
		t2Minus.setOnClickListener(new TicketPurchaseListener("Minus", t2Tickets));
		t3Minus.setOnClickListener(new TicketPurchaseListener("Minus", t3Tickets));
		t1Plus.setOnClickListener(new TicketPurchaseListener("Plus", t1Tickets));
		t2Plus.setOnClickListener(new TicketPurchaseListener("Plus", t2Tickets));
		t3Plus.setOnClickListener(new TicketPurchaseListener("Plus", t3Tickets));

		buyTickets.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				FileHandler fHandler = new FileHandler(((BusTicketer) getActivity().getApplication()).getClientFilename(), "");
				ArrayList<String> fileContents = fHandler.readFromFile();
				
				final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("cid",
						fileContents.get(2)));
				params.add(new BasicNameValuePair("t1", t1Tickets.getText().toString()));
				params.add(new BasicNameValuePair("t2", t2Tickets.getText().toString()));
				params.add(new BasicNameValuePair("t3", t3Tickets.getText().toString()));

				ProgressDialog progDialog = ProgressDialog.show(getActivity(),
						"", "Loading, please wait!",
						true);

				progDialog.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss(DialogInterface dialog) {
						
						ProgressDialog pDiag = ProgressDialog.show(getActivity(),
								"", "Loading, please wait!",
								true);

						pDiag.setOnDismissListener(new OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								purchaseProcess();
								purchaseSuccess();
							}
						});
						
						confirmPurchase(params, pDiag);
					}
				});
				
				currentFunction = RESTFunction.BUY_CLIENT_TICKETS_CLICK;
				ConnectionThread dataThread = new ConnectionThread(
						IPAddress+"buy/", Method.POST,
						params, threadConnectionHandler, progDialog,
						currentFunction, rootView, getActivity());
				
				dataThread.start();
			}

		});
		
		if(((BusTicketer) getActivity().getApplication()).isTimerOn()) {
			t1Minus.setEnabled(false);
			t2Minus.setEnabled(false);
			t3Minus.setEnabled(false);
			t1Plus.setEnabled(false);
			t2Plus.setEnabled(false);
			t3Plus.setEnabled(false);
			buyTickets.setEnabled(false);
		}
		else {
			t1Minus.setEnabled(true);
			t2Minus.setEnabled(true);
			t3Minus.setEnabled(true);
			t1Plus.setEnabled(true);
			t2Plus.setEnabled(true);
			t3Plus.setEnabled(true);
			buyTickets.setEnabled(true);			
		}
		
		if(!((BusTicketer) getActivity().getApplication()).isNetworkAvailable()) {
			t1Minus.setEnabled(false);
			t2Minus.setEnabled(false);
			t3Minus.setEnabled(false);
			t1Plus.setEnabled(false);
			t2Plus.setEnabled(false);
			t3Plus.setEnabled(false);
			buyTickets.setEnabled(false);			
		}
	}
	
	private void purchaseSuccess() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		alertDialogBuilder.setTitle("Purchase succeeded!");

		alertDialogBuilder
		.setMessage("You have bought: " + t1Bought + " T1 Tickets, " + t2Bought + " T2 Tickets and " + t3Bought + " T3 Tickets, for " + transactionCost + "�.")
		.setCancelable(false)
		.setPositiveButton("OK",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
		        refresh();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();		
	}
	
	private void confirmPurchase(final ArrayList<NameValuePair> params, final ProgressDialog pDiag) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		alertDialogBuilder.setTitle("Confirm your purchase");

		alertDialogBuilder
		.setMessage(buildConfirmationMessage())
		.setCancelable(false)
		.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				params.add(new BasicNameValuePair("token", confirmationToken));
				
				currentFunction = RESTFunction.BUY_CONFIRMATION_CLIENT;
				ConnectionThread dataThread = new ConnectionThread(
						IPAddress+"buy/", Method.POST,
						params, threadConnectionHandler, pDiag,
						currentFunction, rootView, getActivity());
				dataThread.start();
			}
		})
		.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
		        refresh();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();		
	}

	private String buildConfirmationMessage() {
		String ret = "";
		
		if(t1Bought != 0) {
			ret += t1Bought + "t1 tickets";
			if(t2Bought != 0) {
				ret += ", " + t2Bought + "t2 tickets";
				if(t3Bought != 0) ret += " and " + t3Bought + "t3 tickets";
			}
			else
				if(t3Bought != 0) ret += " and " + t3Bought + "t3 tickets";
		}
		else {
			if(t2Bought != 0) {
				ret += t2Bought + "t2 tickets";
				if(t3Bought != 0) ret += " and " + t3Bought + "t3 tickets";
			}
			else {
				if(t3Bought != 0) ret += t3Bought + "t3 tickets";
				ret = "(no tickets)";
			}
		}
		
		return "You will get a bonus of: " + ret + " and it will cost you " + transactionCost + " �.";
	}
	
	private void handleBuyPayload(Message msg) {
        JSONObject ticketListing = (JSONObject) msg.obj;
        try {
        	JSONArray t1Array = ticketListing.getJSONArray("t1");
        	
        	for(int i = 0; i < t1Array.length(); i++)
        		tickets.get(1).add(new Ticket(t1Array.getInt(i)));
        	
        	JSONArray t2Array = ticketListing.getJSONArray("t2");
        	for(int i = 0; i < t2Array.length(); i++)
        		tickets.get(2).add(new Ticket(t2Array.getInt(i)));
        	
        	JSONArray t3Array = ticketListing.getJSONArray("t3");
        	for(int i = 0; i < t3Array.length(); i++)
        		tickets.get(3).add(new Ticket(t3Array.getInt(i)));
        	        	
        	t1Bought = t1Array.length();
        	t2Bought = t2Array.length();
        	t3Bought = t3Array.length();
        	
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ((BusTicketer) getActivity().getApplicationContext()).setTickets(tickets);
        
	}
	
	private void handlePurchase(Message msg) {
        JSONObject ticketListing = (JSONObject) msg.obj;
        try {
            t1Bought = ticketListing.getInt("t1");
            t2Bought = ticketListing.getInt("t2");
            t3Bought = ticketListing.getInt("t3");
            transactionCost = ticketListing.getInt("cost");
            confirmationToken = ticketListing.getString("token");
        } catch (JSONException e) {
            e.printStackTrace();
        }
	}
	
	private void purchaseProcess() {
		FileHandler fHandler = new FileHandler(((BusTicketer) getActivity().getApplication()).getClientFilename(), "");
		ArrayList<String> fileContents = fHandler.readFromFile();
		ArrayList<Ticket> t1Tickets = tickets.get(1);
		ArrayList<Ticket> t2Tickets = tickets.get(2);
		ArrayList<Ticket> t3Tickets = tickets.get(3);
		
		for(Ticket t : t1Tickets) {
			if(!FileHandler.checkFileExistance(t.getTicketID()+".pdf"))
				new PDFWriter("t1Ticket-"+t.getTicketID()+".pdf", "T1", fileContents.get(0), null, false).createFile();
		}

		for(Ticket t : t2Tickets) {
			if(!FileHandler.checkFileExistance(t.getTicketID()+".pdf"))
				new PDFWriter("t2Ticket-"+t.getTicketID()+".pdf", "T1", fileContents.get(0), null, false).createFile();
		}
		
		for(Ticket t : t3Tickets) {
			if(!FileHandler.checkFileExistance(t.getTicketID()+".pdf"))
				new PDFWriter("t3Ticket-"+t.getTicketID()+".pdf", "T1", fileContents.get(0), null, false).createFile();
		}
	}
	
	private void quantityHandler() {
		TextView t1TicketsQuantity = (TextView) rootView
				.findViewById(R.id.t1_ticket_quantity);
		TextView t2TicketsQuantity = (TextView) rootView
				.findViewById(R.id.t2_ticket_quantity);
		TextView t3TicketsQuantity = (TextView) rootView
				.findViewById(R.id.t3_ticket_quantity);

		SparseArray<ArrayList<Ticket>> tickets = ((BusTicketer) getActivity().getApplicationContext()).getTickets();
		
		t1TicketsQuantity.setText(tickets.get(1).size() + "");
		t2TicketsQuantity.setText(tickets.get(2).size() + "");
		t3TicketsQuantity.setText(tickets.get(3).size() + "");
	}	
}