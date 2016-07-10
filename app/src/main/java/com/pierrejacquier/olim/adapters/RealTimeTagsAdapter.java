package com.pierrejacquier.olim.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.databinding.ItemTagBinding;
import com.pierrejacquier.olim.utils.FirebaseArray;
import com.pierrejacquier.olim.utils.Graphics;

public class RealTimeTagsAdapter extends
        RecyclerView.Adapter<RealTimeTagsAdapter.ViewHolder> {

    public int hintColor;
    public IconicsDrawable tagIcon;
    FirebaseArray mSnapshots;
    private View.OnClickListener itemViewOnClickListener;
    private EventListener eventListener;

    public RealTimeTagsAdapter(Query ref) {
        mSnapshots = new FirebaseArray(ref);

        mSnapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, int index, int oldIndex) {
                switch (type) {
                    case Added:
                        notifyItemInserted(index);
                        break;
                    case Changed:
                        notifyItemChanged(index);
                        break;
                    case Removed:
                        notifyItemRemoved(index);
                        break;
                    case Moved:
                        notifyItemMoved(oldIndex, index);
                        break;
                    default:
                        throw new IllegalStateException("Incomplete case statement");
                }
            }
        });

        itemViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v);
            }
        };
    }

    public RealTimeTagsAdapter(DatabaseReference ref) {
        this((Query) ref);
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
        Tag tag = getItem(position);
        viewHolder.getBinding().setTag(tag);
        viewHolder.getBinding().getRoot().setOnClickListener(itemViewOnClickListener);

        if (tag.getColor() != null) {
            viewHolder.getBinding().tagIconButton.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
        } else {
            viewHolder.getBinding().tagIconButton.setBackgroundDrawable(
                    Graphics.createRoundDrawable(hintColor)
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

        viewHolder.getBinding().tagIconButton.setImageDrawable(tagIcon);
    }

    @Override
    public int getItemCount() {
        return mSnapshots.getCount();
    }

    public Tag getItem(int position) {
        return mSnapshots.getItem(position).getValue(Tag.class);
    }

    public DatabaseReference getRef(int position) {
        return mSnapshots.getItem(position).getRef();
    }

    @Override
    public long getItemId(int position) {
        return mSnapshots.getItem(position).getKey().hashCode();
    }

    private void onItemViewClick(View v) {
        if (eventListener != null) {
            RecyclerView recyclerView = RecyclerViewAdapterUtils.getParentRecyclerView(v);
            int position = recyclerView.getChildAdapterPosition(v);
            eventListener.onTagClicked(getRef(position));
        }
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public interface EventListener {
        void onTagClicked(DatabaseReference tagRef);
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
}
