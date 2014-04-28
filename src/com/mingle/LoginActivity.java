package com.mingle;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	private Dialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		/* setting up the login button, giving it an onClickListener */
		((Button) findViewById(R.id.loginButton))
			.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoginButtonClicked();
			}
		});
		
		/* Checks to see if the user is already linked with Facebook */
		ParseUser currentUser = ParseUser.getCurrentUser();
		if((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			startMainActivity();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}
	
	private void onLoginButtonClicked() {
		/* Just showing a progress dialog, really just for the user to make it look like
		 * the app is doing something */
		LoginActivity.this.progressDialog = ProgressDialog.show(
				LoginActivity.this, "", "Logging in...", true);
		
		/* Asks them for permissions. we only want basic info, birthday, and location */
		List<String> permissions = Arrays.asList("basic_info", "user_birthday", "user_location");
		
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
			@Override
			public void done(ParseUser user, ParseException e) {
				LoginActivity.this.progressDialog.dismiss();
				/* Handling the various errors that could happen */
				if (user == null) {
					Log.d(MingleApplication.TAG,
							"Uh oh. The user cancelled the Facebook login.");
				} else if(user.isNew()) {
					Log.d(MingleApplication.TAG,
							"User signed up and logged in through Facebook!");
					startMainActivity();
				} else {
					Log.d(MingleApplication.TAG,
							"User logged in through Facebook!");
					startMainActivity();
				}
			}
		});
	}
	
	/* Starting the next activity after successful login. Goes to MainActivity */
	private void startMainActivity() {
		startActivity(new Intent(this, MainActivity.class));
	}
}