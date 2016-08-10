package com.pierrejacquier.olim.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ActivityMainBinding;
import com.pierrejacquier.olim.fragments.FilterFragment;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.SearchFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements TasksFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        FilterFragment.OnFragmentInteractionListener,
        TagsFragment.OnFragmentInteractionListener {


    public final static int DRAWER_TASKS_HEADER = 1;
    public final static int DRAWER_TASKS_OVERDUE = 2;
    public final static int DRAWER_TASKS_TODAY = 3;
    public final static int DRAWER_TASKS_TOMORROW = 4;
    public final static int DRAWER_TASKS_IN_THE_NEXT_SEVEN_DAYS = 5;
    public final static int DRAWER_TASKS_ALL = 6;
    public final static int DRAWER_MANAGE_HEADER = 7;
    public final static int DRAWER_TAGS = 8;
    public final static int DRAWER_SETTINGS = 9;
    public final static int DRAWER_ABOUT = 10;
    public final static int DRAWER_SIGNOUT = 11;
    private static final int RC_SIGN_IN = 30;

    public int lastTargetedTasks = -1;

    private Menu actionsMenu;
    private ActionBar actionBar;
    private String currentFragmentName = null;
    private Toolbar toolbar;
    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do some layout stuff
        initGlide();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Tasks");
        }

        // Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            launchAuth();
            return;
        }

        buildDrawer();
        showTasksFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        actionsMenu = menu;
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        theTextArea.setTextColor(getResources().getColor(R.color.colorPrimaryText));
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean opening) {
                if (!opening) {
                    showTasksFragment();
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (getSearchFragment() == null) {
                    return false;
                }
                getSearchFragment().updateResults(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_filter:
                if (currentFragmentName.equals("TasksFragment")) {
                    TasksFragment tasksFragment = (TasksFragment) getSupportFragmentManager().findFragmentByTag("TasksFragment");
                    tasksFragment.showTagsFilteringDialog();
                }
                break;
            case R.id.action_search:
                if (currentFragmentName.equals("TasksFragment")) {
                    showSearchFragment();
                }
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    buildDrawer();
                    showTasksFragment();
                }
                break;
        }
    }

    /**
     * Navigation handling methods
     */

    public void showTasksFragment() {
        showTasksFragment(-1);
    }

    private void showTasksFragment(int tasksTarget) {
        if (getTasksFragment() == null) {
            if (tasksTarget == -1 && lastTargetedTasks != -1) {
                tasksTarget = lastTargetedTasks;
            }
            Fragment TasksFG = new TasksFragment();
            Bundle b = new Bundle();
            b.putInt("targeted_tasks", tasksTarget);
            TasksFG.setArguments(b);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
            ft.replace(R.id.mainFrame, TasksFG, "TasksFragment");
            actionBar.setTitle("Tasks");
            currentFragmentName = "TasksFragment";
            ft.commit();
            lastTargetedTasks = tasksTarget;
            if (actionsMenu != null) {
                actionsMenu.getItem(0).setVisible(true);
                actionsMenu.getItem(1).setVisible(true);
            }
        } else {
            getTasksFragment().showTargetedTasks(tasksTarget);
        }
    }

    private void showTagsFragment() {
        Fragment TagsFG = new TagsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
        ft.replace(R.id.mainFrame, TagsFG);
        actionBar.setTitle("Tags");
        currentFragmentName = "TagsFragment";
        ft.commit();
        if (actionsMenu != null) {
            actionsMenu.getItem(0).setVisible(false);
            actionsMenu.getItem(1).setVisible(false);
        }
    }

    private void showLoadingFragment() {
        Fragment loadingFragment = new LoadingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, loadingFragment);
        actionBar.setTitle("Olim");
        currentFragmentName = "LoadingFragment";
        ft.commit();
    }

    private void showSearchFragment() {
        Fragment searchFragment = new SearchFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
        ft.replace(R.id.mainFrame, searchFragment, "SearchFragment");
        actionBar.setTitle("Search");
        currentFragmentName = "SearchFragment";
        ft.commit();
        if (actionsMenu != null) {
            actionsMenu.getItem(0).setVisible(false);
            actionsMenu.getItem(1).setVisible(false);
        }
    }

    public void showFilterFragment(String tagKey) {
        Fragment filterFragment = new FilterFragment();
        Bundle b = new Bundle();
        b.putString("tag_key", tagKey);
        filterFragment.setArguments(b);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);
        ft.replace(R.id.mainFrame, filterFragment, "FilterFragment");
        actionBar.setTitle("Filter");
        currentFragmentName = "FilterFragment";
        ft.commit();
        if (actionsMenu != null) {
            actionsMenu.getItem(0).setVisible(false);
            actionsMenu.getItem(1).setVisible(false);
        }
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            launchAuth();
                        }
                    }
                });
    }

    private TasksFragment getTasksFragment() {
        return (TasksFragment) getSupportFragmentManager().findFragmentByTag("TasksFragment");
    }

    private SearchFragment getSearchFragment() {
        return (SearchFragment) getSupportFragmentManager().findFragmentByTag("SearchFragment");
    }

    private void launchAuth() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(
                                AuthUI.EMAIL_PROVIDER,
                                AuthUI.GOOGLE_PROVIDER)
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }


    private void launchSettings() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    private void launchAbout() {
        new LibsBuilder()
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle(getResources().getString(R.string.navigation_drawer_about))
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutDescription(getResources().getString(R.string.app_description))
                .start(this);
    }

    /**
     * Display handling methods
     */

    private void showSnack(String text) {
        Snackbar.make(binding.mainLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initGlide() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                return super.placeholder(ctx, tag);
            }
        });
    }

    private void buildDrawer() {
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.background)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(auth.getCurrentUser().getDisplayName())
                                .withEmail(auth.getCurrentUser().getEmail())
                                .withIcon(auth.getCurrentUser().getPhotoUrl())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(true)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new SectionDrawerItem().withName(R.string.navigation_drawer_tasks)
                                .withIdentifier(DRAWER_TASKS_HEADER)
                                .withDivider(false),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS_OVERDUE)
                                .withName(R.string.overdue)
                                .withIcon(GoogleMaterial.Icon.gmd_warning),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS_TODAY)
                                .withName(R.string.today)
                                .withIcon(GoogleMaterial.Icon.gmd_hourglass_full),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS_TOMORROW)
                                .withName(R.string.tomorrow)
                                .withIcon(GoogleMaterial.Icon.gmd_hourglass_empty),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS_IN_THE_NEXT_SEVEN_DAYS)
                                .withName(R.string.in_the_next_seven_days)
                                .withIcon(GoogleMaterial.Icon.gmd_event),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS_ALL)
                                .withName(R.string.all_upcoming)
                                .withIcon(GoogleMaterial.Icon.gmd_all_inclusive),
                        new SectionDrawerItem().withName(R.string.manage)
                                .withIdentifier(DRAWER_MANAGE_HEADER),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_TAGS)
                                .withName(R.string.navigation_drawer_tags)
                                .withIcon(GoogleMaterial.Icon.gmd_label_outline),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_SETTINGS)
                                .withName(R.string.navigation_drawer_settings)
                                .withIcon(GoogleMaterial.Icon.gmd_settings)
                                .withSelectable(false),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_ABOUT)
                                .withName(R.string.navigation_drawer_about)
                                .withIcon(GoogleMaterial.Icon.gmd_info_outline)
                                .withSelectable(false),
                        new SecondaryDrawerItem()
                                .withIdentifier(DRAWER_SIGNOUT)
                                .withName(R.string.navigation_drawer_signout)
                                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (position) {
                            case DRAWER_TASKS_OVERDUE:
                            case DRAWER_TASKS_TODAY:
                            case DRAWER_TASKS_TOMORROW:
                            case DRAWER_TASKS_IN_THE_NEXT_SEVEN_DAYS:
                            case DRAWER_TASKS_ALL:
                                showTasksFragment(position);
                                break;
                            case DRAWER_TAGS:
                                showTagsFragment();
                                break;
                            case DRAWER_SETTINGS:
//                                launchSettings();
                                break;
                            case DRAWER_ABOUT:
                                launchAbout();
                                break;
                            case DRAWER_SIGNOUT:
                                signOut();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                })
                .build();

        // TODO: replace with a local image or account cover
        ImageView coverView = headerResult.getHeaderBackgroundView();
        Glide.with(this)
                .load("http://www.elementaryos-fr.org/wp-content/uploads/2013/09/wallpaper-1738907-240x150.png")
                .into(coverView);
    }
}
