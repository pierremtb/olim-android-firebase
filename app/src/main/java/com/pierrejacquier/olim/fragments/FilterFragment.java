package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.activities.TagActivity;
import com.pierrejacquier.olim.activities.TaskActivity;
import com.pierrejacquier.olim.adapters.TagsAdapter;
import com.pierrejacquier.olim.adapters.TasksAdapter;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.FragmentFilterBinding;
import com.pierrejacquier.olim.databinding.FragmentSearchBinding;
import com.pierrejacquier.olim.utils.Graphics;

import java.util.ArrayList;
import java.util.List;

public class FilterFragment extends Fragment implements View.OnClickListener, TasksAdapter.EventListener, TagsAdapter.EventListener {

    private OnFragmentInteractionListener Main;
    private FragmentFilterBinding binding;
    private DatabaseReference tagsRef;
    private DatabaseReference tasksRef;
    private List<Tag> tags = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();
    private Tag currentTag = null;
    private String currentTagKey = null;

    public FilterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tagsRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("tags");
        tasksRef = tagsRef.getParent().child("tasks");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_filter, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentTagKey = getArguments().getString("tag_key");

        tagsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Tag tag = child.getValue(Tag.class);
                        tag.setKey(child.getKey());
                        tags.add(tag);
                        if (child.getKey().equals(currentTagKey)) {
                            setCurrentTag(tag);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Task task = child.getValue(Task.class);
                        task.setKey(child.getKey());
                        tasks.add(task);
                    }
                    updateResults();
                    binding.tasksRecyclerView.getAdapter().notifyDataSetChanged();
                    binding.tasksCard.setVisibility(View.VISIBLE);
//                    binding.noTasksLayout.setVisibility(View.GONE);
                } else {
                    binding.tasksCard.setVisibility(View.GONE);
//                    binding.noTasksLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TasksAdapter tasksAdapter = new TasksAdapter(tasks);
        tasksAdapter.setEventListener(this);
        binding.tasksRecyclerView.setAdapter(tasksAdapter);
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.currentTagChipIconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showTasksFragment();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Main = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
    }

    /**
     * Navigation
     */

    private void launchTaskActivity(String taskKey) {
        Intent intent = new Intent(getActivity(), TaskActivity.class);
        Bundle b = new Bundle();
        b.putString("task_key", taskKey);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    private void launchTagActivity(String tagKey) {
        Intent intent = new Intent(getActivity(), TagActivity.class);
        Bundle b = new Bundle();
        b.putString("tag_key", tagKey);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onTaskClicked(String taskRef) {
        launchTaskActivity(taskRef);
    }

    @Override
    public void onTaskDoneToggleClicked(Task task, boolean doneStatus) {
        task.setDone(doneStatus);
        tasksRef.child(task.getKey()).setValue(task.getMap());
    }

    @Override
    public void onTagClicked(String tagKey) {
        launchTagActivity(tagKey);
    }

    public interface OnFragmentInteractionListener {
        void showTasksFragment();
    }

    /**
     * Display
     */
    
    private void updateResults() {
        List<Task> filteredTasks = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getTagKey() != null) {
                if (task.getTagKey().equals(currentTagKey)) {
                    task.setTag(currentTag);
                    filteredTasks.add(task);
                }
            }
        }

        TasksAdapter tasksAdapter = new TasksAdapter(filteredTasks);
        tasksAdapter.setEventListener(this);
        binding.tasksRecyclerView.setAdapter(tasksAdapter);

        binding.tasksCard.setVisibility(filteredTasks.size() == 0 ? View.GONE : View.VISIBLE);
        binding.noResultsLayout.setVisibility(filteredTasks.size() != 0 ? View.GONE : View.VISIBLE);
    }

    private void setCurrentTag(Tag tag) {
        currentTag = tag;

        if (tag == null) {
            currentTagKey = null;
            return;
        }

        binding.currentTagChipLayout.setVisibility(View.VISIBLE);
        binding.currentTagChipLabel.setText(currentTag.getHashName());
        IconicsDrawable tagIcon = new IconicsDrawable(getContext()).sizeDp(13).color(Color.WHITE);
        int hintColor = getContext().getResources().getColor(R.color.colorHintText);

        if (tag.getColor() != null) {
            binding.currentTagChipIcon.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            binding.currentTagChipIcon.setBackgroundDrawable(
                    Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
            );
        }

        if (tag.getIcon() != null) {
            try {
                tagIcon.icon(GoogleMaterial.Icon.valueOf("gmd_" + tag.getIcon()));
            } catch (Exception e) {
                tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            }
        } else {
            tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
        }

        binding.currentTagChipIcon.setImageDrawable(tagIcon);

        binding.currentTagChipIconDelete.setBackgroundDrawable(Graphics.createRoundDrawable("#8C000000"));
        binding.currentTagChipIconDelete.setImageDrawable(
                new IconicsDrawable(getContext()).icon(GoogleMaterial.Icon.gmd_clear)
                        .sizeDp(9)
                        .color(Color.parseColor("#D4D4D4"))
        );
        binding.currentTagChipIconDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.showTasksFragment();
            }
        });
    }

    private void clearCurrentTag() {
        binding.currentTagChipLayout.setVisibility(View.GONE);
        setCurrentTag(null);
    }

}
