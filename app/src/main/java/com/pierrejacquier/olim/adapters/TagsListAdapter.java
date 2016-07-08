package com.pierrejacquier.olim.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.databinding.ItemTagBinding;
import com.pierrejacquier.olim.utils.FirebaseArray;
import com.pierrejacquier.olim.utils.Graphics;

public class TagsListAdapter extends BaseAdapter {
    private final Context context;
    private final View.OnClickListener callback;
    FirebaseArray snapshots;

    public TagsListAdapter(Context context, Query ref, View.OnClickListener callback ) {
        snapshots = new FirebaseArray(ref);
        snapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, int index, int oldIndex) {
                notifyDataSetChanged();
            }
        });
        this.context = context;
        this.callback = callback;
    }

    public TagsListAdapter(Context context, DatabaseReference ref, View.OnClickListener callback) {
        this(context, (Query) ref, callback);
    }

    public void cleanup() {
        snapshots.cleanup();
    }

    @Override
    public int getCount() {
        return snapshots.getCount();
    }

    @Override
    public Tag getItem(int i) {
        return snapshots.getItem(i).getValue(Tag.class);
    }

    public DatabaseReference getRef(int position) { return snapshots.getItem(position).getRef(); }

    @Override
    public long getItemId(int i) {
        return snapshots.getItem(i).getKey().hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ItemTagBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_tag, parent, false);
        IconicsDrawable tagIcon = new IconicsDrawable(parent.getContext()).sizeDp(20).color(Color.WHITE);
        int hintColor = context.getResources().getColor(R.color.colorHintText);
        final Tag tag = getItem(position);
        binding.setTag(tag);
        binding.tagEdit.setVisibility(View.VISIBLE);

        if (tag.getColor() != null) {
            binding.tagIconButton.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            binding.tagIconButton.setBackgroundDrawable(
                    Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
            );
        }

        if (tag.getIcon() != null) {
            try {
                tagIcon.icon(GoogleMaterial.Icon.valueOf("gmd_" + tag.getIcon()));
            } catch (Exception e ) {
                tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            }
        } else {
            tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
        }

        binding.tagIconButton.setImageDrawable(tagIcon);

        View rowView = binding.getRoot();

        tag.setKey(getRef(position).getKey());

        rowView.setOnClickListener(callback);
        rowView.setTag(tag);

        binding.tagEdit.setOnClickListener(callback);
        binding.tagEdit.setTag(tag);

        return rowView;
    }
} 