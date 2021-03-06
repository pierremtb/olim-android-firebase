package com.pierrejacquier.olim.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.SwipeDismissItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.touchguard.RecyclerViewTouchActionGuardManager;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.activities.MainActivity;
import com.pierrejacquier.olim.activities.TagActivity;
import com.pierrejacquier.olim.activities.TaskActivity;
import com.pierrejacquier.olim.adapters.SwipeableRealTimeTasksAdapter;
import com.pierrejacquier.olim.adapters.TagsListAdapter;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.FragmentTasksBinding;
import com.pierrejacquier.olim.utils.CustomLinearLayoutManager;
import com.pierrejacquier.olim.utils.Graphics;
import com.pierrejacquier.olim.utils.Taskify;
import com.pierrejacquier.olim.utils.TaskifyBackend;
import com.pierrejacquier.olim.utils.Tools;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TasksFragment
        extends Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    private MainActivity mainActivity;
    private OnFragmentInteractionListener Main;
    private FragmentTasksBinding binding;

    private Task newTask = new Task();
    private MaterialDialog tagsFilteringDialog;
    private MaterialDialog tagChooserDialog;
    private DatePickerDialog newTaskDueDatePickerDialog;
    private TimePickerDialog newTaskDueTimePickerDialog;
    private DatabaseReference tasksRef;
    private DatabaseReference tagsRef;

    private List<Tag> tags = new ArrayList<>();

    public TasksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasksRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("tasks");
        tagsRef = tasksRef.getParent().child("tags");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tasks, container, false);

        binding.postponeAllOverdueTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(getOverdueTasksQuery());
            }
        });
        binding.postponeAllTodayTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(getTodayTasksQuery());
            }
        });
        binding.postponeAllTomorrowTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(getTomorrowTasksQuery());
            }
        });
        binding.postponeAllInTheNextSevenDaysTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(getInTheNextSevenDaysTasksQuery());
            }
        });
        binding.postponeAllLaterTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postponeAllTheseTasks(getLaterTasksQuery());
            }
        });

        binding.markAsDoneAllOverdueTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(getOverdueTasksQuery());
            }
        });
        binding.markAsDoneAllTodayTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(getTodayTasksQuery());
            }
        });
        binding.markAsDoneAllTomorrowTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(getTomorrowTasksQuery());
            }
        });
        binding.markAsDoneAllInTheNextSevenDaysTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(getInTheNextSevenDaysTasksQuery());
            }
        });
        binding.markAsDoneAllLaterTasksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsDoneAllTheseTasks(getLaterTasksQuery());
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
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    Tag tag = child.getValue(Tag.class);
                    tag.setKey(child.getKey());
                    tags.add(tag);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                binding.noTasksLayout.setVisibility(ds.exists() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getOverdueTasksQuery().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                binding.overdueTasksLayout.setVisibility(ds.exists() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getTodayTasksQuery().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                binding.todayTasksLayout.setVisibility(ds.exists() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getTomorrowTasksQuery().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                binding.tomorrowTasksLayout.setVisibility(ds.exists() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getInTheNextSevenDaysTasksQuery().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                binding.inTheNextSevenDaysTasksLayout.setVisibility(ds.exists() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getLaterTasksQuery().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                binding.laterTasksLayout.setVisibility(ds.exists() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        binding.tasksNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                // TODO: Set appBar elevation if scrolled
            }
        });

        createNewTaskTagChooserDialog(getContext());
        createTagsFilteringDialog(getContext());

        // TODO: Hide done tasks in the overdue view
        // Overdue Tasks
        CustomLinearLayoutManager overdueTasksLayoutManager = new CustomLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerViewTouchActionGuardManager overdueTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        overdueTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        overdueTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager overdueTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        SwipeableRealTimeTasksAdapter overdueTasksItemAdapter = new SwipeableRealTimeTasksAdapter(getOverdueTasksQuery(), false);
        overdueTasksItemAdapter.setEventListener(new TaskEventsListener());
        RecyclerView.Adapter overdueTasksWrappedAdapter = overdueTasksRecyclerViewSwipeManager.createWrappedAdapter(overdueTasksItemAdapter);
        GeneralItemAnimator overdueTasksAnimator = new SwipeDismissItemAnimator();
        overdueTasksAnimator.setSupportsChangeAnimations(false);
        binding.overdueTasksRecyclerView.setLayoutManager(overdueTasksLayoutManager);
        binding.overdueTasksRecyclerView.setAdapter(overdueTasksWrappedAdapter);
        binding.overdueTasksRecyclerView.setItemAnimator(overdueTasksAnimator);
        overdueTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.overdueTasksRecyclerView);
        overdueTasksRecyclerViewSwipeManager.attachRecyclerView(binding.overdueTasksRecyclerView);

        // Today Tasks
        CustomLinearLayoutManager todayTasksLayoutManager = new CustomLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerViewTouchActionGuardManager todayTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        todayTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        todayTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager todayTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        SwipeableRealTimeTasksAdapter todayTasksItemAdapter = new SwipeableRealTimeTasksAdapter(getTodayTasksQuery());
        todayTasksItemAdapter.setEventListener(new TaskEventsListener());
        RecyclerView.Adapter todayTasksWrappedAdapter = todayTasksRecyclerViewSwipeManager.createWrappedAdapter(todayTasksItemAdapter);
        GeneralItemAnimator todayTasksAnimator = new SwipeDismissItemAnimator();
        todayTasksAnimator.setSupportsChangeAnimations(false);
        binding.todayTasksRecyclerView.setLayoutManager(todayTasksLayoutManager);
        binding.todayTasksRecyclerView.setAdapter(todayTasksWrappedAdapter);
        binding.todayTasksRecyclerView.setItemAnimator(todayTasksAnimator);
        todayTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.todayTasksRecyclerView);
        todayTasksRecyclerViewSwipeManager.attachRecyclerView(binding.todayTasksRecyclerView);

        // Tomorrow Tasks
        CustomLinearLayoutManager tomorrowTasksLayoutManager = new CustomLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerViewTouchActionGuardManager tomorrowTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        tomorrowTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        tomorrowTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager tomorrowTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        SwipeableRealTimeTasksAdapter tomorrowTasksItemAdapter = new SwipeableRealTimeTasksAdapter(getTomorrowTasksQuery());
        tomorrowTasksItemAdapter.setEventListener(new TaskEventsListener());
        RecyclerView.Adapter tomorrowTasksWrappedAdapter = tomorrowTasksRecyclerViewSwipeManager.createWrappedAdapter(tomorrowTasksItemAdapter);
        GeneralItemAnimator tomorrowTasksAnimator = new SwipeDismissItemAnimator();
        tomorrowTasksAnimator.setSupportsChangeAnimations(false);
        binding.tomorrowTasksRecyclerView.setLayoutManager(tomorrowTasksLayoutManager);
        binding.tomorrowTasksRecyclerView.setAdapter(tomorrowTasksWrappedAdapter);
        binding.tomorrowTasksRecyclerView.setItemAnimator(tomorrowTasksAnimator);
        tomorrowTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.tomorrowTasksRecyclerView);
        tomorrowTasksRecyclerViewSwipeManager.attachRecyclerView(binding.tomorrowTasksRecyclerView);

        // InTheNextSevenDays Tasks
        CustomLinearLayoutManager inTheNextSevenDaysTasksLayoutManager = new CustomLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerViewTouchActionGuardManager inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager inTheNextSevenDaysTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        SwipeableRealTimeTasksAdapter inTheNextSevenDaysTasksItemAdapter = new SwipeableRealTimeTasksAdapter(getInTheNextSevenDaysTasksQuery());
        inTheNextSevenDaysTasksItemAdapter.setEventListener(new TaskEventsListener());
        RecyclerView.Adapter inTheNextSevenDaysTasksWrappedAdapter = inTheNextSevenDaysTasksRecyclerViewSwipeManager.createWrappedAdapter(inTheNextSevenDaysTasksItemAdapter);
        GeneralItemAnimator inTheNextSevenDaysTasksAnimator = new SwipeDismissItemAnimator();
        inTheNextSevenDaysTasksAnimator.setSupportsChangeAnimations(false);
        binding.inTheNextSevenDaysTasksRecyclerView.setLayoutManager(inTheNextSevenDaysTasksLayoutManager);
        binding.inTheNextSevenDaysTasksRecyclerView.setAdapter(inTheNextSevenDaysTasksWrappedAdapter);
        binding.inTheNextSevenDaysTasksRecyclerView.setItemAnimator(inTheNextSevenDaysTasksAnimator);
        inTheNextSevenDaysTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.inTheNextSevenDaysTasksRecyclerView);
        inTheNextSevenDaysTasksRecyclerViewSwipeManager.attachRecyclerView(binding.inTheNextSevenDaysTasksRecyclerView);

        // Later Tasks
        CustomLinearLayoutManager laterTasksLayoutManager = new CustomLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        RecyclerViewTouchActionGuardManager laterTasksRecyclerViewTouchActionGuardManager = new RecyclerViewTouchActionGuardManager();
        laterTasksRecyclerViewTouchActionGuardManager.setInterceptVerticalScrollingWhileAnimationRunning(true);
        laterTasksRecyclerViewTouchActionGuardManager.setEnabled(true);
        RecyclerViewSwipeManager laterTasksRecyclerViewSwipeManager = new RecyclerViewSwipeManager();
        SwipeableRealTimeTasksAdapter laterTasksItemAdapter = new SwipeableRealTimeTasksAdapter(getLaterTasksQuery());
        laterTasksItemAdapter.setEventListener(new TaskEventsListener());
        RecyclerView.Adapter laterTasksWrappedAdapter = laterTasksRecyclerViewSwipeManager.createWrappedAdapter(laterTasksItemAdapter);
        GeneralItemAnimator laterTasksAnimator = new SwipeDismissItemAnimator();
        laterTasksAnimator.setSupportsChangeAnimations(false);
        binding.laterTasksRecyclerView.setLayoutManager(laterTasksLayoutManager);
        binding.laterTasksRecyclerView.setAdapter(laterTasksWrappedAdapter);
        binding.laterTasksRecyclerView.setItemAnimator(laterTasksAnimator);
        laterTasksRecyclerViewTouchActionGuardManager.attachRecyclerView(binding.laterTasksRecyclerView);
        laterTasksRecyclerViewSwipeManager.attachRecyclerView(binding.laterTasksRecyclerView);

        // Setup TaskAdder
        binding.taskSecondaryText.setText((new Date()).toLocaleString());
        binding.taskAdderInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // TODO: implement SmartTaskAdder
                String text = editable.toString();
                if (!text.equals("")) {
                    newTask.setTitle(text);
                    newTask = new Taskify().run(text, newTask, tags);
                    renderNewTask();
                } else {
                    binding.previewTaskLayout.setVisibility(View.GONE);
                }
            }
        });
        binding.taskAdderInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_SEND) {
                    insertTask();
                    handled = true;
                }
                return handled;
            }
        });
        binding.newTaskClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destroyPreviewTask();
            }
        });
        binding.taskAdderSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertTask();
            }
        });
        binding.newTaskTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagChooserDialog.show();
            }
        });
        binding.newTaskChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar taskDueDate = Calendar.getInstance();
                taskDueDate.setTime(new Date(newTask.getDueDate()));
                int taskYear = taskDueDate.get(Calendar.YEAR);
                int taskMonth = taskDueDate.get(Calendar.MONTH);
                int taskDay = taskDueDate.get(Calendar.DAY_OF_MONTH);
                newTaskDueDatePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int month, int day) {
                                newTask.setDueDate(Tools.editDueDate(newTask.getDueDate(), year, month, day));
                                binding.setNewTask(newTask);

                            }
                        }, taskYear, taskMonth, taskDay);
                newTaskDueDatePickerDialog.show();
            }
        });
        binding.newTaskChooseTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar taskDueDate = Calendar.getInstance();
                taskDueDate.setTime(new Date(newTask.getDueDate()));
                int taskHour = taskDueDate.get(Calendar.HOUR_OF_DAY);
                int taskMinute = taskDueDate.get(Calendar.MINUTE);
                newTaskDueTimePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hour,
                                                  int minute) {
                                newTask.setDueDate(Tools.editDueDate(newTask.getDueDate(), hour, minute));
                                binding.setNewTask(newTask);

                            }
                        }, taskHour, taskMinute, DateFormat.is24HourFormat(getContext()));
                newTaskDueTimePickerDialog.show();
            }
        });

        showTargetedTasks(getArguments().getInt("targeted_tasks"));
        destroyPreviewTask();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        createNewTaskTagChooserDialog(getContext());
        if (resultCode == 1) {
            createTagsFilteringDialog(getContext());
            tagsFilteringDialog.show();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Main = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
        if (context instanceof MainActivity) {
            mainActivity = (MainActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        Tag tag = (Tag) v.getTag();
        if (getResources().getResourceName(v.getId()).equals("com.pierrejacquier.olim:id/tagEdit")) {
            launchTagActivity(tag.getKey());
            tagsFilteringDialog.dismiss();
        } else {
            if (tagsFilteringDialog.isShowing()) {
                tagsFilteringDialog.dismiss();
                Main.showFilterFragment(tag.getKey());
            } else if (tagChooserDialog.isShowing()) {
                tagChooserDialog.dismiss();
                newTask.setTag(tag);
                newTask.setTagKey(tag.getKey());
                renderNewTask();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    /**
     * Data handling methods
     */

    private Query getOverdueTasksQuery() {
        Calendar todayStart = Calendar.getInstance();
        Tools.setStartOfDay(todayStart);
        return tasksRef.orderByChild("dueDate")
                .endAt(todayStart.getTimeInMillis() - 1);
    }

    private Query getTodayTasksQuery() {
        Calendar todayStart = Calendar.getInstance();
        Calendar todayEnd = Calendar.getInstance();
        Tools.setStartOfDay(todayStart);
        Tools.setEndOfDay(todayEnd);
        return tasksRef.orderByChild("dueDate")
                .startAt(todayStart.getTimeInMillis())
                .endAt(todayEnd.getTimeInMillis());
    }

    private Query getTomorrowTasksQuery() {
        Calendar tomorrowStart = Calendar.getInstance();
        Calendar tomorrowEnd = Calendar.getInstance();
        tomorrowStart.add(Calendar.DAY_OF_MONTH, 1);
        tomorrowEnd.add(Calendar.DAY_OF_MONTH, 1);
        Tools.setStartOfDay(tomorrowStart);
        Tools.setEndOfDay(tomorrowEnd);
        return tasksRef.orderByChild("dueDate")
                .startAt(tomorrowStart.getTimeInMillis())
                .endAt(tomorrowEnd.getTimeInMillis());
    }

    private Query getInTheNextSevenDaysTasksQuery() {
        Calendar inTheSevenNextDaysStart = Calendar.getInstance();
        Calendar inTheSevenNextDaysEnd = Calendar.getInstance();
        inTheSevenNextDaysStart.add(Calendar.DAY_OF_MONTH, 2);
        inTheSevenNextDaysEnd.add(Calendar.DAY_OF_MONTH, 7);
        Tools.setStartOfDay(inTheSevenNextDaysStart);
        Tools.setEndOfDay(inTheSevenNextDaysEnd);
        return tasksRef.orderByChild("dueDate")
                .startAt(inTheSevenNextDaysStart.getTimeInMillis())
                .endAt(inTheSevenNextDaysEnd.getTimeInMillis());
    }

    private Query getLaterTasksQuery() {
        Calendar laterStart = Calendar.getInstance();
        laterStart.add(Calendar.DAY_OF_MONTH, 8);
        Tools.setStartOfDay(laterStart);
        return tasksRef.orderByChild("dueDate")
                .startAt(laterStart.getTimeInMillis());
    }

    private void insertTask() {
        Log.e("tag", newTask.toString());
        tasksRef.push().setValue(newTask.getMap());
        destroyPreviewTask();
    }

    private void postponeAllTheseTasks(Query tasksQuery) {
        tasksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    task.postponeToTheNextDay();
                    child.getRef().setValue(task.getMap());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void markAsDoneAllTheseTasks(Query tasksQuery) {
        tasksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    task.setDone(true);
                    child.getRef().setValue(task.getMap());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Layout handling methods
     */

    private void renderNewTask() {
        binding.previewTaskLayout.setVisibility(View.VISIBLE);
        binding.taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        binding.setNewTask(newTask);
        IconicsDrawable id = new IconicsDrawable(getContext()).sizeDp(20).color(Color.WHITE);
        if (newTask.getTag() == null) {
            int color = getContext().getResources().getColor(R.color.colorHintText);
            binding.newTaskTag.setBackgroundDrawable(Graphics.createRoundDrawable(color));
            binding.newTaskTag.setImageDrawable(id.icon("gmd_label_outline"));
        } else {
            binding.newTaskTag.setBackgroundDrawable(Graphics.createRoundDrawable(newTask.getTag().getColor()));
            binding.newTaskTag.setImageDrawable(id.icon(newTask.getTag().getIconicsName()));
        }
    }

    private void createTagsFilteringDialog(Context context) {
        tagsFilteringDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.filter_with_tag)
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    }
                })
                .negativeText(R.string.create_tag)
                .negativeColor(getResources().getColor(R.color.colorPrimaryText))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchTagActivity(null);
                        tagsFilteringDialog.dismiss();
                    }
                })
                .adapter(new TagsListAdapter(
                                getContext(),
                                tagsRef, this),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            }
                        })
                .build();
    }

    private void createNewTaskTagChooserDialog(Context context) {
        tagChooserDialog = new MaterialDialog.Builder(context)
                .title(R.string.change_tag)
                .autoDismiss(true)
                .positiveText(R.string.clear)
                .positiveColor(getResources().getColor(R.color.colorPrimary))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        newTask.setTag(null);
                        renderNewTask();
                    }
                })
                .negativeText(R.string.create_tag)
                .negativeColor(getResources().getColor(R.color.colorPrimaryText))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        launchTagActivity(null);
                    }
                })
                .adapter(new TagsListAdapter(context, tagsRef, this), null)
                .build();
    }

    public void hideTaskAdder() {
        binding.taskAdderCard.setVisibility(View.GONE);
    }

    public void showTaskAdder() {
        binding.taskAdderCard.setVisibility(View.VISIBLE);
    }

    private void destroyPreviewTask() {
        Tools.hideKeyboard(mainActivity);
        newTask = new Task();
        newTask.setDueDate(new Date().getTime());
        binding.previewTaskLayout.setVisibility(View.GONE);
        binding.taskAdderSendButton.setColorFilter(getResources().getColor(R.color.colorHintTextNonFaded), PorterDuff.Mode.SRC_IN);
        binding.taskPrimaryText.setText("");
        binding.taskAdderInput.setText("");
        binding.taskSecondaryText.setText(new Date().toLocaleString());
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    public void showTagsFilteringDialog() {
        tagsFilteringDialog.show();
    }

    public void showSnack(String text) {
        Snackbar.make(binding.tasksCoordinatorLayout, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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

    // TODO: show a no-task message if no tasks in the targeted view
    public void showTargetedTasks(int targetedTasks) {
        switch (targetedTasks) {
            case MainActivity.DRAWER_TASKS_OVERDUE:
                showOverdueTasks();
                break;
            case MainActivity.DRAWER_TASKS_TODAY:
                showTodayTasks();
                break;
            case MainActivity.DRAWER_TASKS_TOMORROW:
                showTomorrowTasks();
                break;
            case MainActivity.DRAWER_TASKS_IN_THE_NEXT_SEVEN_DAYS:
                showInTheNextSevenDaysTasks();
                break;
            case MainActivity.DRAWER_TASKS_ALL:
                showAllUpcomingTasks();
                break;
            case -1: break;
        }
    }
    
    public void hideAllTasks() {
        binding.overdueTasksLayout.setVisibility(View.GONE);
        binding.todayTasksLayout.setVisibility(View.GONE);
        binding.tomorrowTasksLayout.setVisibility(View.GONE);
        binding.inTheNextSevenDaysTasksLayout.setVisibility(View.GONE);
        binding.laterTasksLayout.setVisibility(View.GONE);
    }
    
    public void showAllUpcomingTasks() {
        binding.overdueTasksLayout.setVisibility(View.GONE);
        binding.todayTasksLayout.setVisibility(View.VISIBLE);
        binding.tomorrowTasksLayout.setVisibility(View.VISIBLE);
        binding.inTheNextSevenDaysTasksLayout.setVisibility(View.VISIBLE);
        binding.laterTasksLayout.setVisibility(View.VISIBLE);
    }

    public void showOverdueTasks() {
        hideAllTasks();
        binding.overdueTasksLayout.setVisibility(View.VISIBLE);
    }

    public void showTodayTasks() {
        hideAllTasks();
        binding.todayTasksLayout.setVisibility(View.VISIBLE);
    }

    public void showTomorrowTasks() {
        hideAllTasks();
        binding.tomorrowTasksLayout.setVisibility(View.VISIBLE);
        Log.e("aeiu", "nauie");
    }

    public void showInTheNextSevenDaysTasks() {
        hideAllTasks();
        binding.inTheNextSevenDaysTasksLayout.setVisibility(View.VISIBLE);
    }

    public void showLaterTasks() {
        hideAllTasks();
        binding.laterTasksLayout.setVisibility(View.VISIBLE);
    }

    public interface OnFragmentInteractionListener {
        void showFilterFragment(String tagKey);
    }

    private class TaskEventsListener implements SwipeableRealTimeTasksAdapter.EventListener {

        TaskEventsListener() {}

        @Override
        public void onDataAdded() {
            binding.progressView.setVisibility(View.GONE);
        }

        @Override
        public void onTaskPostponed(DatabaseReference taskRef, Task task) {
            task.postponeToTheNextDay();
            taskRef.setValue(task.getMap());
        }

        @Override
        public void onTaskClicked(DatabaseReference taskRef) {
            launchTaskActivity(taskRef.getKey());
        }

        @Override
        public void onTaskDoneToggleClicked(DatabaseReference taskRef, Task task, boolean doneStatus) {
            task.setDone(doneStatus);
            taskRef.setValue(task.getMap());
        }
    }
}

