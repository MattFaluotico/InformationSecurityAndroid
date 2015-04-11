package claudiusmbemba.com.strangerdanger;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    class NavItem {
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }

    private static String TAG = MainActivity.class.getSimpleName();

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private Fragment fragment = null;

    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();

    //camera intents
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getPackageName());
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create storage directory.");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss", Locale.US).format(new java.util.Date());
        return new File(directory.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = null;
                if (data == null) {
                    // A known bug here! The image should have saved in fileUri
                    Toast.makeText(this, "Image saved successfully",
                            Toast.LENGTH_LONG).show();
//                    photoUri = fileUri;
                } else {
//                    photoUri = data.getData();
                    Toast.makeText(this, "Image saved successfully in: " + data.getData(),
                            Toast.LENGTH_LONG).show();
                }
                //showPhoto(photoUri);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Callout for image capture failed!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

//    private void showPhoto(Uri photoUri) {
//        File imageFile = new File(photoUri);
//        if (imageFile.exists()){
//            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//            BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bitmap);
//            photoImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
//            photoImage.setImageDrawable(drawable);
//        }
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // More info: http://codetheory.in/difference-between-setdisplayhomeasupenabled-sethomebuttonenabled-and-setdisplayshowhomeenabled/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavItems.add(new NavItem("Home", "Notify ICEs", R.drawable.home));
        mNavItems.add(new NavItem("Edit ICE Contacts", "Edit your ICE Contacts", R.drawable.contact));
        mNavItems.add(new NavItem("Camera", "Take photo evidence", R.drawable.camera));
        mNavItems.add(new NavItem("Preferences", "Change your preferences", R.drawable.settings));
        mNavItems.add(new NavItem("About", "About this app", R.drawable.menu));
        mNavItems.add(new NavItem("Logout", "Sign out", R.drawable.logout));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);

        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: " + getTitle());

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //set home fragment
        Fragment fragment = new HomeFragment();
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.mainContent, fragment)
                .commit();
        setTitle("Home");

    }

    /*
* Called when a particular item from the navigation drawer
* is selected.
* */
    private void selectItemFromDrawer(int position) {

        NavItem item = mNavItems.get(position);

        FragmentManager fragmentManager = getFragmentManager();

        switch (item.mTitle) {
            case "Home":
                fragment = new HomeFragment();
                break;
            case "About":
                fragment = new AboutFragment();
                break;
            case "Preferences":
                fragment = new PreferencesFragment();
                break;
            case "Police Map":
                fragment = new PoliceMapFragment();
                break;
            case "Camera": //intent call to camera
                //camera: create Intent to take a picture and return control to the calling application
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = Uri.fromFile(getOutputPhotoFile());// create a file to save the image
                // fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
                // start the image capture Intent
                startActivityForResult(camera_intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                break;
            case "Edit ICE Contacts":
                fragment = new AddContactFragment();
                break;
            case "Logout":
                Intent logout_intent = new Intent(Intent.ACTION_MAIN);
                logout_intent.addCategory(Intent.CATEGORY_HOME);
                logout_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(logout_intent);
                break;
            default:

        }

        if(fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.mainContent, fragment)
                    .commit();
            mDrawerList.setItemChecked(position, true);
            setTitle(mNavItems.get(position).mTitle);
        }

        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }


    // Called when invalidateOptionsMenu() is invoked
    public boolean onPrepareOptionsMenu(Menu menu) {
// If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerPane);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...
        //noinspection SimplifiableIfStatement
        int id = item.getItemId();

//        Menu menu = (Menu) item.getMenuInfo();
        if (id == R.id.action_settings) {
            return true;
//            boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerPane);
//            menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
//            return super.onPrepareOptionsMenu(menu);
        }

        return super.onOptionsItemSelected(item);
    }

}
