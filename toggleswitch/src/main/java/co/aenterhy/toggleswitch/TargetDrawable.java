package co.aenterhy.toggleswitch;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * @author aenterhy
 */
public class TargetDrawable {

    private float mTranslationX = 0.0f;
    private float mTranslationY = 0.0f;
    private float mPositionX = 0.0f;
    private float mPositionY = 0.0f;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private Drawable mDrawable;
    private int mNumDrawables = 1;

    public TargetDrawable(Resources res, int resId) {
        setDrawable(res, resId);
    }

    public void setDrawable(Resources res, int resId) {
        Drawable drawable = resId == 0 ? null : res.getDrawable(resId);
        mDrawable = drawable != null ? drawable.mutate() : null;
        resizeDrawables();
    }

    private void resizeDrawables() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int maxWidth = 0;
            int maxHeight = 0;

            for (int i = 0; i < mNumDrawables; i++) {
                d.selectDrawable(i);
                Drawable childDrawable = d.getCurrent();
                maxWidth = Math.max(maxWidth, childDrawable.getIntrinsicWidth());
                maxHeight = Math.max(maxHeight, childDrawable.getIntrinsicHeight());
            }
            d.setBounds(0, 0, maxWidth, maxHeight);
            for (int i = 0; i < mNumDrawables; i++) {
                d.selectDrawable(i);
                Drawable childDrawable = d.getCurrent();
                childDrawable.setBounds(0, 0, maxWidth, maxHeight);
            }
        } else if (mDrawable != null) {
            mDrawable.setBounds(0, 0, mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    public void setX(float x) {
        mTranslationX = x;
    }

    public void setY(float y) {
        mTranslationY = y;
    }

    public void setPositionX(float x) {
        mPositionX = x;
    }

    public void setPositionY(float y) {
        mPositionY = y;
    }

    public int getWidth() {
        return mDrawable != null ? mDrawable.getIntrinsicWidth() : 0;
    }

    public int getHeight() {
        return mDrawable != null ? mDrawable.getIntrinsicHeight() : 0;
    }

    public void draw(Canvas canvas) {
        if (mDrawable == null) {
            return;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleX, mScaleY, mPositionX, mPositionY);
        canvas.translate(mTranslationX + mPositionX, mTranslationY + mPositionY);
        canvas.translate(-0.5f * getWidth(), -0.5f * getHeight());
        mDrawable.draw(canvas);
        canvas.restore();
    }
}