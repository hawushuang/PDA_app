package com.microtechmd.pda_app.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.microtechmd.pda_app.R;


public class FragmentInput extends FragmentBase {
    public static final int POSITION_CENTER = 0;
    public static final int POSITION_LEFT = 1;
    public static final int POSITION_RIGHT = 2;
    public static final int COUNT_POSITION = 3;

    private String mComment = null;
    private String mSeparatorText[] = null;
    private String mInputText[] = null;
    private int mInputWidth[] = null;
    private int mInputType[] = null;


    public FragmentInput() {
        mInputText = new String[COUNT_POSITION];
        mInputWidth = new int[COUNT_POSITION];
        mInputType = new int[COUNT_POSITION];
        mSeparatorText = new String[COUNT_POSITION];

        for (int position = 0; position < COUNT_POSITION; position++) {
            mInputText[position] = null;
            mInputWidth[position] = 0;
            mInputType[position] = -1;
            mSeparatorText[position] = null;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input, container, false);

        setComment(view, mComment);

        for (int position = 0; position < COUNT_POSITION; position++) {
            setInputText(view, position, mInputText[position]);
            setInputWidth(view, position, mInputWidth[position]);
            setInputType(view, position, mInputType[position]);

            if (position != POSITION_CENTER) {
                setSeparatorText(view, position, mSeparatorText[position]);
            }
        }

        return view;
    }

    public String getComment() {
        View view = getView();

        if (view != null) {
            mComment =
                    ((TextView) view.findViewById(R.id.text_view_comment)).getText().toString();
        }

        return mComment;
    }


    public String getInputText(int position) {
        if (position < COUNT_POSITION) {
            View view = getView();

            if (view != null) {
                mInputText[position] =
                        getEditText(view, position).getText().toString();
            }

            return mInputText[position];
        }

        return null;
    }


    public String getSeparatorText(int position) {
        if ((position == POSITION_LEFT) || (position == POSITION_RIGHT)) {
            View view = getView();

            if (view != null) {
                mSeparatorText[position] =
                        getTextView(view, position).getText().toString();
            }
            return mSeparatorText[position];
        }

        return null;
    }


    public void setComment(String comment) {
        mComment = comment;
        View view = getView();

        if (view != null) {
            setComment(view, comment);
        }
    }


    public void setInputText(int position, String text) {
        if (position < COUNT_POSITION) {
            mInputText[position] = text;
            View view = getView();

            if (view != null) {
                setInputText(view, position, text);
            }
        }
    }


    public void setInputWidth(int position, int width) {
        if (position < COUNT_POSITION) {
            mInputWidth[position] = width;
            View view = getView();

            if (view != null) {
                setInputWidth(view, position, width);
            }
        }
    }


    public void setInputType(int position, int type) {
        if (position < COUNT_POSITION) {
            mInputType[position] = type;
            View view = getView();

            if (view != null) {
                setInputType(view, position, type);
            }
        }
    }


    public void setSeparatorText(int position, String text) {
        if ((position == POSITION_LEFT) || (position == POSITION_RIGHT)) {
            mSeparatorText[position] = text;
            View view = getView();

            if (view != null) {
                setSeparatorText(view, position, text);
            }
        }
    }


    private void setComment(View view, String comment) {
        TextView textViewComment =
                (TextView) view.findViewById(R.id.text_view_comment);

        if (comment != null) {
            textViewComment.setVisibility(View.VISIBLE);
            textViewComment.setText(comment);
        } else {
            textViewComment.setVisibility(View.GONE);
        }
    }


    private void setInputText(View view, int position, String text) {
        EditText editText = getEditText(view, position);

        if (editText == null) {
            return;
        }

        if (text == null) {
            editText.setVisibility(View.GONE);
        } else {
            editText.setVisibility(View.VISIBLE);
            editText.setText(text);
        }
    }


    private void setInputWidth(View view, int position, int width) {
        EditText editText = getEditText(view, position);

        if (editText == null) {
            return;
        }

        if (width > 0) {
            editText.getLayoutParams().width = width;
        }
    }


    private void setInputType(View view, int position, int type) {
        EditText editText = getEditText(view, position);

        if (editText == null) {
            return;
        }

        if (type >= 0) {
            editText.setInputType(type);
        }
    }


    private EditText getEditText(View view, int position) {
        EditText editText = null;

        switch (position) {
            case POSITION_CENTER:
                editText =
                        (EditText) view.findViewById(R.id.edit_text_input_center);
                break;

            case POSITION_LEFT:
                editText =
                        (EditText) view.findViewById(R.id.edit_text_input_left);
                break;

            case POSITION_RIGHT:
                editText =
                        (EditText) view.findViewById(R.id.edit_text_input_right);
                break;

            default:
                break;
        }

        return editText;
    }


    private void setSeparatorText(View view, int position, String text) {
        TextView textView = getTextView(view, position);

        if (textView == null) {
            return;
        }

        if (text == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }


    private TextView getTextView(View view, int position) {
        TextView textView = null;

        switch (position) {
            case POSITION_LEFT:
                textView =
                        (TextView) view.findViewById(R.id.text_view_separator_left);
                break;

            case POSITION_RIGHT:
                textView =
                        (TextView) view.findViewById(R.id.text_view_separator_right);
                break;

            default:
                break;
        }

        return textView;
    }
}
