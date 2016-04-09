package edu.umd.hcil.impressionistpainter434;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

/**
 * Created by psweeney on 4/5/16.
 */

public class SprayPaintTrail {
    private float x, y, vy, radius, speedChangeMultiplier, peakSpeed;
    private int color;
    private boolean speedIncreasing, finished;
    private ImpressionistView _parent;

    private static boolean _permanentInheritDisplayColor = true;
    private static float _inheritBitmapColorAmount = 0.00375f;
    private static float _speedIncreaseRate = 0.15f;
    private static float _speedDecreaseRate = 0.025f;
    private static float _maxSpeedMinIntensity = 2.5f;
    private static float _maxSpeedMaxIntensity = 7.0f;

    public SprayPaintTrail(float x, float y, float radius, int color, ImpressionistView parent){
        this.x = x;
        this.y = y;
        vy = 0;
        peakSpeed = 0;
        _parent = parent;
        speedIncreasing = true;
        speedChangeMultiplier = ((float) Math.random()) * 1.5f + 0.5f;
        this.radius = radius;
        this.color = color;
        finished = false;
    }

    public boolean isFinished(){
        return finished;
    }

    public void update(){
        if(finished){
            return;
        }

        if(y >= _parent.getHeight()){
            finished = true;
            return;
        }

        float effectiveMaxSpeed = _parent.getSprayPaintEffectIntensity() * (_maxSpeedMaxIntensity -
                _maxSpeedMinIntensity) + _maxSpeedMinIntensity;
        if(speedIncreasing){
            vy += _speedIncreaseRate * speedChangeMultiplier * (((float) Math.random()) + 0.5f);

            if(vy >= Math.min(effectiveMaxSpeed, radius)){
                vy = (float) Math.floor(Math.min(effectiveMaxSpeed, radius));
                speedIncreasing = false;
            }
            peakSpeed = vy;
        } else {
            vy -= _speedDecreaseRate * speedChangeMultiplier * (((float) Math.random()) + 0.5f);
            if(vy <= 0){
                vy = 0;
                finished = true;
            }
        }

        y += vy;
    }

    public void renderToCanvas(Canvas canvas, ImageView imageView, Rect bitmapPosition){

        Paint tempPaint = new Paint();
        tempPaint.setStrokeCap(Paint.Cap.ROUND);
        tempPaint.setAntiAlias(true);
        tempPaint.setStyle(Paint.Style.STROKE);
        tempPaint.setStrokeWidth(radius);

        int displayColor = color;

        int bitmapColor;

        if(_parent.getUseAverageColorSampling()) {
            bitmapColor = _parent.getColorFromImageWithinRadius(imageView, bitmapPosition, x, y - vy, radius, true);
        } else {
            bitmapColor = _parent.getColorFromImageAtPoint(imageView, bitmapPosition, x, y - vy);
        }

        if(bitmapColor != Color.argb(0, 0, 0, 0)){
            float trailR = Color.red(color), trailG = Color.green(color), trailB = Color.blue(color);
            float bitmapR = Color.red(bitmapColor), bitmapG = Color.green(bitmapColor), bitmapB = Color.blue(bitmapColor);

            float displayR = (1 - _inheritBitmapColorAmount) * trailR + _inheritBitmapColorAmount * bitmapR;
            float displayG = (1 - _inheritBitmapColorAmount) * trailG + _inheritBitmapColorAmount * bitmapG;
            float displayB = (1 - _inheritBitmapColorAmount) * trailB + _inheritBitmapColorAmount * bitmapB;

            displayColor = Color.argb(255, (int) displayR, (int) displayG, (int) displayB);
        }

        if(_permanentInheritDisplayColor){
            color = displayColor;
        }

        tempPaint.setColor(displayColor);

        if(!speedIncreasing){
            float peakSpeedRatio = vy/peakSpeed;

            float currAlpha = peakSpeedRatio * peakSpeedRatio * 255f;
            tempPaint.setAlpha((int) currAlpha);
        } else {
            tempPaint.setAlpha(255);
        }

        canvas.drawLine(x, y - vy, x, y, tempPaint);
    }
}
