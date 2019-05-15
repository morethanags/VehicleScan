package pe.huntlng.vehiclescan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.TabLayout;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        HandheldFragment.OnHandheldFragmentInteractionListener,
        EntranceFragment.OnEntranceFragmentInteractionListener,
        ExitFragment.OnExitFragmentInteractionListener {

    private static long back_pressed;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //View view = findViewById(android.R.id.content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //si viene de la notificacion...
        /*if(getIntent().getExtras()!=null && getIntent().getExtras().getString("plate")!= null){
            editText_Plate.setText(getIntent().getExtras().getString("plate"));
            sendRequest();
        }*/
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("requestCode", requestCode+"");
        Log.d("resultCode", resultCode+"");
        if (requestCode == HandheldFragment.SCAN_REQUEST) {
            if (resultCode == RESULT_OK) {
                String returnedResult = data.getStringExtra("result");
                Log.d("MainActivity", returnedResult);
                mSectionsPagerAdapter.handheldFragment.setPlate(returnedResult);
            }
        }
    }

   /* @Override
    protected void onNewIntent(Intent intent) {
        Log.d("MainActivity Intent", intent.getAction());
        if(intent.getExtras().getString("plate")!= null){
            editText_Plate.setText(intent.getExtras().getString("plate"));
            sendRequest();
        }
    }*/

    @Override
    public void onHandheldFragmentInteraction() {

    }

    @Override
    public void onEntranceFragmentInteraction() {

    }

    @Override
    public void onExitFragmentInteraction() {

    }


    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis())
            super.onBackPressed();
        else
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private HandheldFragment handheldFragment;
        private EntranceFragment entranceFragment;
        private ExitFragment exitFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                if (handheldFragment == null) {
                    handheldFragment = new HandheldFragment();
                }
                fragment = handheldFragment;
            } else if (position == 1) {
                if (entranceFragment == null) {
                    entranceFragment = new EntranceFragment();
                }
                fragment = entranceFragment;
            } else if (position == 2) {
                if (exitFragment == null) {
                    exitFragment = new ExitFragment();
                }
                fragment = exitFragment;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Handheld";
                case 1:
                    return "Entrance";
                case 2:
                    return "Exit";
            }
            return null;
        }
    }

}
