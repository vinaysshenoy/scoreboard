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
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.Locale;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Created by vinaysshenoy on 14/1/17.
 */

public class PlayerScoreView extends View {

    private static final String TAG = "PlayerScoreView";

    private static final int DEFAULT_POINTS_PER_ROUND = 30;
    private static final float DEFAULT_TRACK_STROKE_WIDTH = 2.0F; //dips
    private static final float DEFAULT_PIN_RADIUS = 16.0F; //dips
    private static final float DEFAULT_SCORE_TEXT_SIZE = 48.0F; //sp
    @TouchState
    private int touchState;
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
    private RectF textDrawBounds;
    private Rect totalScoreTextBounds;
    private PointF prevTouchPoint;
    private PointF curTouchPoint;
    private PointF newPinCenterHolder;
    private int currentScore;
    private String currentScoreText;
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

    /**
     * This is a method simplified for our use case taken from StackOverflow. The simplifications
     * mostly stem from the fact that PointA and center are the same.
     * <br/>
     * <a href="http://stackoverflow.com/questions/13053061/circle-line-intersection-points">LINK</a>
     * <pre>
     * <code>
     * public static List < Point > getCircleLineIntersectionPoint(Point pointA, Point pointB, Point center, double radius) {
     * double baX = pointB.x - pointA.x;
     * double baY = pointB.y - pointA.y;
     * double caX = center.x - pointA.x;
     * double caY = center.y - pointA.y;
     *
     * double a = baX * baX + baY * baY;
     * double bBy2 = baX * caX + baY * caY;
     * double c = caX * caX + caY * caY - radius * radius;
     *
     * double pBy2 = bBy2 / a;
     * double q = c / a;
     *
     * double disc = pBy2 * pBy2 - q;
     * if (disc < 0) {
     * return Collections.emptyList();
     * }
     * // if disc == 0 ... dealt with later
     * double tmpSqrt = Math.sqrt(disc);
     * double abScalingFactor1 = -pBy2 + tmpSqrt;
     * double abScalingFactor2 = -pBy2 - tmpSqrt;
     *
     * Point p1 = new Point(pointA.x - baX * abScalingFactor1, pointA.y - baY * abScalingFactor1);
     * if (disc == 0) { // abScalingFactor1 == abScalingFactor2
     * return Collections.singletonList(p1);
     * }
     * Point p2 = new Point(pointA.x - baX * abScalingFactor2, pointA.y - baY * abScalingFactor2);
     * return Arrays.asList(p1, p2);
     * }
     * </code>
     * </pre>
     */
    public static void findUpdatedCenter(float centerX, float centerY, float newCenterX, float newCenterY, float radius, PointF holder) {
        float baX = newCenterX - centerX;
        float baY = newCenterY - centerY;

        float a = baX * baX + baY * baY;
        float c = -radius * radius;

        float q = c / a;

        float tmpSqrt = (float) Math.sqrt(-q);
        float scalingFactor = -tmpSqrt;

        holder.x = centerX - baX * scalingFactor;
        holder.y = centerY - baY * scalingFactor;
    }

    private static float angleBetween2Lines(float aX1, float aY1, float aX2, float aY2, float bX1, float bY1, float bX2, float bY2) {
        final float angle1 = (float) Math.atan2(aY2 - aY1, aX1 - aX2);
        final float angle2 = (float) Math.atan2(bY2 - bY1, bX1 - bX2);
        float calculatedAngle = (float) Math.toDegrees(angle1 - angle2);
        if (calculatedAngle < 0) {
            calculatedAngle += 359;
        }
        return calculatedAngle;
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attributeSet) {

        pointsPerRound = DEFAULT_POINTS_PER_ROUND;
        degreesPerPoint = 360.0F / pointsPerRound;
        trackStrokeWidth = dpToPx(DEFAULT_TRACK_STROKE_WIDTH);
        pinRadius = dpToPx(DEFAULT_PIN_RADIUS);
        totalScoreTextSize = spToPx(DEFAULT_SCORE_TEXT_SIZE);

        currentScore = 250;
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
        pinBounds = new RectF();
        totalScoreTextBounds = new Rect();
        textDrawBounds = new RectF();
        prevTouchPoint = new PointF();
        curTouchPoint = new PointF();
        newPinCenterHolder = new PointF();

        touchState = TouchState.TOUCH_NOTHING;

        updateCurrentScoreMessage();
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
        updateCurrentScoreMessage();
        invalidate();
    }

    public void setScoreCounterTypeface(@NonNull Typeface typeface) {
        totalScorePaint.setTypeface(typeface);
        updateTextBounds();
        invalidate();
    }

