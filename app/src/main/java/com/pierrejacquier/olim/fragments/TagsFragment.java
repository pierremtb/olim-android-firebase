package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
import com.pierrejacquier.olim.adapters.TagsAdapter;
import com.pierrejacquier.olim.databinding.FragmentTagsBinding;

public class TagsFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener Main;
    private FragmentTagsBinding binding;
    private DatabaseReference tagsRef;

    public TagsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tagsRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("tags");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags, container, false);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTagActivity(null);
            }
        });


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
                    binding.noTagsLayout.setVisibility(View.GONE);
                } else {
                    binding.tagsCard.setVisibility(View.GONE);
                    binding.noTagsLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TagsAdapter adapter = new TagsAdapter(tagsRef);
        adapter.setEventListener(new TagsAdapter.EventListener() {
            @Override
            public void onTagClicked(DatabaseReference tagRef) {
                launchTagActivity(tagRef.getKey());
            }
        });
        binding.tagsRecyclerView.setAdapter(adapter);
        binding.tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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

    private void launchTagActivity(String tagKey) {
        Intent intent = new Intent(getActivity(), TagActivity.class);
        Bundle b = new Bundle();
        b.putString("tag_key", tagKey);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    public interface OnFragmentInteractionListener {
    }

}
