package com.beneville.grandfatherclock.fragments;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.beneville.grandfatherclock.R;
import com.beneville.grandfatherclock.adapters.LibraryListAdapter;
import com.beneville.grandfatherclock.database.AppDatabase;
import com.beneville.grandfatherclock.database.Song;
import com.beneville.grandfatherclock.library_items.ListItem;
import com.beneville.grandfatherclock.library_items.MediaInfo;
import com.beneville.grandfatherclock.library_items.MenuInfo;
import com.beneville.grandfatherclock.views.SideSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by joeja on 11/7/2017.
 */

public class LibraryFragment extends Fragment {

    TextView mTitle;

    private RecyclerView itemsView;
    private SideSelector selector;
    private LibraryListAdapter adapter;
    private AppDatabase mDatabase;
    private EditText searchText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        mTitle = view.findViewById(R.id.title);
        itemsView = view.findViewById(R.id.library_items);
        selector = view.findViewById(R.id.side_selector);
        searchText = view.findViewById(R.id.search_text);
        adapter = new LibraryListAdapter();
        itemsView.setAdapter(adapter);
        itemsView.setHasFixedSize(true);
        itemsView.setLayoutManager(new LinearLayoutManager(getContext()));

        itemsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visiblePos = ((LinearLayoutManager) itemsView.getLayoutManager()).findFirstVisibleItemPosition();
                int section = adapter.getSectionForPosition(visiblePos);
                selector.setCurrentSection(section);
            }
        });

        selector.setView(itemsView);

        mDatabase = Room.databaseBuilder(getContext().getApplicationContext(),
                AppDatabase.class, "database-name").build();

        view.findViewById(R.id.back_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        Bundle bundle = getArguments();
        if(bundle != null) {
            Song.ModeType mode = (Song.ModeType) bundle.getSerializable("mode");
            if(mode != null) {
                String title = bundle.getString("title");
                showCategoryList(title, mode);
            } else {
                showSearchResults("");

                ((EditText) view.findViewById(R.id.search_text)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        searchSongs(((EditText) getView().findViewById(R.id.search_text)).getText().toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {}
                });
            }
        } else {
            showLibraryMenu();
        }

        return view;
    }

    public void onClickCategory(String title, Song.ModeType mode) {
        Fragment fragment = new LibraryFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("mode", mode);
        bundle.putString("title", title);
        fragment.setArguments(bundle);

        BaseFragment.startFragment(getContext(), fragment);
    }

    public void showLibraryMenu() {
        ArrayList<ListItem> data = new ArrayList<>();
        mTitle.setText("LIBRARY");

        data.add(new MenuInfo(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCategory("ALL", Song.ModeType.ALL);
            }
        }, "All"));
        data.add(new MenuInfo(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCategory("MUSIC & MUSICALS", Song.ModeType.MOVIE);
            }
        }, "Music & Musicals"));
        data.add(new MenuInfo(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCategory("MOVIES & TELEVISION", Song.ModeType.SONG);
            }
        }, "Movies & Television"));
        data.add(new MenuInfo(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCategory("HISTORY & LITERATURE", Song.ModeType.BOOK);
            }
        }, "History & Literature"));

        selector.setVisibility(View.GONE);
        adapter.setListItems(data);
    }

    public void showCategoryList(String title, Song.ModeType mode) {
        mTitle.setText(title);
        selector.setVisibility(View.VISIBLE);

        getSongs(mode);
    }

    public void showSearchResults(String search) {
        mTitle.setText("Search");
        selector.setVisibility(View.GONE);
        searchText.setVisibility(View.VISIBLE);
        searchText.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        searchSongs(search);
    }

    private void getSongs(final Song.ModeType mode) {
        new AsyncTask<Void, Void, List<Song>>() {
            @Override
            protected List<Song> doInBackground(Void... params) {
                final Song.Dao songDao = mDatabase.songDao();
                if(mode != Song.ModeType.ALL) {
                    return songDao.getAllByType(mode);
                } else {
                    return songDao.getAll();
                }
            }

            @Override
            protected void onPostExecute(List<Song> songs) {
                adapter.clearList();
                for (Song song : songs) {
                    adapter.addListItem(new MediaInfo(song));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private void searchSongs(final String text) {
        new AsyncTask<Void, Void, List<Song>>() {
            @Override
            protected List<Song> doInBackground(Void... params) {
                final Song.Dao songDao = mDatabase.songDao();
                    return songDao.search("%"+text+"%");
            }

            @Override
            protected void onPostExecute(List<Song> songs) {
                adapter.clearList();
                for (Song song : songs) {
                    adapter.addListItem(new MediaInfo(song));
                }
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

}