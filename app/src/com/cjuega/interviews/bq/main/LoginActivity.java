package com.cjuega.interviews.bq.main;

import com.cjuega.interviews.bq.R;
import com.cjuega.interviews.dropbox.DropboxManager;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

/**
 * 
 * @author cjuega
 * 
 * Dummy Activity that calls directly the Dropbox login method. It redirects to {@code LibraryActivity} if 
 * login process succeed or call {@code finish()} otherwise.
 *  
 * This Activity would be useful whenever we add another file provider to the library, i.e. Google Drive. If so,
 * we could add different buttons to log in with different providers. 
 * 
 */
public class LoginActivity extends Activity {
	
	static final int REQUEST_LINK_TO_DBX = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		DropboxManager dropboxManager = DropboxManager.getInstance();
		
		if (dropboxManager.isLoggedin()){
			startLibrary();
		} else {
			dropboxManager.dropboxLogin(this, REQUEST_LINK_TO_DBX);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_LINK_TO_DBX) {
	        if (resultCode == Activity.RESULT_OK) {
	        	startLibrary();
	        } else {
	            finish();
	        }
	    } else {
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	private void startLibrary(){
		Intent intent = new Intent(this, LibraryActivity.class);
		startActivity(intent);
	}
}
