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
import com.google.firebase.auth.FirebaseAuth;
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
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ActivityMainBinding;
import com.pierrejacquier.olim.fragments.LoadingFragment;
import com.pierrejacquier.olim.fragments.TagsFragment;
import com.pierrejacquier.olim.fragments.TasksFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity
        extends AppCompatActivity
        implements TasksFragment.OnFragmentInteractionListener,
        TagsFragment.OnFragmentInteractionListener {

    private final static int DRAWER_TASKS = 1;
    private final static int DRAWER_TAGS = 2;
    private final static int DRAWER_DIVIDER = 3;
    private final static int DRAWER_SETTINGS = 4;
    private final static int DRAWER_ABOUT = 4;
    private final static int DRAWER_SIGNOUT = 5;
    private static final int RC_SIGN_IN = 10;
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
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    buildDrawer();
                    showTasksFragment();
                } else {
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
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        actionsMenu = menu;
//                .getReference().child("users")
//                .child(auth.getCurrentUser().getUid())
//                .child("tasks")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot child : dataSnapshot.getChildren()) {
//                            Task task = child.getValue(Task.class);
//                            task.setKey(child.getKey());
//                            tasks.add(task);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
//
//        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        theTextArea.setTextColor(getResources().getColor(R.color.colorPrimaryText));
//        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean opening) {
//                if (opening) {
//                    getTasksFragment().hideTaskAdder();
//                } else {
//                    getTasksFragment().showTaskAdder();
//                }
//            }
//        });
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                List<Task> filteredTasks = new ArrayList<>();
//                if (newText.equals("")) {
//                    filteredTasks = tasks;
//                } else {
//                    for (Task task : tasks) {
//                        if (task.getTitle() != null &&
//                                task.getTitle().toUpperCase().contains(newText.toUpperCase())) {
//                            filteredTasks.add(task);
//                        }
//                    }
//                }
//                Log.e("iiiii", filteredTasks.toString());
//                return false;
//            }
//        });
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
//            case R.id.action_search:
//                if (currentFragmentName.equals("TasksFragment")) {
//                    getTasksFragment().hideTaskAdder();
//                }
//                break;
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
                    showTasksFragment();
                }
                break;
        }
    }

    /**
     * Navigation handling methods
     */

    private void showTasksFragment() {
        Fragment TasksFG = new TasksFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, TasksFG, "TasksFragment");
        actionBar.setTitle("Tasks");
        currentFragmentName = "TasksFragment";
        ft.commit();
        if (actionsMenu != null) {
            actionsMenu.getItem(0).setVisible(true);
        }
    }

    private void showTagsFragment() {
        Fragment TagsFG = new TagsFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, TagsFG);
        actionBar.setTitle("Tags");
        currentFragmentName = "TagsFragment";
        ft.commit();
        actionsMenu.getItem(0).setVisible(false);
    }

    private void showLoadingFragment() {
        Fragment loadingFragment = new LoadingFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrame, loadingFragment);
        actionBar.setTitle("Olim");
        currentFragmentName = "LoadingFragment";
        ft.commit();
    }

    private void signOut() {
        auth.signOut();
    }

    private TasksFragment getTasksFragment() {
        return (TasksFragment) getSupportFragmentManager().findFragmentByTag("TasksFragment");
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
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }

                //we use the default one for
                //DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name()

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
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_TASKS)
                                .withName(R.string.navigation_drawer_tasks)
                                .withIcon(GoogleMaterial.Icon.gmd_done_all),
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_TAGS)
                                .withName(R.string.navigation_drawer_tags)
                                .withIcon(GoogleMaterial.Icon.gmd_label_outline),
                        new DividerDrawerItem().withIdentifier(DRAWER_DIVIDER),
//                        new SecondaryDrawerItem()
//                                .withIdentifier(DRAWER_SETTINGS)
//                                .withName(R.string.navigation_drawer_settings)
//                                .withIcon(GoogleMaterial.Icon.gmd_settings)
//                                .withSelectable(false),
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
                            case DRAWER_TASKS:
                                showTasksFragment();
                                break;
                            case DRAWER_TAGS:
                                showTagsFragment();
                                break;
//                            case DRAWER_SETTINGS:
//                                launchSettings();
//                                break;
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
                .load("http://www.elementaryos-fr.org/wp-content/uploads/2013/09/wallpaper-2132913.jpg")
                .into(coverView);
    }
}