    private void updateCurrentScoreMessage() {
        currentScoreText = String.format(Locale.getDefault(), "%d", currentScore);
        updateTextBounds();
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

        /*
        * Note: Order of operations is important as each uses values
        * calculated in the previous step
        **/
        updateTrackBounds();
        updatePinBounds();
        textDrawBounds.set(
                trackBounds.centerX() - totalScoreTextBounds.width() / 2F,
                trackBounds.centerY() - totalScoreTextBounds.height() / 2F,
                trackBounds.centerX() + totalScoreTextBounds.width() / 2F,
                trackBounds.centerY() + totalScoreTextBounds.height() / 2F
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean handled = false;

        final float eventX = event.getX();
        final float eventY = event.getY();
        curTouchPoint.set(eventX, eventY);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN: {
                if (pinBounds.contains(eventX, eventY)) {
                    touchState = TouchState.TOUCH_PIN;
                }
                prevTouchPoint.set(curTouchPoint);
                handled = true;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                touchState = TouchState.TOUCH_NOTHING;
                Log.d(TAG, "onTouchEvent: Complete");
                handled = true;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (TouchState.TOUCH_PIN == touchState) {
                    handlePinMovedBy(curTouchPoint.x - prevTouchPoint.x, curTouchPoint.y - prevTouchPoint.y);
                    prevTouchPoint.set(curTouchPoint);
                    recalculateNewScore();
                    handled = true;
                    invalidate();
                    break;
                }
            }
        }

        return handled || super.onTouchEvent(event);
    }

    private void recalculateNewScore() {

        final float angle = angleBetween2Lines(
                trackBounds.centerX(), trackBounds.centerY(), trackBounds.centerX(), trackBounds.top,
                trackBounds.centerX(), trackBounds.centerY(), pinBounds.centerX(), pinBounds.centerY());

    }

    private void handlePinMovedBy(float dX, float dY) {


        final float newCenterX = pinBounds.centerX() + dX;
        final float newCenterY = pinBounds.centerY() + dY;

        newPinCenterHolder.set(newCenterX, newCenterY);
        findUpdatedCenter(trackBounds.centerX(), trackBounds.centerY(), newCenterX, newCenterY, trackBounds.width() / 2, newPinCenterHolder);
        pinBounds.set(newPinCenterHolder.x - pinBounds.width() / 2F, newPinCenterHolder.y - pinBounds.height() / 2F, newPinCenterHolder.x + pinBounds.width() / 2F, newPinCenterHolder.y + pinBounds.height() / 2F);

    }

    private void updateTrackBounds() {
        trackBounds.set(contentRect);
        adjustToBeSquare(trackBounds);

        //Inset track to keep space for the pin
        trackBounds.inset(pinRadius, pinRadius);
    }

    private void updateTextBounds() {
        totalScorePaint.getTextBounds(currentScoreText, 0, currentScoreText.length(), totalScoreTextBounds);
        textDrawBounds.set(
                trackBounds.centerX() - totalScoreTextBounds.width() / 2F,
                trackBounds.centerY() - totalScoreTextBounds.height() / 2F,
                trackBounds.centerX() + totalScoreTextBounds.width() / 2F,
                trackBounds.centerY() + totalScoreTextBounds.height() / 2F
        );
    }

    private void updatePinBounds() {

        final float trackRadius = trackBounds.width() / 2F;


        final float cX = (trackRadius * (float) cos(toRadians(pinAngularPosition))) + trackBounds.centerX();
        final float cY = (trackRadius * (float) sin(toRadians(pinAngularPosition))) + trackBounds.centerY();

        pinBounds.set(cX - pinRadius, cY - pinRadius, cX + pinRadius, cY + pinRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (contentRect.height() > 0F && contentRect.width() > 0F) {
            canvas.drawColor(Color.LTGRAY);
            canvas.drawCircle(trackBounds.centerX(), trackBounds.centerY(), trackBounds.width() / 2F, trackPaint);
            canvas.drawCircle(pinBounds.centerX(), pinBounds.centerY(), pinRadius, pointPinPaint);
            canvas.drawText(currentScoreText, trackBounds.centerX(), trackBounds.centerY() + totalScoreTextBounds.height() / 4F, totalScorePaint);

        }
    }

    @IntDef({TouchState.TOUCH_NOTHING, TouchState.TOUCH_PIN})
    private @interface TouchState {
        int TOUCH_NOTHING = -1;
        int TOUCH_PIN = 0;
    }

}
