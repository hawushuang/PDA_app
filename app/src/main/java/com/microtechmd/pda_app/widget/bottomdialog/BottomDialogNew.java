package com.microtechmd.pda_app.widget.bottomdialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microtechmd.pda_app.R;
import com.microtechmd.pda_app.widget.myswitchbutton.LukeSwitchButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BottomDialogNew {
    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;
    public static final int VERTICAL = OrientationHelper.VERTICAL;

    public static final int LINEAR = 0;
    public static final int GRID = 1;

    private CustomDialog customDialog;

    public BottomDialogNew(Context context) {
        customDialog = new CustomDialog(context);
    }

    public BottomDialogNew title(String title) {
        customDialog.title(title);
        return this;
    }

    public BottomDialogNew title(int title) {
        customDialog.title(title);
        return this;
    }

    public BottomDialogNew background(int res) {
        customDialog.background(res);
        return this;
    }

    public BottomDialogNew layout(int layout) {
        customDialog.layout(layout);
        return this;
    }

    public BottomDialogNew size(int size) {
        customDialog.size(size);
        return this;
    }

    public BottomDialogNew orientation(int orientation) {
        customDialog.orientation(orientation);
        return this;
    }

    public BottomDialogNew addItems(List<Item> items, OnItemClickListener onItemClickListener) {
        customDialog.addItems(items, onItemClickListener);
        return this;
    }

    /**
     * @deprecated
     */
    public BottomDialogNew itemClick(OnItemClickListener listener) {
        customDialog.setItemClick(listener);
        return this;
    }

    public void show() {
        customDialog.show();
    }

    private final class CustomDialog extends Dialog {
        private LinearLayout background;
        private LinearLayout container;
        private TextView titleView;

        private DialogAdapter adapter;

        private int padding;
        private int topPadding;
        private int leftPadding;
        private int topIcon;
        private int leftIcon;

        private int orientation;
        private int layout;
        private int size;

        private boolean iconFlag = true;

        CustomDialog(Context context) {
            super(context, R.style.BottomDialog);

            init();
        }

        private void init() {
            padding = getContext().getResources().getDimensionPixelSize(R.dimen.app_normal_margin);
            topPadding = getContext().getResources().getDimensionPixelSize(R.dimen.app_tiny_margin);
            leftPadding = getContext().getResources().getDimensionPixelSize(R.dimen.app_normal_margin);
            topIcon = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_dialog_top_icon);
            leftIcon = getContext().getResources().getDimensionPixelSize(R.dimen.bottom_dialog_left_icon);
            size = 3;

            setContentView(R.layout.bottom_dialog_new);
            setCancelable(true);
            setCanceledOnTouchOutside(true);
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

            background = (LinearLayout) findViewById(R.id.background);
            titleView = (TextView) findViewById(R.id.title);
            container = (LinearLayout) findViewById(R.id.container);
            findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            LukeSwitchButton icon_switch = findViewById(R.id.icon_switch);
            icon_switch.setOnToggleChanged(new LukeSwitchButton.OnToggleChanged() {
                @Override
                public void onToggle(boolean on) {
                    iconFlag = on;
                    adapter.notifyDataSetChanged();
                }
            });
        }

        void addItems(List<Item> items, OnItemClickListener onItemClickListener) {
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RecyclerView.LayoutManager manager;

            adapter = new DialogAdapter(items, layout, orientation);
            adapter.setItemClick(onItemClickListener);

            if (layout == LINEAR)
                manager = new LinearLayoutManager(getContext(), orientation, false);
            else if (layout == GRID)
                manager = new GridLayoutManager(getContext(), size, orientation, false);
            else manager = new LinearLayoutManager(getContext(), orientation, false);

            RecyclerView recyclerView = new RecyclerView(getContext());
            recyclerView.setLayoutParams(params);
            recyclerView.setLayoutManager(manager);
            recyclerView.setAdapter(adapter);

            container.addView(recyclerView);
        }

        public void title(int title) {
            title(getContext().getString(title));
        }

        public void title(String title) {
            titleView.setText(title);
            titleView.setVisibility(View.VISIBLE);
        }

        public void layout(int layout) {
            this.layout = layout;
            if (adapter != null) adapter.setLayout(layout);
        }

        public void size(int size) {
            this.size = size;
        }

        public void orientation(int orientation) {
            this.orientation = orientation;
            if (adapter != null) adapter.setOrientation(orientation);
        }

        public void background(int res) {
            background.setBackgroundResource(res);
        }

        void setItemClick(OnItemClickListener onItemClickListener) {
            adapter.setItemClick(onItemClickListener);
        }

        /**
         * recycler view adapter, provide HORIZONTAL and VERTICAL item style
         */
        private class DialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private List<Item> mItems = Collections.emptyList();
            private OnItemClickListener itemClickListener;

            private int orientation;
            private int layout;

            DialogAdapter(List<Item> mItems, int layout, int orientation) {
                setList(mItems);
                this.layout = layout;
                this.orientation = orientation;
            }

            private void setList(List<Item> items) {
                mItems = items == null ? new ArrayList<Item>() : items;
            }

            void setItemClick(OnItemClickListener onItemClickListener) {
                this.itemClickListener = onItemClickListener;
            }

            public void setOrientation(int orientation) {
                this.orientation = orientation;
                notifyDataSetChanged();
            }

            public void setLayout(int layout) {
                this.layout = layout;
                notifyDataSetChanged();
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                if (layout == GRID)
                    return new TopHolder(new LinearLayout(parent.getContext()));
                else if (orientation == HORIZONTAL)
                    return new TopHolder(new LinearLayout(parent.getContext()));
                else return new LeftHolder(new LinearLayout(parent.getContext()));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                final Item item = mItems.get(position);

                TopHolder topHolder;
                LeftHolder leftHolder;

                if (layout == GRID) {
                    topHolder = (TopHolder) holder;
                    TextView tv_name = topHolder.item.findViewById(R.id.tv_name);
                    tv_name.setText(item.getTitle());
                    ImageView iv_draw_bg = topHolder.item.findViewById(R.id.iv_draw_bg);
                    iv_draw_bg.setBackground(item.getIcon());

                    LukeSwitchButton sub_switch = topHolder.item.findViewById(R.id.icon_sub_switch);
                    if (iconFlag) {
                        sub_switch.toggleOn();
                    } else {
                        sub_switch.toggleOff();
                    }

                    iv_draw_bg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                            if (itemClickListener != null) itemClickListener.click(item);
                        }
                    });
                } else if (orientation == HORIZONTAL) {
                    topHolder = (TopHolder) holder;

//                    topHolder.item.setText(item.getTitle());
//                    topHolder.item.setCompoundDrawablesWithIntrinsicBounds(null, topHolder.icon(item.getIcon()), null, null);
                    topHolder.item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                            if (itemClickListener != null) itemClickListener.click(item);
                        }
                    });
                } else {
                    leftHolder = (LeftHolder) holder;

                    leftHolder.item.setText(item.getTitle());
                    leftHolder.item.setCompoundDrawablesWithIntrinsicBounds(leftHolder.icon(item.getIcon()), null, null, null);
                    leftHolder.item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dismiss();
                            if (itemClickListener != null) itemClickListener.click(item);
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return mItems.size();
            }

            /**
             * horizontal item adapter
             */
            class TopHolder extends RecyclerView.ViewHolder {
                private View item;

                TopHolder(View view) {
                    super(view);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.width = Utils.getScreenWidth(getContext()) / size;

                    item = LayoutInflater.from(view.getContext()).inflate(R.layout.bottom_dialog_new_item, null);
                    item.setLayoutParams(params);
                    item.setPadding(0, padding, 0, padding);

                    TypedValue typedValue = new TypedValue();
                    view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
                    item.setBackgroundResource(typedValue.resourceId);

                    ((LinearLayout) view).addView(item);
                }
            }

            class LeftHolder extends RecyclerView.ViewHolder {
                private TextView item;

                LeftHolder(View view) {
                    super(view);

                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    view.setLayoutParams(params);
                    item = new TextView(view.getContext());
                    item.setLayoutParams(params);
                    item.setMaxLines(1);
                    item.setEllipsize(TextUtils.TruncateAt.END);
                    item.setGravity(Gravity.CENTER_VERTICAL);
                    item.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
                    item.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.font_normal));
                    item.setCompoundDrawablePadding(leftPadding);
                    item.setPadding(padding, padding, padding, padding);

                    TypedValue typedValue = new TypedValue();
                    view.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true);
                    item.setBackgroundResource(typedValue.resourceId);

                    ((LinearLayout) view).addView(item);
                }

                private Drawable icon(Drawable drawable) {
                    if (drawable != null) {
                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                        @SuppressWarnings("SuspiciousNameCombination") Drawable resizeIcon = new BitmapDrawable(getContext().getResources(), Bitmap.createScaledBitmap(bitmap, leftIcon, leftIcon, true));
                        Drawable.ConstantState state = resizeIcon.getConstantState();
                        resizeIcon = DrawableCompat.wrap(state == null ? resizeIcon : state.newDrawable().mutate());
                        return resizeIcon;
                    }
                    return null;
                }
            }
        }
    }
}
