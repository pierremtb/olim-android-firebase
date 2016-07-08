package com.pierrejacquier.olim.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ItemTaskBinding;
import com.pierrejacquier.olim.utils.FirebaseArray;
import com.pierrejacquier.olim.utils.Graphics;

public class SwipeableTaskAdapter
        extends RecyclerView.Adapter<SwipeableTaskAdapter.TaskViewHolder>
        implements SwipeableItemAdapter<SwipeableTaskAdapter.TaskViewHolder> {

    private FirebaseArray snapshots;

    private interface Swipeable extends SwipeableItemConstants {}

    private EventListener eventListener;
    private View.OnClickListener clickListener;
    private View.OnClickListener swipeableViewContainerOnClickListener;

    private int hintColor;
    private IconicsDrawable tagIcon;

    public interface EventListener {
        void onItemRemoved(DatabaseReference taskRef, Task task);

        void onItemPinned(DatabaseReference taskRef, Task task);

        void onItemViewClicked(DatabaseReference taskRef);
    }

    public static class TaskViewHolder extends AbstractSwipeableItemViewHolder {
        private ItemTaskBinding binding;

        public TaskViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ItemTaskBinding getBinding() {
            return binding;
        }

        @Override
        public View getSwipeableContainerView() {
            return binding.taskContainer;
        }
    }

    public SwipeableTaskAdapter(Query ref) {
        snapshots = new FirebaseArray(ref);

        snapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, final int index, int oldIndex) {
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

        clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v);
            }
        };
        swipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeableViewContainerClick(v);
            }
        };
        setHasStableIds(true);
    }

    public SwipeableTaskAdapter(DatabaseReference ref) {
        this((Query) ref);
    }

    public void cleanup() {
        snapshots.cleanup();
    }

    private void onItemViewClick(View v) {
        if (eventListener != null) {
            View itemView = RecyclerViewAdapterUtils.getParentViewHolderItemView(v);
            RecyclerView recyclerView = RecyclerViewAdapterUtils.getParentRecyclerView(itemView);
            int position = recyclerView.getChildAdapterPosition(itemView);
            eventListener.onItemViewClicked(getRef(position)); // true --- pinned
        }
    }

    private void onSwipeableViewContainerClick(View v) {
        if (eventListener != null) {
            View itemView = RecyclerViewAdapterUtils.getParentViewHolderItemView(v);
            RecyclerView recyclerView = RecyclerViewAdapterUtils.getParentRecyclerView(itemView);
            int position = recyclerView.getChildAdapterPosition(itemView);
            eventListener.onItemViewClicked(getRef(position));  // false --- not pinned
        }
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_task;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        hintColor = parent.getContext().getResources().getColor(R.color.colorHintText);
        tagIcon = new IconicsDrawable(parent.getContext()).sizeDp(20).color(Color.WHITE);
        final View v = inflater.inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, int position) {
        final Task task = getItem(position);
        holder.getBinding().setTask(task);
        holder.getBinding().executePendingBindings();

        holder.getBinding().taskLayout.setOnClickListener(clickListener);
        holder.itemView.setOnClickListener(clickListener);
        holder.getBinding().taskContainer.setOnClickListener(swipeableViewContainerOnClickListener);


        if (task.getTagKey() != null) {
            getRef(position).getParent().getParent()
                    .child("tags").child(task.getTagKey())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Tag tag = dataSnapshot.getValue(Tag.class);
                                if (!task.isDone()) {
                                    if (tag.getColor() != null) {
                                        holder.getBinding().taskIconButton
                                                .setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
                                    }
                                } else if (tag.getColor() != null) {
                                    tagIcon.color(Color.parseColor(tag.getColor()));
                                } else {
                                    tagIcon.color(hintColor);
                                }
                                if (tag.getIcon() != null) {
                                    try {
                                        tagIcon.icon(tag.getIconicsName());
                                    } catch (Exception e) {
                                        tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
                                    }
                                } else {
                                    tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            if (task.isDone()) {
                tagIcon.color(hintColor);
            } else {
                holder.getBinding().taskIconButton.setBackgroundDrawable(
                        Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
                );
            }
        }
        if (task.isDone()) {
            holder.getBinding().taskIconButton.setAlpha(Float.valueOf("0.6"));
            holder.getBinding().taskPrimaryText.setTextColor(hintColor);
        }

        holder.getBinding().taskIconButton.setImageDrawable(tagIcon);

        final int swipeState = holder.getSwipeStateFlags();

        if ((swipeState & Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;

            if ((swipeState & Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & Swipeable.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.getBinding().taskContainer.setBackgroundResource(bgResId);
        }

        holder.setSwipeItemHorizontalSlideAmount(0);
    }

    @Override
    public int getItemCount() {
        return snapshots.getCount();
    }

    public Task getItem(int position) {
        return snapshots.getItem(position).getValue(Task.class);
    }

    public DatabaseReference getRef(int position) { return snapshots.getItem(position).getRef(); }

    @Override
    public long getItemId(int position) {
        return snapshots.getItem(position).getKey().hashCode();
    }

    @Override
    public int onGetSwipeReactionType(TaskViewHolder holder, int position, int x, int y) {
        return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(TaskViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(TaskViewHolder holder, final int position, int result) {
        switch (result) {
            case Swipeable.RESULT_SWIPED_RIGHT:
                return new SwipeRightResultAction(this, position);
            case Swipeable.RESULT_SWIPED_LEFT:
                return new SwipeLeftResultAction(this, position);
            case Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private SwipeableTaskAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(SwipeableTaskAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

//            mAdapter.notifyItemChanged(mPosition);
//            mSetPinned = true;
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.eventListener != null) {
                mAdapter.eventListener.onItemPinned(mAdapter.getRef(mPosition), mAdapter.getItem(mPosition));
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
        }
    }

    private static class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private SwipeableTaskAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(SwipeableTaskAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
//            mAdapter.tasks.remove(mPosition);
//            mAdapter.notifyItemRemoved(mPosition);
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.eventListener != null) {
                mAdapter.eventListener.onItemRemoved(mAdapter.getRef(mPosition), mAdapter.getItem(mPosition));
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            mAdapter = null;
        }
    }

    private static class UnpinResultAction extends SwipeResultActionDefault {
        private SwipeableTaskAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(SwipeableTaskAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

//            Task task = mAdapter.tasks.get(mPosition);
//            task.setPinned(false);
//            mAdapter.notifyItemChanged(mPosition);
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            mAdapter = null;
        }
    }
}
