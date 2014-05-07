package com.mingle;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;

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
import android.view.View;
import android.widget.LinearLayout;

import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.ConferenceCore.MediaDevice;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.IConferenceCore.ConnectionStatistics;
import com.oovoo.core.IConferenceCoreListener;
import com.oovoo.core.ClientCore.VideoChannelPtr;
import com.oovoo.core.Exceptions.NullApplicationContext;
import com.oovoo.core.ui.VideoRenderer;

public  class LocationActivity extends Activity implements IConferenceCoreListener{
	
	private ParseUser mUser;
	private ParseObject mConference;
	private ParseQuery<ParseObject> matchQuery;
	ConferenceCoreError errorCode;
	/* Global Variable for firstName2, recieved by listener method participent on joined */ 
	public String firstName2; 
	
    protected void onCreate(Bundle savedInstanceState) {	
    	super.onCreate(savedInstanceState);
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
								mConference = confs.get(0);
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
					e1.printStackTrace();
				}
			}
		});
	}
	

	
	
	private void startConference() {		
    	
    	/* Initiating the conference */ 
    	
		String conferenceID = mConference.getObjectId();
		String firstName = null;
		JSONObject profile  = mUser.getJSONObject("profile");
		try {
			if (profile.getString("firstName") != null) {
				firstName = profile.getString("firstName");
			}		
		} catch (JSONException e) {
			e.printStackTrace();		
		}
		
		IConferenceCore mConferenceCore = ConferenceCore.instance(this);
        // Authenticating for use 
		mConferenceCore.initSdk("12349983350851",
        		"MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB%2FOj%2Byd3iuFj%2BwgOGGjP1rL%2FTdnwapuUxDfjpUrxVfM%2Fp72b4x1RDU%2FElQz3Q3dVtD%2FnwJW1ZpKNB1ggkWbDrdeD%2F%2B%2FYeCcWyLgZ13k5kEE0zDXPHlrsMV3eRKfwA6FOM%3D",
        		"https://api-sdk.dev.oovoo.com/");
        try {
        	/* Kirk has to change it to this for it to work
        	 * 			mConferenceCore.setContext(this);

        	 */
			mConferenceCore.setContext(getApplicationContext());
		} catch (NullApplicationContext e1) {
			e1.printStackTrace();
		}
        /* for kirk
			mConferenceCore.setListener((IConferenceCoreListener) this);
		*/ 
        
		/* for Steven */ 
		mConferenceCore.setListener(this);

		
        SurfaceView view = (SurfaceView) findViewById(R.id.sView);
	    GLSurfaceView glView = (GLSurfaceView) findViewById(R.id.userTwoVideoView); 

        /*Joining a conference */ 
        if( mConferenceCore.joinConference(conferenceID,
        	firstName,null) == ConferenceCoreError.OK) {
        		try {
        			mConferenceCore.setPreviewSurface(view);
        			mConferenceCore.turnMyVideoOn(); 

        			mConferenceCore.turnMicrophoneOn();
        			/* Need to figure out a way to use the OnParticipentJoinedConference method
        			 * to grab the participant ID and place it here instead of Kirk */ 
        				
        			/* Might be a couple other things i need to do to Receive the participants video call */ 
        				 

        			/*Setting up the glView, grabbing the remote particpents video */ 
        			
        	        VideoRenderer mRenderer = new com.oovoo.core.ui.VideoRenderer(glView);
        	        glView.setEGLContextClientVersion(2);
        	        glView.setRenderer(mRenderer);
        	        //l.addView(glView,0);

        	        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        			mConferenceCore.receiveParticipantVideoOn(firstName2);

        	        VideoChannelPtr in = mConferenceCore.getVideoChannelForUser(firstName2);
                	Log.d(MingleApplication.TAG, "IN CLASS, pid =  "+firstName2);        
                	
        	        mRenderer.connect(in, firstName2);
        		    glView.setVisibility(View.VISIBLE);

				} catch (NullApplicationContext e) {
    				System.out.println("Error detected!");
    				e.printStackTrace();
    			} catch (Exception e) {
					e.printStackTrace();
				} 
        	}
        else {
        	Log.d(MingleApplication.TAG, "Problem Joining Conference");        
        }
     }
  
    /**
     * A placeholder fragment containing a simple view.
     */
	
	public void OnCameraSelected(ConferenceCoreError arg0, String arg1,
			String arg2) {
		
	}

	public void OnConferenceError(ConferenceCoreError arg0) {
		
	}

	public void OnConnectionStatisticsUpdate(ConnectionStatistics arg0) {
		
	}

	public void OnGetMediaDeviceList(MediaDevice[] arg0) {
			
	}
	
	public void OnIncallMessage(byte[] arg0, String arg1) {
		
	}

	public void OnJoinConference(ConferenceCoreError arg0, String arg1) {
		
	}

	public void OnLeftConference(ConferenceCoreError arg0) {
		
	}

	public void OnMicrophoneSelected(ConferenceCoreError arg0, String arg1,
			String arg2) {
		
	}

	public void OnMicrophoneTurnedOff(ConferenceCoreError arg0, String arg1) {
		
	}

	public void OnMicrophoneTurnedOn(ConferenceCoreError arg0, String arg1) {
		
	}

	public void OnMyVideoTurnedOff(ConferenceCoreError arg0) {
			
	}

	public void OnMyVideoTurnedOn(ConferenceCoreError arg0, FrameSize arg1,
			int arg2) {
			
	}

	public void OnParticipantJoinedConference(String arg0, String arg1) {
		 /* When a participent joins the conference we set the global firstName2 = to their particpent ID */ 
		 firstName2 = arg0; 

    	Log.d(MingleApplication.TAG, " PARTICIPENT ID = "+ arg0);        

	}

	
	public void OnParticipantLeftConference(String arg0) {
		
	}

	public void OnParticipantVideoPaused(String arg0) {
		
	}

	public void OnParticipantVideoReceiveOff(ConferenceCoreError arg0,
			String arg1) {
		
	}

	public void OnParticipantVideoReceiveOn(ConferenceCoreError arg0,
			String arg1, FrameSize arg2) {
		
	}

	public void OnParticipantVideoResumed(String arg0) {
		
	}

	public void OnSpeakerSelected(ConferenceCoreError arg0, String arg1,
			String arg2) {
		
	}
	
	public void OnSpeakerTurnedOff(ConferenceCoreError arg0, String arg1) {
			
	}
	
	public void OnSpeakerTurnedOn(ConferenceCoreError arg0, String arg1) {
		
	}

	public void onPause() {
		super.onPause();
	}

	
	public void onResume() {
		super.onResume();
	}

}
