package com.neteru.tixtat.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neteru.tixtat.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class DocumentationFragment extends Fragment {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_documentation, container, false);
        webView = root.findViewById(R.id.webView);

        if (getActivity() != null) {
            FloatingActionButton scrollToTop = getActivity().findViewById(R.id.fab_1);

            scrollToTop.setOnClickListener(view -> webView.scrollTo(0,0));

        }

        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl("file:///android_asset/course_"+getString(R.string.lang)+".html");

        webView.getSettings().setDefaultTextEncodingName("utf-8");

        return root;
    }

}
