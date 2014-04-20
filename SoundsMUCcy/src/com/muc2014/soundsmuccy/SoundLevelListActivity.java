package com.muc2014.soundsmuccy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

public class SoundLevelListActivity extends FragmentActivity
        implements SoundLevelListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundlevel_list);

        if (findViewById(R.id.soundlevel_detail_container) != null) {
            mTwoPane = true;
            ((SoundLevelListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.soundlevel_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public void onItemSelected(String id) {
    	
//    	Context context = getApplicationContext();
//    	CharSequence text = "(empty)";
//    	int duration = Toast.LENGTH_SHORT;

    	//always goes to "else" branch?? possibly somethign to o with wehter there is a pre-existing version of that view?
        if (mTwoPane) {
//        	text = "TOP Subsequent!";
            Bundle arguments = new Bundle();
            arguments.putString(SoundLevelDetailFragment.ARG_ITEM_ID, id);
            SoundLevelDetailFragment fragment = new SoundLevelDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.soundlevel_detail_container, fragment)
                    .commit();

        } else {
        	
        	if (id.equalsIgnoreCase("1")) {

//            	Toast toast = Toast.makeText(context, "Please wait whilst sound level is taken.", Toast.LENGTH_LONG);
//            	toast.show();
            	
//            	text = "TOP FIRST id 1!";
            	
	            Intent detailIntent = new Intent(this, SoundLevelDetailActivity.class);
	            
	            SoundLevelDetailActivity.DETAIL_ARG_ITEM_ID = id;
	            
	            //commented as don't yet know which fragment to start...
	            detailIntent.putExtra(SoundLevelDetailFragmentTH.ARG_ITEM_ID, id);
	            startActivity(detailIntent);

        	} else {
//            	text = "TOPFIRST id NOT 1!";
	            Intent detailIntent = new Intent(this, SoundLevelDetailActivity.class);
	            
	            SoundLevelDetailActivity.DETAIL_ARG_ITEM_ID = id;
	            
	            detailIntent.putExtra(SoundLevelDetailFragment.ARG_ITEM_ID, id);
	            startActivity(detailIntent);
        	}
        }

//    	Toast toast = Toast.makeText(context, text, duration);
//    	toast.show();
    }
}
