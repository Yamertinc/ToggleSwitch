package co.aenterhy.toggleswitch;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author aenterhy
 */
public class ToggleSwitchButton extends View {

    private static final int STATE_IDLE = 0;
    private static final int STATE_START = 1;
    private static final int STATE_FINISH = 2;

    private static final float SNAP_MARGIN_DEFAULT = 20.0f;

    private OnTriggerListener mOnTriggerListener;

    private TargetDrawable mHandleDrawable;
    private TargetDrawable mOuterRing;

    private TargetDrawable upper;
    private TargetDrawable bottom;

    private int mActiveTarget = -1;
    private float mGlowRadius;
    private float mWaveCenterX;
    private float mWaveCenterY;
    private int mMaxTargetHeight;
    private int mMaxTargetWidth;

    private float mOuterRadius = 0.0f;
    private float mSnapMargin = 0.0f;
    private boolean mDragging;
    private final int TARGET_UP = 1;
    private final int TARGET_BOTTOM = 3;
    private int mPointerId;

    public interface OnTriggerListener {

        void toggledUp();

        void toggledDown();
    }

    public ToggleSwitchButton(Context context) {
        this(context, null);
    }

    public ToggleSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToggleSwitchButton);

        mHandleDrawable = new TargetDrawable(res, getResourceId(a, R.styleable.ToggleSwitchButton_handleDrawable));
        upper = new TargetDrawable(res, getResourceId(a, R.styleable.ToggleSwitchButton_upper));
        bottom = new TargetDrawable(res, getResourceId(a, R.styleable.ToggleSwitchButton_bottom));

        mOuterRing = new TargetDrawable(res, R.drawable.ic_switch_shape);
        mOuterRadius = a.getDimension(R.styleable.ToggleSwitchButton_outerRadius, mOuterRadius);

        mGlowRadius = mOuterRing.getHeight() / 2;
        mMaxTargetWidth = mHandleDrawable.getWidth();
        mMaxTargetHeight = mHandleDrawable.getHeight();

        a.recycle();

        if (mOuterRadius == 0.0f)
            mOuterRadius = Math.max(mOuterRing.getWidth(), mOuterRing.getHeight()) / 2.0f;

        mSnapMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SNAP_MARGIN_DEFAULT, getContext().getResources().getDisplayMetrics());
    }

    private int getResourceId(TypedArray a, int id) {
        TypedValue tv = a.peekValue(id);
        return tv == null ? 0 : tv.resourceId;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return mMaxTargetWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return mOuterRing.getHeight() + mMaxTargetHeight;
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int computedWidth = resolveMeasured(widthMeasureSpec, getSuggestedMinimumWidth());
        int computedHeight = resolveMeasured(heightMeasureSpec, getSuggestedMinimumHeight());
        setMeasuredDimension(computedWidth, computedHeight);
    }

    private void switchToState(int state) {
        switch (state) {
            case STATE_IDLE:
            case STATE_START:
                reset();
                break;
            case STATE_FINISH:
                doFinish();
                break;
        }
    }

    private void reset() {
        mHandleDrawable.setY(0);
        mHandleDrawable.setX(0);
        mActiveTarget = -1;
    }

    private void dispatchTriggerEvent(int whichTarget) {
        switch (whichTarget) {
            case TARGET_UP:
                mOnTriggerListener.toggledUp();
                break;
            case TARGET_BOTTOM:
                mOnTriggerListener.toggledDown();
                break;
        }
    }

    private void doFinish() {
        final int activeTarget = mActiveTarget;
        final boolean targetHit = activeTarget != -1;
        if (targetHit) {
            dispatchTriggerEvent(activeTarget);
        }
        reset();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        boolean handled = false;
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                handleDown(event);
                handleMove(event);
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                handleMove(event);
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                handleMove(event);
                handleUp(event);
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handleMove(event);
                handleCancel();
                handled = true;
                break;
        }
        invalidate();
        return handled ? true : super.onTouchEvent(event);
    }

    private void updateGlowPosition(float y) {
        mHandleDrawable.setY(y);
    }

    private void handleDown(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        float eventX = event.getX(actionIndex);
        float eventY = event.getY(actionIndex);
        switchToState(STATE_START);
        if (!trySwitchToFirstTouchState(eventX, eventY)) {
            mDragging = false;
        } else {
            mPointerId = event.getPointerId(actionIndex);
            updateGlowPosition(eventY);
        }
    }

    private void handleUp(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        if (event.getPointerId(actionIndex) == mPointerId) switchToState(STATE_FINISH);

    }

    private void handleCancel() {
        switchToState(STATE_FINISH);
    }

    private void handleMove(MotionEvent event) {
        int activeTarget = -1;
        final int historySize = event.getHistorySize();
        float y = 0.0f;
        int actionIndex = event.findPointerIndex(mPointerId);

        if (actionIndex == -1) return;

        for (int k = 0; k < historySize + 1; k++) {
            float eventX = k < historySize ? event.getHistoricalX(actionIndex, k) : event.getX(actionIndex);
            float eventY = k < historySize ? event.getHistoricalY(actionIndex, k) : event.getY(actionIndex);
            float tx = eventX - mWaveCenterX;
            float ty = eventY - mWaveCenterY;
            float touchRadius = (float) Math.sqrt(dist2(tx, ty));
            final float scale = touchRadius > mOuterRadius ? mOuterRadius / touchRadius : 1.0f;
            float limitY = ty * scale;
            double angleRad = Math.atan2(-ty, tx);

            if (!mDragging)
                trySwitchToFirstTouchState(eventX, eventY);

            if (mDragging) {
                final float snapRadius = mOuterRadius - mSnapMargin;
                final float snapDistance2 = snapRadius * snapRadius;

                for (int i = 0; i < 4; i++) {
                    double targetMinRad = (i - 0.5) * 2 * Math.PI / 4;
                    double targetMaxRad = (i + 0.5) * 2 * Math.PI / 4;
                    boolean angleMatches = (angleRad > targetMinRad && angleRad <= targetMaxRad) || (angleRad + 2 * Math.PI > targetMinRad && angleRad + 2 * Math.PI <= targetMaxRad);
                    if (angleMatches && (dist2(tx, ty) > snapDistance2)) {
                        activeTarget = i;
                    }
                }
            }
            y = limitY;
        }

        if (!mDragging) return;

        updateGlowPosition(y);
        mActiveTarget = activeTarget;
    }

    private boolean trySwitchToFirstTouchState(float x, float y) {
        final float tx = x - mWaveCenterX;
        final float ty = y - mWaveCenterY;
        if (dist2(tx, ty) <= mGlowRadius * mGlowRadius) {
            updateGlowPosition(ty);
            mDragging = true;
            return true;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final int width = right - left;
        final int height = bottom - top;
        final float placementWidth = mHandleDrawable.getWidth();
        final float placementHeight = mOuterRing.getHeight();
        float newWaveCenterX = Math.max(width, mMaxTargetWidth + placementWidth) / 4;
        float newWaveCenterY = Math.max(height, +mMaxTargetHeight + placementHeight) / 2;

        mOuterRing.setPositionX(newWaveCenterX);
        mOuterRing.setPositionY(newWaveCenterY);

        mHandleDrawable.setPositionX(newWaveCenterX);
        mHandleDrawable.setPositionY(newWaveCenterY);

        updateTargetPositions(newWaveCenterX, newWaveCenterY);
        updateGlowPosition(newWaveCenterY);

        mWaveCenterX = newWaveCenterX;
        mWaveCenterY = newWaveCenterY;

        switchToState(STATE_FINISH);
    }

    private void updateTargetPositions(float centerX, float centerY) {
        final float alpha = (float) (-2.0f * Math.PI / 4);

        upper.setPositionX(centerX);
        upper.setPositionY(centerY);
        bottom.setPositionX(centerX);
        bottom.setPositionY(centerY);

        upper.setX(mOuterRadius * (float) Math.cos(alpha * 1));
        upper.setY(mOuterRadius * (float) Math.sin(alpha * 1));
        bottom.setX(mOuterRadius * (float) Math.cos(alpha * 3));
        bottom.setY(mOuterRadius * (float) Math.sin(alpha * 3));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mOuterRing.draw(canvas);
        bottom.draw(canvas);
        upper.draw(canvas);
        mHandleDrawable.draw(canvas);
    }

    public void setOnTriggerListener(OnTriggerListener listener) {
        mOnTriggerListener = listener;
    }

    private float dist2(float dx, float dy) {
        return dx * dx + dy * dy;
    }
}