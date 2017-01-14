package com.vinaysshenoy.scoreboard.widget.playerscoreview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Created by vinaysshenoy on 14/1/17.
 */

public class PlayerScoreView extends View {

    private static final int DEFAULT_POINTS_PER_ROUND = 30;
    private static final float DEFAULT_TRACK_STROKE_WIDTH = 2.0F; //dips
    private static final float DEFAULT_PIN_RADIUS = 16.0F; //dips
    private static final float DEFAULT_TOTAL_SCORE_TEXTSIZE = 16.0F; //sp

    private int pointsPerRound;
    private float degreesPerPoint;

    private Paint trackPaint;
    private Paint pointPinPaint;
    private TextPaint totalScorePaint;

    private float trackStrokeWidth;
    private float totalScoreTextSize;
    private float pinRadius;

    private RectF contentRect;
    private Rect viewRect;

    private RectF trackBounds;
    private RectF pinBounds;
    private Rect totalScoreTextBounds;
    private PointF pinCenter;

    private int currentScore;
    //Varies from 0F to 359F
    private float pinAngularPosition;

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

    private static void adjustToBeSquare(@NonNull RectF rectF) {

        final float width = rectF.width();
        final float height = rectF.height();

        if (width > height) {
            //Adjust width to match height
            final float delta = (width - height) / 2F;
            rectF.inset(delta, 0F);

        } else if (height > width) {
            //Adjust height to match width
            final float delta = (height - width) / 2F;
            rectF.inset(0F, delta);
        }
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {

        pointsPerRound = DEFAULT_POINTS_PER_ROUND;
        degreesPerPoint = 360.0F / pointsPerRound;
        trackStrokeWidth = dpToPx(DEFAULT_TRACK_STROKE_WIDTH);
        totalScoreTextSize = spToPx(DEFAULT_TOTAL_SCORE_TEXTSIZE);
        pinRadius = dpToPx(DEFAULT_PIN_RADIUS);

        currentScore = 10;

        calculateAngularPositionOfPin();

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
        pinBounds = new RectF(0F, 0F, pinRadius, pinRadius);
        totalScoreTextBounds = new Rect();
        pinCenter = new PointF();
    }

    public int getPointsPerRound() {
        return pointsPerRound;
    }

    public void setPointsPerRound(int pointsPerRound) {
        this.pointsPerRound = pointsPerRound;
        degreesPerPoint = 360.0F / pointsPerRound;
        calculateAngularPositionOfPin();
        invalidate();
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
        calculateAngularPositionOfPin();
        invalidate();
    }

    private void calculateAngularPositionOfPin() {
        pinAngularPosition = ((currentScore * degreesPerPoint) % 360.0F) - 90.0F;
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

        precalculateItemBounds();
    }

    private void precalculateItemBounds() {

        trackBounds.set(contentRect);
        adjustToBeSquare(trackBounds);

        //Inset track to keep space for the pin
        trackBounds.inset(pinRadius, pinRadius);

        updatePinCenterPosition();
    }

    private void updatePinCenterPosition() {

        final float trackRadius = trackBounds.width() / 2F;
        pinCenter.x = (trackRadius * (float) cos(toRadians(pinAngularPosition))) + trackBounds.centerX();
        pinCenter.y = (trackRadius * (float) sin(toRadians(pinAngularPosition))) + trackBounds.centerY();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (contentRect.height() > 0F && contentRect.width() > 0F) {
            canvas.drawColor(Color.LTGRAY);
            canvas.drawCircle(trackBounds.centerX(), trackBounds.centerY(), trackBounds.width() / 2F, trackPaint);
            canvas.drawCircle(pinCenter.x, pinCenter.y, pinRadius, pointPinPaint);
        }
    }
}
