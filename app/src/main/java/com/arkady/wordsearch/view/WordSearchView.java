package com.arkady.wordsearch.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.arkady.wordsearch.R;
import com.arkady.wordsearch.data.WordSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WordSearchView extends View {
    private static final String FOUND_CELLS_KEY = "foundCells";
    private static final String FOUND_WORDS_KEY = "foundWords";
    private static final String SUPER_STATE_KEY = "superState";

    private int mNumColumns, mNumRows;
    private int mCellWidth, mCellHeight;
    private RectF mDrawBounds = new RectF();
    private Paint mWhiteTextPaint = new Paint();
    private Paint mBlackTextPaint = new Paint();
    private Paint mGreenPaint = new Paint();
    private Paint mGreyPaint = new Paint();
    private int mStartRow = -1;
    private int mStartCol = -1;

    private WordSearch mWordSearch;
    private boolean[][] mSelectedCells;
    private boolean[][] mFoundCells;
    private WordGridListener mListener;
    private HashMap<String, String> mFoundWords = new HashMap<>();

    public interface WordGridListener {
        void onWordFound(WordSearch wordSearch, String word, boolean isLastWord);
    }

    public WordSearchView(Context context) {
        super(context);
        init();
    }

    public WordSearchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WordSearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WordSearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mBlackTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mGreenPaint.setColor(ContextCompat.getColor(getContext(), R.color.found_highlight_color));
        mGreyPaint.setColor(ContextCompat.getColor(getContext(), R.color.selected_highlight_color));
        mWhiteTextPaint.setColor(Color.WHITE);
        mWhiteTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
    }

    public void setData(WordSearch wordSearch) {
        mWordSearch = wordSearch;
        mNumRows = wordSearch.getCharacterGrid().size();
        mNumColumns = wordSearch.getCharacterGrid().get(0).size();
        mSelectedCells = new boolean[mNumRows][mNumColumns];
        mFoundCells = new boolean[mNumRows][mNumColumns];
        mFoundWords.clear();
        calculateCellDimensions();
    }

    /**
     * Creates a centered square as the bounds for drawing.
     */
    private void adjustDrawBounds(int w, int h) {
        int smallestDimension = Math.min(w, h);
        float left = (w - smallestDimension) / 2f;
        float top = (h - smallestDimension) / 2f;
        float right = left + smallestDimension;
        float bottom = top + smallestDimension;

        mDrawBounds = new RectF(left, top, right, bottom);
    }

    private void calculateCellDimensions() {
        if (mNumColumns > 0 && mNumRows > 0) {
            mCellWidth = (int) ((mDrawBounds.right - mDrawBounds.left) / mNumColumns);
            mCellHeight = (int) ((mDrawBounds.bottom - mDrawBounds.top) / mNumRows);

            mBlackTextPaint.setTextSize(mCellWidth /2);
            mWhiteTextPaint.setTextSize(mCellWidth /2);
            invalidate();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState());
        bundle.putSerializable(FOUND_CELLS_KEY, mFoundCells);
        bundle.putSerializable(FOUND_WORDS_KEY, mFoundWords);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mFoundCells = (boolean[][]) bundle.getSerializable(FOUND_CELLS_KEY);
            mFoundWords = (HashMap<String, String>) bundle.getSerializable(FOUND_WORDS_KEY);
            state = bundle.getParcelable(SUPER_STATE_KEY);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        adjustDrawBounds(w, h);
        calculateCellDimensions();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);

        if (isReadyForDraw()) {
            for (int row = 0; row < mNumRows; row++) {
                List<String> rowOfLetters = mWordSearch.getCharacterGrid().get(row);
                for (int col = 0; col < mNumColumns; col++) {
                    drawCell(canvas, row, col, rowOfLetters.get(col));
                }
            }

            drawGridLines(canvas);
        }
    }

    public boolean isReadyForDraw() {
        return mNumColumns > 0 && mNumRows > 0 && mCellWidth > 0 && mCellHeight > 0 && mWordSearch != null;
    }

    private void drawCell(Canvas canvas, int row, int col, String letter) {
        RectF cellRect = getCellRect(row, col);

        if (mFoundCells[row][col]) {
            canvas.drawRect(cellRect, mGreenPaint);
        }

        if (mSelectedCells[row][col]) {
            canvas.drawRect(cellRect, mGreyPaint);
        }

        float textOffsetY = mCellHeight / 2 - (mBlackTextPaint.descent() + mBlackTextPaint.ascent()) / 2;
        float textX =  cellRect.left + getXOffsetToCenterText(letter);
        float textY = cellRect.top + textOffsetY;

        if (mFoundCells[row][col]) {
            canvas.drawText(letter, textX, textY, mWhiteTextPaint);
        } else {
            canvas.drawText(letter, textX, textY, mBlackTextPaint);
        }
    }

    private void drawGridLines(Canvas canvas) {
        for (int row = 1; row <= mNumRows - 1; row++) {
            float y = (row * mCellHeight) + mDrawBounds.top;
            canvas.drawLine(mDrawBounds.left, y, mDrawBounds.right, y, mGreyPaint);
        }

        for (int col = 1; col <= mNumColumns - 1; col++) {
            float x = (col * mCellWidth) + mDrawBounds.left;
            canvas.drawLine(x, mDrawBounds.top, x, mDrawBounds.bottom, mGreyPaint);
        }
    }

    private RectF getCellRect(int row, int col) {
        float left = (col * mCellWidth) + mDrawBounds.left;
        float top = (row * mCellHeight) + mDrawBounds.top;
        float right = ((col + 1) * mCellWidth) + mDrawBounds.left;
        float bottom = ((row + 1) * mCellHeight) + mDrawBounds.top;

        return new RectF(left, top, right, bottom);
    }

    public int getXOffsetToCenterText(String text) {
        float textWidth = mBlackTextPaint.measureText(text);
        int xOffset = (int)((mCellWidth -textWidth)/2f);
        return xOffset;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isReadyForDraw()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mStartCol = getColumnAtX(event.getX());
                mStartRow = getRowAtY(event.getY());
                mSelectedCells[mStartRow][mStartCol] = true;

            } else if (event.getAction() == MotionEvent.ACTION_MOVE && mStartRow >= 0 && mStartCol >= 0) {
                clearSelected();
                int col = getColumnAtX(event.getX());
                int row = getRowAtY(event.getY());

                setCellsInRangeAsSelected(mStartRow, mStartCol, row, col);
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                addFoundWordIfSelected();
                clearSelected();
                invalidate();
            }
            return true;
        }

        return false;
    }

    private void setCellsInRangeAsSelected(int startRow, int startCol, int endRow, int endCol) {
        int rowDiff = Math.abs(startRow - endRow);
        int colDiff = Math.abs(startCol - endCol);
        int lagerDiff = Math.max(rowDiff, colDiff);
        boolean selectionIsDiagonal = rowDiff != 0 && colDiff != 0;

        // If diagonal, transfer the larger range to the smaller. This will force the
        // diagonal selection range to be a square based on the largest dimension.
        if (selectionIsDiagonal) {
            boolean selectionDirectionIsUp = startCol - endCol > 0;
            boolean selectionDirectionIsLeft = startRow - endRow > 0;

            if (selectionDirectionIsUp) {
                endCol = startCol - lagerDiff;
            } else {
                endCol = startCol + lagerDiff;
            }

            if (selectionDirectionIsLeft) {
                endRow = startRow - lagerDiff;
            } else {
                endRow = startRow + lagerDiff;
            }

            endCol = Math.min(endCol, mNumColumns - 1);
            endCol = Math.max(endCol, 0);

            endRow = Math.min(endRow, mNumRows - 1);
            endRow = Math.max(endRow, 0);
        }

        int lowerBoundRow = Math.min(startRow, endRow);
        int upperBoundRow = Math.max(startRow, endRow);

        int lowerBoundCol = Math.min(startCol, endCol);
        int upperBoundCol = Math.max(startCol, endCol);

        for (int row = lowerBoundRow; row <= upperBoundRow; row++) {
            for (int col = lowerBoundCol; col <= upperBoundCol; col++) {
                //if diagonal make sure to only set diagonal cells true in the range
                //if selection is a straight line (not diagonal) then set all true in the range
                if (!selectionIsDiagonal || Math.abs(startCol - col) == Math.abs(startRow - row)) {
                    mSelectedCells[row][col] = true;
                }
            }
        }
    }

    private int getColumnAtX(float x) {
        int rawCol = (int) (x - mDrawBounds.left) / mCellWidth;
        return Math.min(mNumColumns - 1, Math.max(0, rawCol));
    }

    private int getRowAtY(float y) {
        int rawRow = (int) (y - mDrawBounds.top) / mCellWidth;
        return Math.min(mNumRows - 1, Math.max(0, rawRow));
    }

    private void clearSelected() {
        for (int i = 0; i < mSelectedCells.length; i++) {
            for (int j = 0; j < mSelectedCells[i].length; j++) {
                mSelectedCells[i][j] = false;
            }
        }
    }

    private void addFoundWordIfSelected() {
        List<Pair<Integer, Integer>> selectedCells = getSelectedCells();
        String selectedString = createCellsKeyString(selectedCells);

        if (mWordSearch.getWordLocations().containsKey(selectedString)) {
            for (Pair<Integer, Integer> cell : selectedCells) {
                mFoundCells[cell.second][cell.first] = true;
            }

            mFoundWords.put(selectedString, mWordSearch.getWordLocations().get(selectedString));

            if (mListener != null) {
                boolean isLastWord = mFoundWords.size() >= mWordSearch.getWordLocations().size();
                mListener.onWordFound(mWordSearch, mWordSearch.getWordLocations().get(selectedString), isLastWord);
            }
        }
    }

    private String createCellsKeyString(List<Pair<Integer, Integer>> cells) {
        String cellKeyString = "";
        for (Pair<Integer, Integer> cellPair : cells) {
            if (cellKeyString.isEmpty()) {
                cellKeyString += cellPair.first + "," + cellPair.second;
            } else {
                cellKeyString += "," + cellPair.first  + "," + cellPair.second;
            }
        }
        return cellKeyString;
    }

    private List<Pair<Integer,Integer>> getSelectedCells() {
        List<Pair<Integer, Integer>> selectedCells = new ArrayList<>();
        for (int row = 0; row < this.mSelectedCells.length; row++) {
            for (int col = 0; col < this.mSelectedCells[row].length; col++) {
                if (this.mSelectedCells[row][col] == true) {
                    selectedCells.add(new Pair<Integer, Integer>(col, row));
                }
            }
        }
        return selectedCells;
    }

    public void setListener(WordGridListener listener) {
        mListener = listener;
    }
}
