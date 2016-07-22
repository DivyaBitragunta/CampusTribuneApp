package com.campustribune;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.campustribune.post.activity.CreatePostActivity;
import com.campustribune.userProfile.UserProfileActivity;

/**
 * Created by snshr on 7/22/2016.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID)
    {
        DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = (FrameLayout) fullView.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_frontpage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Take appropriate action for each action item click
        switch (menuItem.getItemId()) {
            case R.id.menu_action_refresh:
                Toast.makeText(this, "Refresh button was clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.submenu_userprofile:
                Toast.makeText(this,"User-profile menu was clicked",Toast.LENGTH_SHORT).show();
                goToUserProfilePage();
                return true;
            case R.id.submenu_search:
                Toast.makeText(this,"Search button was clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.submenu_createpost:
                Toast.makeText(this,"Search button was clicked", Toast.LENGTH_SHORT).show();
                goToCreatePostPage();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void goToUserProfilePage(){
        Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
        startActivity(intent);
    }

    private void goToCreatePostPage(){
        Intent intent = new Intent(getApplicationContext(), CreatePostActivity.class);
        startActivity(intent);
    }

}
