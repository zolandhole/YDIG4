package com.surampaksakosoy.ydig4.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.surampaksakosoy.ydig4.R;

import java.util.Objects;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private Button buttonProfileLogout;
    private ListenerProfile listener;

    public interface ListenerProfile{
        void inputProfile(CharSequence input);
    }

    public ProfileFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ListenerProfile) {
            listener = (ListenerProfile) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ListenerLogFragments");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_profilei, container,false);
        buttonProfileLogout = view.findViewById(R.id.buttonProfileLogout);
        buttonProfileLogout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonProfileLogout) {
            listener.inputProfile("logout");
        }
    }
}
