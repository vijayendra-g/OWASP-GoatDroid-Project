/**
 * OWASP GoatDroid Project
 * 
 * This file is part of the Open Web Application Security Project (OWASP)
 * GoatDroid project. For details, please see
 * https://www.owasp.org/index.php/Projects/OWASP_GoatDroid_Project
 *
 * Copyright (c) 2012 - The OWASP Foundation
 * 
 * GoatDroid is published by OWASP under the GPLv3 license. You should read and accept the
 * LICENSE before you use, modify, and/or redistribute this software.
 * 
 * @author Jack Mannino (Jack.Mannino@owasp.org https://www.owasp.org/index.php/User:Jack_Mannino)
 * @author Walter Tighzert
 * @created 2012
 */
package org.owasp.goatdroid.fourgoats.activities;

import org.owasp.goatdroid.fourgoats.R;
import org.owasp.goatdroid.fourgoats.base.BaseUnauthenticatedActivity;
import org.owasp.goatdroid.fourgoats.db.UserInfoDBHelper;
import org.owasp.goatdroid.fourgoats.jsonobjects.Login;
import org.owasp.goatdroid.fourgoats.misc.Constants;
import org.owasp.goatdroid.fourgoats.misc.Utils;
import org.owasp.goatdroid.fourgoats.request.LoginRequest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BaseUnauthenticatedActivity {

	Context context;
	EditText userNameEditText;
	EditText passwordEditText;
	CheckBox rememberMeCheckBox;
	String previousActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		context = this.getApplicationContext();
		userNameEditText = (EditText) findViewById(R.id.userName);
		passwordEditText = (EditText) findViewById(R.id.password);
		rememberMeCheckBox = (CheckBox) findViewById(R.id.rememberMeCheckBox);
		SharedPreferences prefs = getSharedPreferences("credentials",
				MODE_WORLD_READABLE);
		try {
			previousActivity = getIntent().getExtras().getString(
					"previousActivity");
		} catch (NullPointerException e) {
			previousActivity = "";
		}
		userNameEditText.setText(prefs.getString("username", ""));
		passwordEditText.setText(prefs.getString("password", ""));
		if (prefs.getBoolean("remember", true))
			rememberMeCheckBox.setChecked(true);
		else
			rememberMeCheckBox.setChecked(false);

	}

	public void checkCredentials(View v) {

		if (allFieldsCompleted(userNameEditText.getText().toString(),
				passwordEditText.getText().toString())) {
			ValidateCredsAsyncTask validate = new ValidateCredsAsyncTask(this);
			validate.execute(null, null);
		} else
			Utils.makeToast(context, Constants.ALL_FIELDS_REQUIRED,
					Toast.LENGTH_LONG);

	}

	public void launchRegistration(View v) {
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(intent);
	}

	public void launchHome() {
		Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
		startActivity(intent);
	}

	public void launchAdminHome() {
		Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
		startActivity(intent);
	}

	public boolean allFieldsCompleted(String userName, String password) {

		return (!userName.equals("") && !password.equals(""));
	}

	private class ValidateCredsAsyncTask extends AsyncTask<Void, Void, Login> {

		LoginActivity mActivity;

		public ValidateCredsAsyncTask(LoginActivity activity) {
			mActivity = activity;
		}

		@Override
		protected Login doInBackground(Void... params) {

			LoginRequest client = new LoginRequest(context);
			String userName = userNameEditText.getText().toString();
			String password = passwordEditText.getText().toString();
			boolean rememberMe = rememberMeCheckBox.isChecked();
			Login login = new Login();
			if (allFieldsCompleted(userName, password)) {
				UserInfoDBHelper dbHelper = new UserInfoDBHelper(context);
				try {
					login = client.validateCredentials(userName, password);
				} catch (Exception e) {
					e.getMessage();
				}
				// if (userInfo.get("success").equals("false"))
				/*
				 * userInfo.put("errors", Constants.LOGIN_FAILED); else {
				 * dbHelper.deleteInfo(); dbHelper.insertSettings(userInfo); if
				 * (rememberMe) saveCredentials(userName, password); // our
				 * secret backdoor account if userInfo.put("isAdmin", "true"); }
				 * } catch (Exception e) { userInfo.put("errors",
				 * Constants.COULD_NOT_CONNECT); userInfo.put("success",
				 * "false"); Log.w("Failed login", "Login with " +
				 * userNameEditText.getText().toString() + " " +
				 * passwordEditText.getText().toString() + " failed"); } finally
				 * { dbHelper.close(); } } else { userInfo.put("error",
				 * Constants.ALL_FIELDS_REQUIRED); userInfo.put("success",
				 * "false"); }
				 */
			}
			return login;

		}

		protected void onPostExecute(Login login) {
			/*
			 * if (results.get("success").equals("true")) { if
			 * (!previousActivity.isEmpty()) { ComponentName toLaunch = new
			 * ComponentName( "org.owasp.goatdroid.fourgoats",
			 * previousActivity); Intent intent = new Intent();
			 * intent.addCategory(Intent.CATEGORY_LAUNCHER);
			 * intent.setComponent(toLaunch);
			 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * startActivity(intent); } else if
			 * (results.get("isAdmin").equals("true")) { Intent intent = new
			 * Intent(mActivity, AdminHomeActivity.class);
			 * startActivity(intent); } else { Intent intent = new
			 * Intent(mActivity, HomeActivity.class); startActivity(intent); } }
			 * else { Utils.makeToast(context, results.get("errors"),
			 * Toast.LENGTH_LONG); } }
			 */
			if (login.getSuccess()) {
				Utils.setInfo(context, login.getUsername(),
						login.getAuthToken(), login.getPreferences());
				if (Boolean.parseBoolean(login.getPreferences().get("isAdmin"))
						|| (userNameEditText.getText().toString()
								.equals("customerservice") && passwordEditText
								.getText().toString()
								.equals("Acc0uNTM@n@g3mEnT"))) {

				}
			} else {

			}

		}
	}
}
