package com.mingle;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.mingle.R;
import com.oovoo.core.ConferenceCore;

import android.view.SurfaceView;
import android.content.Context;
import android.util.Log;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.ConferenceCore.MediaDevice;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.IConferenceCore.ConnectionStatistics;
import com.oovoo.core.IConferenceCoreListener;
import com.oovoo.core.Exceptions.NullApplicationContext;
public  class LocationActivity extends Activity implements IConferenceCoreListener{
	private ParseUser mUser;
	private ParseObject mConference;
	private ParseQuery<ParseObject> matchQuery;
	ConferenceCoreError errorCode; 
    

    
    

    protected void onCreate(Bundle savedInstanceState) {
    	
    super.onCreate( savedInstanceState);
    setContentView(R.layout.video_call);
    mUser = ParseUser.getCurrentUser();
		if(mUser != null) {
			findConference();
		}
	}
	private void findConference() {

		matchQuery = ParseQuery.getQuery("Conference");
		matchQuery.whereEqualTo("status", "waiting");
		matchQuery.whereNotEqualTo("user1", mUser);
		matchQuery.countInBackground(new CountCallback() {
			@Override
			public void done(int count, ParseException e) {
				if (count > 0) {
					int row = (int) Math.floor(Math.random()*count);
					matchQuery.setLimit(1);
					matchQuery.setSkip(row);
					matchQuery.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> confs, ParseException e) {
							if (confs.size() > 0) {
								lockConference();
							} else {
								createConference();
							}
						}
					});
				} else {
					createConference();
				}
			}
			
		});
	}
	
	private void lockConference() {
		mConference.increment("lock");
		mConference.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if(mConference.getInt("lock") <= 2) {
					mConference.put("user2", mUser);
					mConference.put("status", "mingling");
					mConference.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e) {
							try {
								startConference();
							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					});
				} else {
					// could not join, we're just going to create a conference instead
					createConference();
				}
			}
		});
	}
	
	private void createConference() {
		mConference = new ParseObject("Conference");
		mConference.put("lock", 1);
		mConference.put("user1", mUser);
		mConference.put("status", "waiting");
		mConference.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				try {
					startConference();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
	}
	
	private void startConference() throws Exception {
		
    	Context mApp = null;
    	
    	/* Initiating the conference */ 
    	mApp = getApplicationContext();
    	IConferenceCore mConferenceCore = null;

		String conferenceID = mConference.getObjectId();
		String firstName = null;
		JSONObject profile  = mUser.getJSONObject("profile");
		try {
			if (profile.getString("firstName") != null) {
				firstName = profile.getString("firstName");
			}

			/* Start the video conference using the object ID and first name 
			 * this is where Steven would take over I guess*/
			
			 
			 /* Maybe should have placed my code HERE?*. 
			
			  */

		} catch (JSONException e) {
			e.printStackTrace();
		
		}
		
		

		mConferenceCore = ConferenceCore.instance(mApp);
        // Authenticating for use 
		mConferenceCore.initSdk("12349983350851",
        		"MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB%2FOj%2Byd3iuFj%2BwgOGGjP1rL%2FTdnwapuUxDfjpUrxVfM%2Fp72b4x1RDU%2FElQz3Q3dVtD%2FnwJW1ZpKNB1ggkWbDrdeD%2F%2B%2FYeCcWyLgZ13k5kEE0zDXPHlrsMV3eRKfwA6FOM%3D",
        		"https://api-sdk.dev.oovoo.com/");
        mConferenceCore.setContext(mApp);
		mConferenceCore.setListener((IConferenceCoreListener) this);
        SurfaceView view = (SurfaceView)findViewById(R.id.sView);
        
        /*Joining a conference */ 
     if( mConferenceCore.joinConference(conferenceID,
        		firstName,null) == ConferenceCoreError.OK)
        		{
        			try {
        				mConferenceCore.setPreviewSurface(view);
        				mConferenceCore.turnMyVideoOn(); 

        				mConferenceCore.turnMicrophoneOn();
        				/* Need to figure out a way to use the OnParticipentJoinedConference method
        				 * to grab the participent ID and place it here instead of Kirk */ 
        				
        				/* Might be a couple other things i need to do to Recieve the participents video call */ 
        				 
        				mConferenceCore.receiveParticipantVideoOn("Kirk");
					}
        			
        			catch (NullApplicationContext e) {
    					// TODO Auto-generated catch block
    					System.out.println("Error detected!");
    					e.printStackTrace();
    				} 
        		}
     else 
Log.d(MingleApplication.TAG,
							"Problem Joining Conference");        
    }
  

    
    /**
     * A placeholder fragment containing a simple view.
     */
   

        

	public void OnCameraSelected(ConferenceCoreError arg0, String arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}

	public void OnConferenceError(ConferenceCoreError arg0) {
		// TODO Auto-generated method stub
		
	}

	public void OnConnectionStatisticsUpdate(ConnectionStatistics arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnGetMediaDeviceList(MediaDevice[] arg0) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnIncallMessage(byte[] arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void OnJoinConference(ConferenceCoreError arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void OnLeftConference(ConferenceCoreError arg0) {
		// TODO Auto-generated method stub
		
	}

	public void OnMicrophoneSelected(ConferenceCoreError arg0, String arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}

	public void OnMicrophoneTurnedOff(ConferenceCoreError arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void OnMicrophoneTurnedOn(ConferenceCoreError arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void OnMyVideoTurnedOff(ConferenceCoreError arg0) {
		// TODO Auto-generated method stub
		
	}

	public void OnMyVideoTurnedOn(ConferenceCoreError arg0, FrameSize arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnParticipantJoinedConference(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnParticipantLeftConference(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void OnParticipantVideoPaused(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void OnParticipantVideoReceiveOff(ConferenceCoreError arg0,
			String arg1) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnParticipantVideoReceiveOn(ConferenceCoreError arg0,
			String arg1, FrameSize arg2) {
		// TODO Auto-generated method stub
		
	}

	public void OnParticipantVideoResumed(String arg0) {
		// TODO Auto-generated method stub
		
	}

	public void OnSpeakerSelected(ConferenceCoreError arg0, String arg1,
			String arg2) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnSpeakerTurnedOff(ConferenceCoreError arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	
	public void OnSpeakerTurnedOn(ConferenceCoreError arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

	
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

}
