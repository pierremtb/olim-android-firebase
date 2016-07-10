package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.activities.TagActivity;
import com.pierrejacquier.olim.activities.TaskActivity;
import com.pierrejacquier.olim.adapters.RealTimeTagsAdapter;
import com.pierrejacquier.olim.adapters.TagsAdapter;
import com.pierrejacquier.olim.adapters.TasksAdapter;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.FragmentSearchBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements View.OnClickListener, TasksAdapter.EventListener, TagsAdapter.EventListener {

    private OnFragmentInteractionListener Main;
    private FragmentSearchBinding binding;
    private DatabaseReference tagsRef;
    private DatabaseReference tasksRef;
    private List<Tag> tags = new ArrayList<>();
    private List<Task> tasks = new ArrayList<>();

    public SearchFragment() {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tagsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    binding.tagsCard.setVisibility(View.VISIBLE);
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Tag tag = child.getValue(Tag.class);
                        tag.setKey(child.getKey());
                        tags.add(tag);
                    }
                    binding.tagsRecyclerView.getAdapter().notifyDataSetChanged();
//                    binding.noTagsLayout.setVisibility(View.GONE);
                } else {
                    binding.tagsCard.setVisibility(View.GONE);
//                    binding.noTagsLayout.setVisibility(View0.1VISIBLE);
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

        TagsAdapter tagsAdapter = new TagsAdapter(tags);
        tagsAdapter.setEventListener(this);
        binding.tagsRecyclerView.setAdapter(tagsAdapter);
        binding.tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        TasksAdapter tasksAdapter = new TasksAdapter(tasks);
        tasksAdapter.setEventListener(this);
        binding.tasksRecyclerView.setAdapter(tasksAdapter);
        binding.tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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
    }

    /**
     * Display
     */
    
    public void updateResults(String query) {
        List<Tag> filteredTags = new ArrayList<>();
        List<Task> filteredTasks = new ArrayList<>();

        for (Tag tag : tags) {
            if (tag.getName().toUpperCase().contains(query.toUpperCase())) {
                filteredTags.add(tag);
            }
        }
        for (Task task : tasks) {
            if (task.getTitle().toUpperCase().contains(query.toUpperCase())) {
                filteredTasks.add(task);
            }
        }

        TagsAdapter tagsAdapter = new TagsAdapter(filteredTags);
        tagsAdapter.setEventListener(this);
        binding.tagsRecyclerView.setAdapter(tagsAdapter);
        TasksAdapter tasksAdapter = new TasksAdapter(filteredTasks);
        tasksAdapter.setEventListener(this);
        binding.tasksRecyclerView.setAdapter(tasksAdapter);

        binding.tasksCard.setVisibility(filteredTasks.size() == 0 ? View.GONE : View.VISIBLE);
        binding.tagsCard.setVisibility(filteredTags.size() == 0 ? View.GONE : View.VISIBLE);
        binding.noResultsLayout.setVisibility(
                filteredTasks.size() != 0 || filteredTags.size() != 0 ? View.GONE : View.VISIBLE
        );
    }

}
