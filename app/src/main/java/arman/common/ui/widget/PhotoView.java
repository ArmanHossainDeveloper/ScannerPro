package arman.common.ui.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import arman.common.infocodes.InfoCode;

public class PhotoView extends View {
    private Bitmap bitmap;
    private Matrix matrix = new Matrix();
    private float scale = 1f;
    private float currentScale = 1f;
    float minScale = 1f, maxScale = 100f;
    private float lastX, lastY;
    private float dx, dy;
    int viewWidth, viewHeight;

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;


    public PhotoView(Context context) {
        super(context);
        init(context);
    }
    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        calculateMinScale();
        invalidate();
    }

    private void calculateMinScale() {
        if (bitmap == null || viewWidth == 0 || viewHeight == 0) return;
        float scaleX = (float) viewWidth / bitmap.getWidth();
        float scaleY = (float) viewHeight / bitmap.getHeight();
        minScale = Math.min(scaleX, scaleY);
        InfoCode.log("w: " + scaleX + " h: " + scaleY + " m: " + minScale);

        //currentScale = minScale;

    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (viewWidth == w) return;
        viewWidth = w;
        viewHeight = h;
        calculateMinScale();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, matrix, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        /*switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    float x = event.getX();
                    float y = event.getY();

                    // Calculate pan distance
                    float dx = x - lastX;
                    float dy = y - lastY;

                    matrix.postTranslate(dx, dy);
                    invalidate();

                    lastX = x;
                    lastY = y;
                }
                break;
        }*/

        return true;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            float newScale = currentScale * scaleFactor;
            newScale = Math.max(minScale, Math.min(newScale, maxScale));

            if (newScale != currentScale) {
                currentScale = newScale;
                matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                invalidate();
            }
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            matrix.postTranslate(-distanceX, -distanceY);
            invalidate();
            return true;
        }
    }

}






















