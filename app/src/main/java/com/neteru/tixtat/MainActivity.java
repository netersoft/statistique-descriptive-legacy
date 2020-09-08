package com.neteru.tixtat;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.neteru.tixtat.classes.utilities.ViewPagerAdapter;
import com.neteru.tixtat.fragments.BackupsFragment;
import com.neteru.tixtat.fragments.ContinuousVariablesFragment;
import com.neteru.tixtat.fragments.DiscreteVariablesFragment;
import com.neteru.tixtat.fragments.DocumentationFragment;
import com.neteru.tixtat.fragments.QualitativeVariablesFragment;
import com.neteru.tixtat.fragments.TutorialFragment;
import com.vorlonsoft.android.rate.AppRate;
import com.vorlonsoft.android.rate.StoreType;
import com.vorlonsoft.android.rate.Time;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Barre de sélection de fragment
        TabLayout tabLayout = findViewById(R.id.tabs);

        viewPager = findViewById(R.id.viewpager);

        // Volet de navigation latérale
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Vue de navigation latérale
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        setViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        final FloatingActionButton fab = findViewById(R.id.fab_1);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            /**
             * A la sélèction d'une page, on désactive tous les items
             * puis on sélectionne les items correspondants
             * et on gère la visibilité du bouton flottant
             * @param position / position du sélecteur
             */
            @Override
            public void onPageSelected(int position) {
                uncheckedAllItem();
                switch (position){
                    case 0:
                        navigationView.getMenu().getItem(0).setChecked(true);
                        fab.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        navigationView.getMenu().getItem(1).getSubMenu().getItem(0).setChecked(true);
                        fab.setVisibility(View.GONE);
                        break;

                    case 2:
                        navigationView.getMenu().getItem(1).getSubMenu().getItem(1).setChecked(true);
                        fab.setVisibility(View.GONE);
                        break;

                    case 3:
                        navigationView.getMenu().getItem(2).setChecked(true);
                        fab.setVisibility(View.GONE);
                        break;

                    case 4:
                        navigationView.getMenu().getItem(3).setChecked(true);
                        fab.setVisibility(View.GONE);
                        break;

                    case 5:
                        navigationView.getMenu().getItem(4).setChecked(true);
                        fab.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setNavMenuItemThemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        setAppRateSystem();
    }

    /**
     * Initialisation du système de notation
     */
    private void setAppRateSystem() {

        AppRate.with(this)
                .setStoreType(StoreType.GOOGLEPLAY)
                .setTimeToWait(Time.DAY, (short) 10) // default is 10 days, 0 means install millisecond, 10 means app is launched 10 or more time units later than installation
                .setLaunchTimes((byte) 10)          // default is 10, 3 means app is launched 3 or more times
                .setRemindTimeToWait(Time.DAY, (short) 1) // default is 1 day, 1 means app is launched 1 or more time units after neutral button clicked
                .setRemindLaunchesNumber((byte) 0)  // default is 0, 1 means app is launched 1 or more times after neutral button clicked
                .setSelectedAppLaunches((byte) 1)   // default is 1, 1 means each launch, 2 means every 2nd launch, 3 means every 3rd launch, etc
                .setShowLaterButton(true)           // default is true, true means to show the Neutral button ("Remind me later").
                .setVersionCodeCheck(false)          // default is false, true means to re-enable the Rate Dialog if a new version of app with different version code is installed
                .setVersionNameCheck(false)          // default is false, true means to re-enable the Rate Dialog if a new version of app with different version name is installed
                .setDebug(false)                    // default is false, true is for development only, true ensures that the Rate Dialog will be shown each time the app is launched
                .setOnClickButtonListener(which -> Log.d(MainActivity.this.getLocalClassName(), Byte.toString(which)))
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);

    }

    /**
     * Initialisation du viewPager
     * @param viewPager / Objet viewpager
     */
    protected void setViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DocumentationFragment(), getString(R.string.documentation_str));
        adapter.addFragment(new DiscreteVariablesFragment(), getString(R.string.var_d_str));
        adapter.addFragment(new ContinuousVariablesFragment(), getString(R.string.var_c_str));
        adapter.addFragment(new QualitativeVariablesFragment(), getString(R.string.var_qlt));
        adapter.addFragment(new BackupsFragment(), getString(R.string.safeguards));
        adapter.addFragment(new TutorialFragment(), getString(R.string.tutorial));

        viewPager.setAdapter(adapter);
    }

    /**
     * Customisation de la couleur des menus de la barre latérale en état d'activité
     * @param color / couleur
     */
    public void setNavMenuItemThemeColors(int color){
        //Setting default colors for menu item Text and Icon
        int navDefaultTextColor = ContextCompat.getColor(this, R.color.colorPrimary);
        int navDefaultIconColor = ContextCompat.getColor(this, R.color.colorPrimary);

        //Defining ColorStateList for menu item Text
        ColorStateList navMenuTextList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed}
                },
                new int[] {
                        color,
                        navDefaultTextColor,
                        navDefaultTextColor,
                        navDefaultTextColor,
                        navDefaultTextColor
                }
        );

        //Defining ColorStateList for menu item Icon
        ColorStateList navMenuIconList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_pressed}
                },
                new int[] {
                        color,
                        navDefaultIconColor,
                        navDefaultIconColor,
                        navDefaultIconColor,
                        navDefaultIconColor
                }
        );

        navigationView.setItemTextColor(navMenuTextList);
        navigationView.setItemIconTintList(navMenuIconList);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Gestion de click sur les items de la vue de navigation
        final int itemId = item.getItemId();

        new Handler().postDelayed(() -> {

            switch (itemId){
                case R.id.nav_documentation:
                    viewPager.setCurrentItem(0);
                    break;

                case R.id.nav_discretes:
                    viewPager.setCurrentItem(1);
                    break;

                case R.id.nav_continues:
                    viewPager.setCurrentItem(2);
                    break;

                case R.id.nav_qualitatives:
                    viewPager.setCurrentItem(3);
                    break;

                case R.id.nav_safeguards:
                    viewPager.setCurrentItem(4);
                    break;

                case R.id.nav_tutorial:
                    viewPager.setCurrentItem(5);
                    break;

                case R.id.nav_param:
                    startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class),0);
                    overridePendingTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity);
                    break;

                case R.id.nav_about:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("\n");

                    LayoutInflater factory = LayoutInflater.from(MainActivity.this);

                    @SuppressLint("InflateParams")
                    View aboutView = factory.inflate(R.layout.about_view, null);

                    TextView appName = aboutView.findViewById(R.id.AboutTxtView_1);
                    TextView version = aboutView.findViewById(R.id.AboutTxtView_2);
                    ImageView aboutParam = aboutView.findViewById(R.id.aboutParam);
                    ImageView aboutRate = aboutView.findViewById(R.id.aboutRate);
                    ImageView aboutShare = aboutView.findViewById(R.id.aboutShare);

                    appName.setText(R.string.app_name);
                    version.setText(R.string.app_version);

                    aboutParam.setOnClickListener(v -> {

                        startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class),0);
                        overridePendingTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity);

                    });

                    aboutRate.setOnClickListener(v -> toRateUs());

                    aboutShare.setOnClickListener(v -> toShare());

                    builder
                            .setView(aboutView)
                            .show();
                    break;

                case R.id.nav_evaluate:

                    toRateUs();

                    break;

                case R.id.nav_share:

                    toShare();

                    break;

            }

        }, 475);

        // Fermeture du drawer après le chargement du fragment
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Partage de l'App
     */
    public void toShare(){

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name)+":\n\n https://play.google.com/store/apps/details?id="+getPackageName()+"\n\n");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_title)));

    }

    /**
     * Notation de l'App
     */
    public void toRateUs(){

        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        }else{

            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        }

        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" +getPackageName())));
        }

    }

    /**
     * Désactivation de tous les items
     */
    public void uncheckedAllItem(){
        for(int i = 0; i < 3; i++){
            switch (i) {
                case 0:

                    navigationView.getMenu().getItem(i).setChecked(false);
                    navigationView.getMenu().getItem(2).setChecked(false);
                    navigationView.getMenu().getItem(3).setChecked(false);
                    navigationView.getMenu().getItem(4).setChecked(false);

                    break;
                case 1:

                    for (int y = 0; y < 2; y++) {
                        navigationView.getMenu().getItem(i).getSubMenu().getItem(y).setChecked(false);
                    }

                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
    }

}
