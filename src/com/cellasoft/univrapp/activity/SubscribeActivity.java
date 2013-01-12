package com.cellasoft.univrapp.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.cellasoft.univrapp.adapter.LecturerAdapter;
import com.cellasoft.univrapp.manager.ContentManager;
import com.cellasoft.univrapp.model.Channel;
import com.cellasoft.univrapp.model.Lecturer;
import com.cellasoft.univrapp.utils.Constants;
import com.cellasoft.univrapp.utils.ImageLoader;
import com.cellasoft.univrapp.utils.Settings;

public class SubscribeActivity extends SherlockActivity {
	static final int START_DATE_DIALOG_ID = 0;
	static final int END_DATE_DIALOG_ID = 1;
	private Spinner sp_lecturer;
	private EditText startDate;
	private EditText endDate;
	private PostData post_data;
	ArrayList<Lecturer> lecturers;
	LecturerAdapter lctAdapter;

	class PostData {
		public static final int ALL = -1;
		public static final int NO_ONE = 0;

		int gi;
		int mi;
		int ai;
		int gf;
		int mf;
		int af;
		int personeMittente;
		int struttureMittente = NO_ONE;
		int biblioCRMittente = NO_ONE;
		int csMittente = NO_ONE;
		int oiMittente = NO_ONE;

		public Map<String, Integer> getPostData() {
			Map<String, Integer> post = new HashMap<String, Integer>();
			post.put("gi", gi);
			post.put("mi", mi);
			post.put("ai", ai);
			post.put("gf", gf);
			post.put("mf", mf);
			post.put("af", af);
			post.put("personeMittente", personeMittente);
			post.put("struttureMittente", struttureMittente);
			post.put("biblioCRMittente", biblioCRMittente);
			post.put("csMittente", csMittente);
			post.put("oiMittente", oiMittente);
			return post;
		}

		public String setParams() {
			Map<String, Integer> params = getPostData();
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> postParams = new ArrayList<NameValuePair>();

				for (Map.Entry<String, Integer> entry : params.entrySet())
					if (entry.getValue() != null)
						postParams.add(new BasicNameValuePair(entry.getKey(),
								String.valueOf(entry.getValue())));
				return "&" + URLEncodedUtils.format(postParams, "utf-8");
			}
			return "";
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subscribe);
		ImageLoader.initialize(this);
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadData();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case START_DATE_DIALOG_ID:
			return new DatePickerDialog(this, mStartDateSetListener,
					post_data.ai, post_data.mi - 1, post_data.gi);
		case END_DATE_DIALOG_ID:
			return new DatePickerDialog(this, mEndDateSetListener,
					post_data.af, post_data.mf - 1, post_data.gf);
		default:
			return super.onCreateDialog(id);
		}
	}

	private void init() {
		startDate = (EditText) findViewById(R.id.subscribe_etStartDate);
		endDate = (EditText) findViewById(R.id.subscribe_etEndDate);
		sp_lecturer = (Spinner) findViewById(R.id.subscribe_lecturer);

		findViewById(R.id.subscribe_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});

		findViewById(R.id.subscribe_save).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						confirmBeforeSavingSubscriptions();
						setResult(Activity.RESULT_OK);

					}
				});

		findViewById(R.id.subscribe_btnStartDate).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						showDialog(START_DATE_DIALOG_ID);
					}
				});

		findViewById(R.id.subscribe_btnEndDate).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						showDialog(END_DATE_DIALOG_ID);
					}
				});

		post_data = new PostData();
		// get the current date
		Calendar c = Calendar.getInstance();
		post_data.gi = c.get(Calendar.DAY_OF_MONTH);
		post_data.mi = c.get(Calendar.MONTH) + 1;
		post_data.ai = c.get(Calendar.YEAR);
		post_data.gf = c.get(Calendar.DAY_OF_MONTH);
		post_data.mf = c.get(Calendar.MONTH) + 1;
		post_data.af = c.get(Calendar.YEAR) + 5;

		updateStartDate();
		updateEndDate();
	}

	private void updateStartDate() {
		startDate.setText(new StringBuilder().append(post_data.gi).append("/")
				.append(post_data.mi).append("/").append(post_data.ai)
				.append(" "));
	}

	private void updateEndDate() {
		endDate.setText(new StringBuilder().append(post_data.gf).append("/")
				.append(post_data.mf).append("/").append(post_data.af)
				.append(" "));
	}

	private DatePickerDialog.OnDateSetListener mStartDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			post_data.gi = dayOfMonth;
			post_data.mi = monthOfYear + 1;
			post_data.ai = year;
			updateStartDate();
		}
	};
	private DatePickerDialog.OnDateSetListener mEndDateSetListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			post_data.gf = dayOfMonth;
			post_data.mf = monthOfYear + 1;
			post_data.af = year;
			updateEndDate();
		}
	};

	private void loadData() {
		loadLecturers();
		lctAdapter = new LecturerAdapter(this, lecturers);
		sp_lecturer.setAdapter(lctAdapter);
	}

	private void loadLecturers() {
		int dest = Constants.UNIVERSITY.DEST.get(Settings.getUniversity());
		lecturers = ContentManager.loadLecturersOfDest(dest,
				ContentManager.FULL_LECTURER_LOADER);
		if (lecturers == null || lecturers.isEmpty()) {
			lecturers = new ArrayList<Lecturer>();
		}
	}

	private Runnable updateLecturers = new Runnable() {

		@Override
		public void run() {
			loadData();
		}
	};

	private void confirmBeforeSavingSubscriptions() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle("Subscribe")
				.setMessage("Subscribe channel?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								saveSubscriptions();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	private void saveSubscriptions() {
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Unsubscribe selected channels");
		AsyncTask<Void, Void, Void> subscribingTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				progressDialog.dismiss();

				String message = "Successfull";
				Toast.makeText(SubscribeActivity.this, message, 1000).show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				Lecturer lecturer = (Lecturer) sp_lecturer.getSelectedItem();
				post_data.personeMittente = lecturer.key;
				Channel channel = new Channel(lecturer.name,
						Constants.UNIVERSITY.URL.get(Settings.getUniversity())
								+ post_data.setParams(), lecturer.thumbnail);
				ContentManager.saveChannel(channel);

				return null;
			}
		};

		progressDialog.show();
		subscribingTask.execute();
	}
}