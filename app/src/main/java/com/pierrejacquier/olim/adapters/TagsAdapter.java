package com.pierrejacquier.olim.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ItemTagBinding;
import com.pierrejacquier.olim.utils.Graphics;

import java.util.List;

public class TagsAdapter extends
        RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private List<Tag> tags;
    public int hintColor;
    public IconicsDrawable tagIcon;
    private EventListener eventListener;

    public TagsAdapter(List<Tag> tags) {
        this.tags = tags;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTagBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public ItemTagBinding getBinding() {
            return binding;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tagView = inflater.inflate(R.layout.item_tag, parent, false);

        hintColor = parent.getContext().getResources().getColor(R.color.colorHintText);
        tagIcon = new IconicsDrawable(parent.getContext()).sizeDp(20).color(Color.WHITE);

        return new ViewHolder(tagView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final Tag tag = tags.get(position);
        ItemTagBinding binding = viewHolder.getBinding();
        binding.setTag(tag);
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onTagClicked(tag.getKey());
            }
        });

        if (tag.getColor() != null) {
            binding.tagIconButton.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            binding.tagIconButton.setBackgroundDrawable(
                    Graphics.createRoundDrawable(hintColor)
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
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public interface EventListener {
        void onTagClicked(String tagRef);
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
}
