package com.arkady.wordsearch.data;


import com.google.gson.Gson;

import java.util.ArrayList;

public class WordSearchesParser {
    /**
     * This method expects each line in the string to be a JSON object.
     * It will split the string at new lines.
     * Once split, it assumes each JSON string can be converted to a WordSearch object.
     * @param string
     * @return list of WordSearch, or empty list if string is null
     */
    public static ArrayList<WordSearch> parse(String string) {
        ArrayList<WordSearch> wordSearches = new ArrayList<>();

        if (string != null) {
            Gson gson = new Gson();
            String[] split = string.split("\\n");

            for (String jsonString : split) {
                WordSearch wordSearch = gson.fromJson(jsonString, WordSearch.class);
                wordSearches.add(wordSearch);
            }
        }

        return wordSearches;
    }
}
