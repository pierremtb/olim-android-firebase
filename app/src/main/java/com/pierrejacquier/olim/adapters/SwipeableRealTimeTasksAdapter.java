package com.pierrejacquier.olim.adapters;

import android.content.Context;
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
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
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

public class SwipeableRealTimeTasksAdapter
        extends RecyclerView.Adapter<SwipeableRealTimeTasksAdapter.TaskViewHolder>
        implements SwipeableItemAdapter<SwipeableRealTimeTasksAdapter.TaskViewHolder> {

    private FirebaseArray snapshots;
    private EventListener eventListener;
    private View.OnClickListener clickListener;
    private View.OnClickListener swipeableViewContainerOnClickListener;
    private int hintColor;
    private int primaryColor;
    private Context context;

    public SwipeableRealTimeTasksAdapter(Query ref, final boolean allowDone) {
        snapshots = new FirebaseArray(ref);

        snapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, final int index, int oldIndex) {
                eventListener.onDataAdded();
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

    public SwipeableRealTimeTasksAdapter(Query ref) {
        this(ref, true);
    }

    public SwipeableRealTimeTasksAdapter(DatabaseReference ref) {
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
            eventListener.onTaskClicked(getRef(position));
        }
    }

    private void onSwipeableViewContainerClick(View v) {
        if (eventListener != null) {
            View itemView = RecyclerViewAdapterUtils.getParentViewHolderItemView(v);
            RecyclerView recyclerView = RecyclerViewAdapterUtils.getParentRecyclerView(itemView);
            int position = recyclerView.getChildAdapterPosition(itemView);
            eventListener.onTaskClicked(getRef(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        context = parent.getContext();
        hintColor = context.getResources().getColor(R.color.colorHintText);
        primaryColor = context.getResources().getColor(R.color.colorPrimaryText);
        final View v = inflater.inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TaskViewHolder holder, int position) {
        final Task task = getItem(holder.getAdapterPosition());
        final ItemTaskBinding binding = holder.getBinding();
        final IconicsDrawable myTagIcon = new IconicsDrawable(context).sizeDp(20).color(Color.WHITE);

        binding.setTask(task);
        binding.taskLayout.setOnClickListener(clickListener);
        binding.taskContainer.setOnClickListener(swipeableViewContainerOnClickListener);

        binding.taskDoneToggle.setIcon(task.isDone() ? "gmd-undo" : "gmd-done");
        binding.taskDoneToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onTaskDoneToggleClicked(getRef(holder.getAdapterPosition()), task, !task.isDone());
            }
        });

        holder.itemView.setOnClickListener(clickListener);

        // Define transparency regarding done status
        if (task.isDone()) {
            binding.taskIconButton.setAlpha(Float.valueOf("0.6"));
            binding.taskPrimaryText.setTextColor(hintColor);
        } else {
            binding.taskIconButton.setAlpha(Float.valueOf("1"));
            binding.taskPrimaryText.setTextColor(primaryColor);
        }

        // Define color and icon regarding tag
        if (task.getTagKey() != null) {
            getRef(holder.getAdapterPosition()).getParent().getParent()
                    .child("tags").child(task.getTagKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Tag tag = dataSnapshot.getValue(Tag.class);
                                if (!task.isDone()) {
                                    if (tag.getColor() != null) {
                                        binding.taskIconButton
                                                .setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
                                    }
                                } else if (tag.getColor() != null) {
                                    myTagIcon.color(Color.parseColor(tag.getColor()));
                                } else {
                                    myTagIcon.color(hintColor);
                                }
                                if (tag.getIcon() != null) {
                                    try {
                                        myTagIcon.icon(tag.getIconicsName());
                                    } catch (Exception e) {
                                        myTagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
                                    }
                                } else {
                                    myTagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            myTagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            if (task.isDone()) {
                myTagIcon.color(hintColor);
                binding.taskIconButton.setBackgroundDrawable(
                        Graphics.createRoundDrawable("#00000000")
                );
            } else {
                myTagIcon.color(Color.WHITE);
                binding.taskIconButton.setBackgroundDrawable(
                        Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
                );
            }
        }

        binding.taskIconButton.setImageDrawable(myTagIcon);

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
            binding.taskContainer.setBackgroundResource(bgResId);
        }

        holder.setSwipeItemHorizontalSlideAmount(0);

        if (getItem(holder.getAdapterPosition()).isDone()) {
            holder.setMaxRightSwipeAmount(0);
            holder.setMaxLeftSwipeAmount(0);
        } else {
            holder.setMaxRightSwipeAmount(RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_RIGHT);
            holder.setMaxLeftSwipeAmount(RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT);
        }
    }

    @Override
    public int getItemCount() {
        return snapshots.getCount();
    }

    private Task getItem(int position) {
        return snapshots.getItem(position).getValue(Task.class);
    }

    private DatabaseReference getRef(int position) {
        return snapshots.getItem(position).getRef();
    }

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
            case SwipeableItemConstants.DRAWABLE_SWIPE_DOWN_BACKGROUND:
            case SwipeableItemConstants.DRAWABLE_SWIPE_UP_BACKGROUND:
            default: break;
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
            case Swipeable.RESULT_NONE:
            case Swipeable.RESULT_SWIPED_DOWN:
            case Swipeable.RESULT_SWIPED_UP:
            default: return null;
        }
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    private interface Swipeable extends SwipeableItemConstants {
    }

    public interface EventListener {
        void onDataAdded();

        void onTaskPostponed(DatabaseReference taskRef, Task task);

        void onTaskClicked(DatabaseReference taskRef);

        void onTaskDoneToggleClicked(DatabaseReference taskRef, Task task, boolean doneStatus);
    }

    static class TaskViewHolder extends AbstractSwipeableItemViewHolder {
        private ItemTaskBinding binding;

        TaskViewHolder(View v) {
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

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private final int position;
        private SwipeableRealTimeTasksAdapter adapter;

        SwipeLeftResultAction(SwipeableRealTimeTasksAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
            if (!adapter.getItem(position).isDone()) {
                adapter.eventListener.onTaskPostponed(adapter.getRef(position), adapter.getItem(position));
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            adapter = null;
        }
    }

    private static class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private final int position;
        private SwipeableRealTimeTasksAdapter adapter;

        SwipeRightResultAction(SwipeableRealTimeTasksAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
            if (!adapter.getItem(position).isDone()) {
                adapter.eventListener.onTaskDoneToggleClicked(
                        adapter.getRef(position),
                        adapter.getItem(position),
                        true
                );
                adapter.notifyItemChanged(position, null);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            adapter = null;
        }
    }
}
