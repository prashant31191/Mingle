package com.mingle;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class MingleApplication extends Application {

	static final String TAG = "Mingle";
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "rVrDM19UXGinyG0y2cvlYVCIRBZRc48NtJ1jDZYD", "N6QHE90jOsSZ1D1iRUtXcKcz2HSYkHkmrwCcxHJv");
		ParseFacebookUtils.initialize(getString(R.string.app_id));
	}
}
