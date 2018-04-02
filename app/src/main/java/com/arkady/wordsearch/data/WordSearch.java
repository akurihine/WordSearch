package com.arkady.wordsearch.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class WordSearch implements Serializable {

    @SerializedName("source_language") private String sourceLanguage;
    @SerializedName("word") private String word;
    @SerializedName("character_grid") private List<List<String>> characterGrid;
    @SerializedName("word_locations") private Map<String, String> wordLocations;
    @SerializedName("target_language") private String targetLanguage;

    public String getWord() {
        return word;
    }

    public List<List<String>> getCharacterGrid() {
        return characterGrid;
    }

    public Map<String, String> getWordLocations() {
        return wordLocations;
    }
}