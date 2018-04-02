package com.arkady.wordsearch.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.arkady.wordsearch.data.ApiClient;
import com.arkady.wordsearch.R;
import com.arkady.wordsearch.data.WordSearch;
import com.arkady.wordsearch.data.WordSearchApi;
import com.arkady.wordsearch.data.WordSearchesParser;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements WordSearchView.WordGridListener {
    private static final String WORD_SEARCHES_KEY = "wordSearches";
    private static final String POSITIONS_KEY = "position";

    @BindView(R.id.wordGrid) WordSearchView mWordSearchView;
    @BindView(R.id.title) TextView mTitle;

    private int mPosition = 0;
    private ArrayList<WordSearch> mWordSearches;
    private Spring mSpring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mWordSearchView.setListener(this);
        setUpTransitionSpring();

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(POSITIONS_KEY);
            mWordSearches = (ArrayList<WordSearch>) savedInstanceState.getSerializable(WORD_SEARCHES_KEY);
            showWordSearch(mWordSearches.get(mPosition));
        }

        if (mWordSearches == null || mWordSearches.isEmpty()) {
            loadWordSearches();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(WORD_SEARCHES_KEY, mWordSearches);
        outState.putInt(POSITIONS_KEY, mPosition);
        super.onSaveInstanceState(outState);
    }

    public void loadWordSearches() {
        ApiClient.getClient().create(WordSearchApi.class).getWordSearch().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                mWordSearches = WordSearchesParser.parse(response.body());
                showWordSearch(mWordSearches.get(mPosition));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Ooops! Something went wrong.", Toast.LENGTH_LONG);
            }
        });
    }

    private void showWordSearch(WordSearch wordSearch) {
        mTitle.setText(wordSearch.getWord());
        mWordSearchView.setData(wordSearch);
    }

    @Override
    public void onWordFound(WordSearch wordSearch, String word, boolean isLastWord) {
        if (isLastWord) {
            mWordSearchView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPosition = (mPosition + 1) % mWordSearches.size(); //Increase position and keep it looping
                    mSpring.setCurrentValue(1);
                    mSpring.setEndValue(0);
                    showWordSearch(mWordSearches.get(mPosition));
                }
            }, 650);
        }
    }

    private void setUpTransitionSpring() {
        SpringSystem springSystem = SpringSystem.create();
        mSpring = springSystem.createSpring();
        mSpring.addListener(new SimpleSpringListener() {

            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();
                float scale = 1f - (value * 0.5f);
                mWordSearchView.setScaleX(scale);
                mWordSearchView.setScaleY(scale);
            }
        });

        mSpring.setCurrentValue(0);
    }
}
