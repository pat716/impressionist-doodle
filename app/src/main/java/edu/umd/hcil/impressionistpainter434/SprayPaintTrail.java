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
    private float x, y, vy, radius, speedChangeMultiplier;
    private int color;
    private boolean speedIncreasing, finished;

    private static boolean _permanentInheritDisplayColor = true;
    private static float _bitmapColorAmount = 0.0025f;
    private static float _speedIncreaseRate = 0.1f;
    private static float _speedDecreaseRate = 0.05f;
    private static float _maxSpeed = 4.5f;

    public SprayPaintTrail(float x, float y, float radius, int color){
        this.x = x;
        this.y = y;
        vy = 0;
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
        if(speedIncreasing){
            vy += _speedIncreaseRate * speedChangeMultiplier;
            if(vy >= Math.min(_maxSpeed, radius)){
                vy = (float) Math.floor(Math.min(_maxSpeed, radius));
                speedIncreasing = false;
            }
        } else {
            vy -= _speedDecreaseRate * speedChangeMultiplier;
            if(vy <= 0){
                vy = 0;
                finished = true;
                radius *= 5;
            }
        }

        y += vy;
    }

    public void renderToCanvas(ImpressionistView impressionistView, Canvas canvas, ImageView imageView, Rect bitmapPosition){

        Paint tempPaint = new Paint();
        tempPaint.setStrokeCap(Paint.Cap.ROUND);
        tempPaint.setAntiAlias(true);
        tempPaint.setStyle(Paint.Style.STROKE);
        tempPaint.setStrokeWidth(radius);

        int displayColor = color;

        int bitmapColor = impressionistView.getColorFromImageWithinRadius(imageView, bitmapPosition, x, y - vy, radius, true);
        if(bitmapColor != Color.argb(0, 0, 0, 0)){
            float trailR = Color.red(color), trailG = Color.green(color), trailB = Color.blue(color);
            float bitmapR = Color.red(bitmapColor), bitmapG = Color.green(bitmapColor), bitmapB = Color.blue(bitmapColor);

            float displayR = (1 - _bitmapColorAmount) * trailR + _bitmapColorAmount * bitmapR;
            float displayG = (1 - _bitmapColorAmount) * trailG + _bitmapColorAmount * bitmapG;
            float displayB = (1 - _bitmapColorAmount) * trailB + _bitmapColorAmount * bitmapB;

            displayColor = Color.argb(255, (int) displayR, (int) displayG, (int) displayB);
        }

        if(_permanentInheritDisplayColor){
            color = displayColor;
        }

        tempPaint.setColor(displayColor);

        canvas.drawLine(x, y - vy, x, y, tempPaint);
    }
}
