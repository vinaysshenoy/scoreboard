package com.vinaysshenoy.scoreboard.widget.playerscoreview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by vinaysshenoy on 14/1/17.
 */

public class PlayerScoreView extends View {

    private static final int DEFAULT_POINTS_PER_ROUND = 30;
    private static final float DEFAULT_TRACK_STROKE_WIDTH = 2.0F; //dips
    private static final float DEFAULT_TOTAL_SCORE_TEXTSIZE = 16.0F; //sp

    private int pointsPerRound;
    private float degreesPerPoint;

    private Paint trackPaint;
    private Paint pointPinPaint;
    private TextPaint totalScorePaint;

    private float trackStrokeWidth;
    private float totalScoreTextSize;

    private RectF contentRect;
    private Rect viewRect;

    private RectF trackBounds;
    private RectF pinBounds;
    private Rect totalScoreTextBounds;

    private int currentScore;

    public PlayerScoreView(Context context) {
        super(context);
        init(context, null);
    }

    public PlayerScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlayerScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerScoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * Converts a raw dp value to a pixel value, based on the device density
     */
    private static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    /**
     * Converts a raw sp value to a pixel value, based on the device density
     */
    private static float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {

        pointsPerRound = DEFAULT_POINTS_PER_ROUND;
        degreesPerPoint = 360.0F / pointsPerRound;
        trackStrokeWidth = dpToPx(DEFAULT_TRACK_STROKE_WIDTH);
        totalScoreTextSize = spToPx(DEFAULT_TOTAL_SCORE_TEXTSIZE);

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(trackStrokeWidth);
        trackPaint.setColor(Color.BLACK);

        pointPinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPinPaint.setStyle(Paint.Style.FILL);
        pointPinPaint.setStrokeWidth(0F);
        pointPinPaint.setColor(Color.BLACK);

        totalScorePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        totalScorePaint.setHinting(Paint.HINTING_ON);
        totalScorePaint.setStyle(Paint.Style.FILL);
        totalScorePaint.setStrokeWidth(0F);
        totalScorePaint.setColor(Color.BLACK);
        totalScorePaint.setTextSize(totalScoreTextSize);
        totalScorePaint.setTextAlign(Paint.Align.CENTER);

        contentRect = new RectF();
        viewRect = new Rect();
        trackBounds = new RectF();
        pinBounds = new RectF();
        totalScoreTextBounds = new Rect();

    }

    public int getPointsPerRound() {
        return pointsPerRound;
    }

    public void setPointsPerRound(int pointsPerRound) {
        this.pointsPerRound = pointsPerRound;
        invalidate();
    }

    public float getDegreesPerPoint() {
        return degreesPerPoint;
    }

    public void setDegreesPerPoint(float degreesPerPoint) {
        this.degreesPerPoint = degreesPerPoint;
        invalidate();
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getDrawingRect(viewRect);
        contentRect.set(viewRect);
        contentRect.left += ViewCompat.getPaddingStart(this);
        contentRect.right -= ViewCompat.getPaddingEnd(this);
        contentRect.top += getPaddingTop();
        contentRect.bottom -= getPaddingBottom();
    }
}
