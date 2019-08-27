package com.microtechmd.pda_app.fragment;

import android.support.v4.app.Fragment;

import com.microtechmd.pda.library.utility.LogPDA;


public class FragmentBase extends Fragment {
    protected LogPDA mLog = null;

    public FragmentBase() {
        mLog = new LogPDA();
    }
}