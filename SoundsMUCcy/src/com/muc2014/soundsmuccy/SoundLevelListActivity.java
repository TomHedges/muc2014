package com.muc2014.soundsmuccy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

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
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(SoundLevelDetailFragment.ARG_ITEM_ID, id);
            SoundLevelDetailFragment fragment = new SoundLevelDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.soundlevel_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, SoundLevelDetailActivity.class);
            detailIntent.putExtra(SoundLevelDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
