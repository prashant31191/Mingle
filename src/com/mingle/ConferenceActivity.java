package com.mingle;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.oovoo.core.ConferenceCore;
import com.oovoo.core.ConferenceCore.FrameSize;
import com.oovoo.core.ConferenceCore.MediaDevice;
import com.oovoo.core.IConferenceCore;
import com.oovoo.core.IConferenceCore.ConferenceCoreError;
import com.oovoo.core.IConferenceCore.ConnectionStatistics;
import com.oovoo.core.IConferenceCoreListener;
import com.oovoo.core.ClientCore.VideoChannelPtr;
import com.oovoo.core.Exceptions.NullApplicationContext;
import com.oovoo.core.ui.VideoRenderer;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ConferenceActivity extends Activity implements IConferenceCoreListener {

	private final String TAG = "MINGLE_CONFERENCE_MANAGER";
	
	private ParseUser mUser;
	private ParseObject mConference;
	private ParseQuery<ParseObject> mQuery;
	
	private VideoRenderer mRenderer;
	private IConferenceCore mConferenceCore;
	
	private SurfaceView sView;
	private GLSurfaceView glView;
	private String participantId;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_conference);
	
		sView = (SurfaceView) findViewById(R.id.mVideo);
		glView = (GLSurfaceView) findViewById(R.id.uVideo);
						
		((Button) findViewById(R.id.leaveButton))
			.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onLeaveButtonClicked();
				}
			});
		
		sView.setVisibility(View.INVISIBLE);
		glView.setVisibility(View.INVISIBLE);
		
		mConferenceCore = ConferenceCore.instance(this);
		mConferenceCore.initSdk("12349983351091",
				"MDAxMDAxAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACAye86PQ2KfCfJmHonCqsMY2YIqMhypD92ZB7iHRMjJ7dQsS8VZm7JOwxPiDSNYMkgR8XPcK0sHWSF6xH12LBS4EiHqWB5OiFxtnL4ZWFNN4KqfxlIQMTW%2FcR%2FdnusGwk%3D",
				"https://api-sdk.dev.oovoo.com/");
		try {
			mConferenceCore.setContext(this);
		} catch (NullApplicationContext e) {
			
		}
		mConferenceCore.setListener(this);
		
		mRenderer = new VideoRenderer(glView);
		glView.setEGLContextClientVersion(2);
		glView.setRenderer(mRenderer);
		glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		mUser = ParseUser.getCurrentUser();
		if(mUser != null) {
			findConference();
		}
	}	
	
	private void onLeaveButtonClicked() {
		mConferenceCore.leaveConference(ConferenceCoreError.OK);

		
	}

	private void findConference() {
		mQuery = ParseQuery.getQuery("Conference");
		mQuery.whereEqualTo("status", "waiting");
		mQuery.whereNotEqualTo("user1", mUser);
		mQuery.countInBackground(new CountCallback() {
			@Override
			public void done(int count, ParseException e) {
				if (count > 0) {
					int row = (int) Math.floor(Math.random()*count);
					mQuery.setLimit(1);
					mQuery.setSkip(row);
					mQuery.findInBackground(new FindCallback<ParseObject>() {
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
		String firstName = null;
		String conferenceID = mConference.getObjectId();
		JSONObject profile  = mUser.getJSONObject("profile");
		try {
			if (profile.getString("firstName") != null) {
				firstName = profile.getString("firstName");
			}		
		} catch (JSONException e) {
			e.printStackTrace();		
		}    
		
	    mConferenceCore.joinConference(conferenceID,firstName,null);
	}
	
	
	
	
	
	@Override
	public void OnJoinConference(ConferenceCoreError eCode, String mPId) {
		
		if (eCode == ConferenceCoreError.OK) {
			try {
				ConferenceCore.instance().turnMyVideoOn();
				ConferenceCore.instance().turnMicrophoneOn();
				ConferenceCore.instance().turnSpeakerOn();

				ConferenceCore.instance().setPreviewSurface(sView);
			} catch (Exception e1) {
				Log.d(TAG, "video not turned on");
			}
		}
	}
	
	@Override
	public void OnMyVideoTurnedOn(ConferenceCoreError arg0, FrameSize arg1, int arg2) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				sView.setVisibility(View.VISIBLE);
			}
		});
	}
		
	@Override
	public void OnParticipantJoinedConference(String pId, String pInfo) {
		// Handle the participant
		ConferenceCore.instance().receiveParticipantVideoOn(pId);
	}
	
	@Override
	public void OnParticipantVideoReceiveOn(ConferenceCoreError eCode, String pId, FrameSize fSize) {
		participantId = pId;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				VideoChannelPtr in = mConferenceCore.getVideoChannelForUser(participantId);
				mRenderer.connect(in, participantId);
				glView.setVisibility(View.VISIBLE);	
			}
		});
	}
	
	@Override
	public void OnCameraSelected(ConferenceCoreError eCode, String arg1, String arg2) {
		
	}

	@Override
	public void OnConferenceError(ConferenceCoreError eCode) {
		
	}

	@Override
	public void OnConnectionStatisticsUpdate(ConnectionStatistics arg0) {
		
	}

	@Override
	public void OnGetMediaDeviceList(MediaDevice[] arg0) {
		
	}

	@Override
	public void OnIncallMessage(byte[] arg0, String arg1) {
		
	}

	@Override
	public void OnLeftConference(ConferenceCoreError arg0) {
		/* after we leave the conference, we should find a new one */ 
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					mConference.delete();
				} catch (ParseException e) {
					/* doesn't exist, or no Internet */
				}
				glView.setVisibility(View.INVISIBLE);
				findConference();
			}
		});
	}
	
	@Override
	public void OnMicrophoneSelected(ConferenceCoreError arg0, String arg1, String arg2) {
		
	}

	@Override
	public void OnMicrophoneTurnedOff(ConferenceCoreError arg0, String arg1) {
		
	}

	@Override
	public void OnMicrophoneTurnedOn(ConferenceCoreError arg0, String arg1) {
	
	}

	@Override
	public void OnMyVideoTurnedOff(ConferenceCoreError arg0) {
		
	}

	@Override
	public void OnParticipantLeftConference(String arg0) {
		/* If the other person leaves, we want to leave as well */ 
		mConferenceCore.leaveConference(ConferenceCoreError.OK);	
	}

	@Override
	public void OnParticipantVideoPaused(String arg0) {
		
	}

	@Override
	public void OnParticipantVideoReceiveOff(ConferenceCoreError arg0, String arg1) {
		
	}

	@Override
	public void OnParticipantVideoResumed(String arg0) {
			
	}

	@Override
	public void OnSpeakerSelected(ConferenceCoreError arg0, String arg1, String arg2) {
		
	}

	@Override
	public void OnSpeakerTurnedOff(ConferenceCoreError arg0, String arg1) {
		
	}

	@Override
	public void OnSpeakerTurnedOn(ConferenceCoreError arg0, String arg1) {
		
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {		
		super.onResume();
	}
}
