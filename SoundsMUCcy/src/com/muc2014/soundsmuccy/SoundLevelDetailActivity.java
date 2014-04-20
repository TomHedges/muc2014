package com.muc2014.soundsmuccy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

public class SoundLevelDetailActivity extends FragmentActivity {

    public static String DETAIL_ARG_ITEM_ID = "item_id_TH";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
//    	Context context = getApplicationContext();
//    	String text = "(empty)";
//    	int duration = Toast.LENGTH_SHORT;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundlevel_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
//        	text = DETAIL_ARG_ITEM_ID;
        	
            Bundle arguments = new Bundle();

        	if (DETAIL_ARG_ITEM_ID.equalsIgnoreCase("1")) {
                arguments.putString(SoundLevelDetailFragmentTH.ARG_ITEM_ID,
                        getIntent().getStringExtra(SoundLevelDetailFragmentTH.ARG_ITEM_ID));
                
                SoundLevelDetailFragmentTH fragment = new SoundLevelDetailFragmentTH();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.soundlevel_detail_container, fragment)
                        .commit();
        		
        	} else{
                arguments.putString(SoundLevelDetailFragment.ARG_ITEM_ID,
                        getIntent().getStringExtra(SoundLevelDetailFragment.ARG_ITEM_ID));
                SoundLevelDetailFragment fragment = new SoundLevelDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.soundlevel_detail_container, fragment)
                        .commit();
        	}

        }
        
//    	Toast toast = Toast.makeText(context, text, duration);
//    	toast.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, SoundLevelListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
