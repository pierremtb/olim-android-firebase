package com.pierrejacquier.olim.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ItemTagBinding;
import com.pierrejacquier.olim.databinding.ItemTaskBinding;
import com.pierrejacquier.olim.utils.Graphics;

import java.util.List;

public class TasksAdapter extends
        RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private List<Task> tasks;
    private int hintColor;
    private int primaryColor;
    private Context context;
    private EventListener eventListener;

    public TasksAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemTaskBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public ItemTaskBinding getBinding() {
            return binding;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View tagView = inflater.inflate(R.layout.item_task, parent, false);

        hintColor = context.getResources().getColor(R.color.colorHintText);
        primaryColor = context.getResources().getColor(R.color.colorPrimaryText);

        return new ViewHolder(tagView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Task task = getItem(holder.getAdapterPosition());
        final ItemTaskBinding binding = holder.getBinding();
        final IconicsDrawable myTagIcon = new IconicsDrawable(context).sizeDp(20).color(Color.WHITE);

        binding.setTask(task);

        binding.taskDoneToggle.setIcon(task.isDone() ? "gmd-undo" : "gmd-done");
        binding.taskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onTaskClicked(task.getKey());
            }
        });
        binding.taskDoneToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eventListener.onTaskDoneToggleClicked(task, !task.isDone());
            }
        });

        // Define transparency regarding done status
        if (task.isDone()) {
            binding.taskIconButton.setAlpha(Float.valueOf("0.6"));
            binding.taskPrimaryText.setTextColor(hintColor);
        } else {
            binding.taskIconButton.setAlpha(Float.valueOf("1"));
            binding.taskPrimaryText.setTextColor(primaryColor);
        }

        // Define color and icon regarding tag
        if (task.getTag() != null) {
            Tag tag = task.getTag();
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
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    private Task getItem(int i) {
        return tasks.get(i);
    }

    public interface EventListener {
        void onTaskClicked(String taskRef);

        void onTaskDoneToggleClicked(Task task, boolean doneStatus);
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }
}
