package com.neteru.tixtat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neteru.tixtat.R;
import com.neteru.tixtat.classes.adapters.BackupsAdapter;
import com.neteru.tixtat.classes.databases.DB_Manager;
import com.neteru.tixtat.classes.models.Backups;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class BackupsFragment extends Fragment {

    private DB_Manager db_manager;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noDataMsg;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_backups, container, false);

        db_manager = new DB_Manager(getContext());

        noDataMsg = root.findViewById(R.id.noDataMsg);
        recyclerView = root.findViewById(R.id.recycler);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);

        swipeRefreshLayout.setColorSchemeResources(R.color.gray);
        swipeRefreshLayout.setOnRefreshListener(this::getBackups);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getBackups();

        return root;
    }

    private void getBackups(){
        List<Backups> backupsList = db_manager.db_readAllBackups();

        if (backupsList.size() != 0){
            noDataMsg.setVisibility(View.GONE);
        }else {
            noDataMsg.setVisibility(View.VISIBLE);
        }

        BackupsAdapter adapter = new BackupsAdapter(backupsList, R.layout.backups_model, getContext(), backups -> {

            db_manager.db_removeBackup(backups);
            getBackups();

        });
        adapter.notifyDataSetChanged();

        recyclerView.setAdapter(adapter);

        if (swipeRefreshLayout.isRefreshing()){swipeRefreshLayout.setRefreshing(false);}
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (getActivity() != null) {
            getActivity().getMenuInflater().inflate(R.menu.refresh, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_refresh) {
            getBackups();
        }

        return super.onOptionsItemSelected(item);
    }
}
