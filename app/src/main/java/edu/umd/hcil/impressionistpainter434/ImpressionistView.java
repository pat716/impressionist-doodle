package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jon on 3/20/2016.
 */

public class ImpressionistView extends View {

    private ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 150;
    private int _defaultRadius = 15;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private float _brushStrokeSizeSpeedMultiplier = 0.25f;
    private float _brushStrokeSizeSpeedPow = 0.5f;
    private boolean _useAverageColorWithinRadius = false;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 2;
    private float _maxBrushRadius = 150;

    private int _minSplatterNum = 2;
    private int _maxSplatterNum = 6;
    private float _splatterRandomizeFactor = 0.025f;
    private float _minSplatterDistanceMultiplier = 0;
    private float _maxSplatterDistanceMultiplier = 4;
    private boolean _calculateSplatterColorsIndependently = false;

    private boolean _sprayPaintMode = false;
    private ArrayList<SprayPaintTrail> _sprayPaintTrails;

    private static float sprayPaintFillPercentMinIntensity = 1f;
    private static float sprayPaintFillPercentMaxIntensity = 0f;
    private static float sprayPaintTrailRadiusSizeMultiplierMinIntensity = 0f;
    private static float sprayPaintTrailRadiusSizeMultiplierMaxIntensity = 0.5f;
    private static int minSprayPaintTrailsMinIntensity = 0;
    private static int minSprayPaintTrailsMaxIntensity = 3;
    private static int maxSprayPaintTrailsMinIntensity = 0;
    private static int maxSprayPaintTrailsMaxIntensity = 10;

    private float _sprayPaintEffectIntensity;

    private float _sprayPaintRadiusFillPercent;
    private float _sprayPaintRadiusSizeMultiplier = 0.5f;
    private float _sprayPaintTrailRadiusSizeMultiplier;
    private float _minSprayPaintFleckSize = 1f;
    private float _maxSprayPaintFleckSize = 3f;
    private int _minSprayPaintTrails;
    private int _maxSprayPaintTrails;

    /*
    private float _sprayPaintEffectIntensity = 0.5f;

    private float _sprayPaintRadiusFillPercent = 0.5f;
    private float _sprayPaintRadiusSizeMultiplier = 0.5f;
    private float _sprayPaintTrailRadiusSizeMultiplier = 0.25f;
    private float _minSprayPaintFleckSize = 1f;
    private float _maxSprayPaintFleckSize = 3f;
    private int _minSprayPaintTrails = 2;
    private int _maxSprayPaintTrails = 6;
    */

    private int _softBrushNumStages = 10;

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        setSprayPaintEffectIntensity(0.5f);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(4);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);

        this._sprayPaintTrails = new ArrayList<>();

        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);
        }
    }

    public Bitmap getCurrentPainting(){
        return _offScreenBitmap;
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;
    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    public boolean getUseAverageColorSampling(){
        return _useAverageColorWithinRadius;
    }

    public void setUseAverageColorSampling(boolean newValue){
        this._useAverageColorWithinRadius = newValue;
        this._calculateSplatterColorsIndependently = newValue;
    }

    public boolean getSprayPaintMode(){
        return _sprayPaintMode;
    }

    public void setSprayPaintMode(boolean sprayPaintMode){
        _sprayPaintMode = sprayPaintMode;
    }

    public float getSprayPaintEffectIntensity(){
        return _sprayPaintEffectIntensity;
    }

    public void setSprayPaintEffectIntensity(float intensity){
        float intensityCapped = Math.max(0, Math.min(1, intensity));
        _sprayPaintEffectIntensity = intensityCapped;
        _sprayPaintRadiusFillPercent = (1 - intensityCapped) * sprayPaintFillPercentMinIntensity +
                intensityCapped * sprayPaintFillPercentMaxIntensity;
        _sprayPaintTrailRadiusSizeMultiplier = (1 - intensityCapped) * sprayPaintTrailRadiusSizeMultiplierMinIntensity +
                intensityCapped * sprayPaintTrailRadiusSizeMultiplierMaxIntensity;
        _minSprayPaintTrails = (int) ((1 - intensity) * ((float) minSprayPaintTrailsMinIntensity) + intensity *
                ((float) minSprayPaintTrailsMaxIntensity));
        _maxSprayPaintTrails = (int) ((1 - intensity) * ((float) maxSprayPaintTrailsMinIntensity) + intensity *
                ((float) maxSprayPaintTrailsMaxIntensity));
    }

    /**
     * Clears the painting
     */
    public void clearPainting(){
        _offScreenBitmap = Bitmap.createBitmap(_offScreenBitmap.getWidth(), _offScreenBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        _offScreenCanvas = new Canvas(_offScreenBitmap);
        _sprayPaintTrails.clear();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Set<SprayPaintTrail> removeTrails = new HashSet<>();

        boolean trailDrawn = false;

        for(SprayPaintTrail s : _sprayPaintTrails){
            if(s.isFinished()){
                removeTrails.add(s);
                continue;
            }
            s.update();
            s.renderToCanvas(_offScreenCanvas, _imageView, getBitmapPositionInsideImageView(_imageView));
            trailDrawn = true;
        }

        _sprayPaintTrails.removeAll(removeTrails);

        if(trailDrawn){
            invalidate();
        }


        if(_offScreenBitmap != null) {
            _paint.setAlpha(255);
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    public float getDistance(float x1, float y1, float x2, float y2){
        return ((float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
    }

    public int getColorFromImageAtPoint(ImageView imageView, Rect bitmapPosition, float x, float y){
        if(x < bitmapPosition.left || x >= bitmapPosition.right || y < bitmapPosition.top ||
                y >= bitmapPosition.bottom){
            return Color.argb(0, 0, 0, 0);
        }

        float[] matrixValues = new float[9];
        imageView.getImageMatrix().getValues(matrixValues);
        float imgScaleX = matrixValues[Matrix.MSCALE_X], imgScaleY = matrixValues[Matrix.MSCALE_Y];
        float imgX = (x - bitmapPosition.left) / imgScaleX, imgY = (y - bitmapPosition.top) / imgScaleY;
        Bitmap bmp = null;
        try{
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (ClassCastException e){
            e.printStackTrace();
            return Color.argb(0, 0, 0, 0);
        }

        try{
            return bmp.getPixel((int) imgX, (int) imgY);
        } catch (IllegalArgumentException e){
            return Color.argb(0, 0, 0, 0);
        }
    }

    public int getColorFromImageWithinRadius(ImageView imageView, Rect bitmapPosition, float x, float y, float
            radius, boolean circleMode){
        if(x + radius/2 < bitmapPosition.left || x - radius/2 >= bitmapPosition.right || y + radius/2 < bitmapPosition.top ||
                y - radius/2 >= bitmapPosition.bottom){
            return Color.argb(0, 0, 0, 0);
        }
        float[] matrixValues = new float[9];
        imageView.getImageMatrix().getValues(matrixValues);
        float imgScaleX = matrixValues[Matrix.MSCALE_X], imgScaleY = matrixValues[Matrix.MSCALE_Y];
        float imgX = (x - bitmapPosition.left) / imgScaleX, imgY = (y - bitmapPosition.top) / imgScaleY;



        int defaultColor = Color.argb(0, 0, 0, 0);
        Bitmap bmp = null;
        try{
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } catch (ClassCastException e){
            e.printStackTrace();
            return Color.argb(0, 0, 0, 0);
        }

        float counter = 0, totalR = 0, totalG = 0, totalB = 0, totalA = 0;
        for(float cxOffset = -radius/2; cxOffset < radius/2; cxOffset++){
            for(float cyOffset = -radius/2; cyOffset < radius/2; cyOffset++){
                float imgCX = ((x + cxOffset) - bitmapPosition.left) / imgScaleX,
                        imgCY = ((y + cyOffset) - bitmapPosition.top) / imgScaleY;
                if(imgCX < 0 || imgCX >= bitmapPosition.right || imgCY < 0 || imgCY >= bitmapPosition.bottom){
                    break;
                }

                if(circleMode){
                    float offsetDistance = getDistance(0, 0, cxOffset, cyOffset);
                    if(offsetDistance > radius){
                        continue;
                    }
                }

                try{
                    int currColor = bmp.getPixel((int) imgCX, (int) imgCY);
                    totalA += Color.alpha(currColor);
                    totalR += Color.red(currColor);
                    totalG += Color.green(currColor);
                    totalB += Color.blue(currColor);
                    counter++;
                } catch (IllegalArgumentException e){
                    continue;
                }
            }
        }
        int color = Color.argb((int) (totalA/counter), (int) (totalR/counter), (int) (totalG/counter),
                (int) (totalB/counter));
        if(color == defaultColor){
            return getColorFromImageAtPoint(imageView, bitmapPosition, x, y);
        }

        return color;
    }

    private boolean drawSquare(float x, float y, float radius, Rect bitmapPosition){
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color = defaultColor;
        if(_useAverageColorWithinRadius){
            color = getColorFromImageWithinRadius(_imageView, bitmapPosition, x, y, radius, false);
        } else {
            color = getColorFromImageAtPoint(_imageView, bitmapPosition, x, y);
        }

        if(color == defaultColor){
            color = _paint.getColor();
        }

        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeCap(Paint.Cap.SQUARE);
        _paint.setColor(color);
        _paint.setStrokeWidth(radius);
        _offScreenCanvas.drawPoint(x, y, _paint);

        return true;
    }

    private boolean drawSquareSoft(float x, float y, float radius, Rect bitmapPosition){
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color = defaultColor;

        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeCap(Paint.Cap.SQUARE);
        for(int i = 0; i < _softBrushNumStages; i++){
            float progressRatio = ((float) i)/((float) _softBrushNumStages);
            float currRadius = (1 - progressRatio) * (radius - _minBrushRadius) + _minBrushRadius;
            float currAlpha = progressRatio * 255;

            if(_useAverageColorWithinRadius){
                color = getColorFromImageWithinRadius(_imageView, bitmapPosition, x, y, currRadius, false);
            } else {
                color = getColorFromImageAtPoint(_imageView, bitmapPosition, x, y);
            }

            if(color == defaultColor){
                color = _paint.getColor();
            }

            _paint.setColor(color);

            _paint.setAlpha((int) currAlpha);
            _paint.setStrokeWidth(currRadius);
            _offScreenCanvas.drawPoint(x, y, _paint);
        }

        return true;
    }

    private boolean drawCircle(float x, float y, float radius, Rect bitmapPosition){
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color = defaultColor;
        if(_useAverageColorWithinRadius){
            color = getColorFromImageWithinRadius(_imageView, bitmapPosition, x, y, radius, true);
        } else {
            color = getColorFromImageAtPoint(_imageView, bitmapPosition, x, y);
        }

        if(color == defaultColor){
            color = _paint.getColor();
        }

        _paint.setStyle(Paint.Style.FILL);
        _paint.setColor(color);
        _offScreenCanvas.drawCircle(x, y, radius / 2, _paint);

        return true;
    }

    private boolean drawCircleSoft(float x, float y, float radius, Rect bitmapPosition){
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color = defaultColor;

        _paint.setStyle(Paint.Style.FILL);

        for(int i = 0; i < _softBrushNumStages; i++){
            float progressRatio = ((float) i)/((float) _softBrushNumStages);
            float currRadius = (1 - progressRatio) * (radius - _minBrushRadius) + _minBrushRadius;
            float currAlpha = progressRatio * 255;

            if(_useAverageColorWithinRadius){
                color = getColorFromImageWithinRadius(_imageView, bitmapPosition, x, y, currRadius, true);
            } else {
                color = getColorFromImageAtPoint(_imageView, bitmapPosition, x, y);
            }

            if(color == defaultColor){
                color = _paint.getColor();
            }

            _paint.setColor(color);
            _paint.setAlpha((int) currAlpha);
            _offScreenCanvas.drawCircle(x, y, currRadius / 2, _paint);
        }

        return true;
    }

    private boolean drawCircleSplatter(float x, float y, float radius, Rect bitmapPosition){
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color = defaultColor;
        if(_useAverageColorWithinRadius){
            color = getColorFromImageWithinRadius(_imageView, bitmapPosition, x, y, radius, true);
        } else {
            color = getColorFromImageAtPoint(_imageView, bitmapPosition, x, y);
        }

        if(color == defaultColor){
            color = _paint.getColor();
        }

        _paint.setStyle(Paint.Style.FILL);
        _paint.setColor(color);
        _offScreenCanvas.drawCircle(x, y, radius / 2, _paint);

        float brushRadiusRatio = (radius - _minBrushRadius)/(_maxBrushRadius - _minBrushRadius);

        float numSplatterNonRandom = (int) (brushRadiusRatio * (_maxSplatterNum - _minSplatterNum) + _minSplatterNum);
        float numSplatterRandom = ((float)Math.random()) * (_maxSplatterNum - _minSplatterNum) + _minSplatterNum;

        float splatterMaxDistanceMultiplierNonRandom = brushRadiusRatio * (_maxSplatterDistanceMultiplier -
                _minSplatterDistanceMultiplier) + _minSplatterDistanceMultiplier;
        float splatterMaxDistanceMultiplierRandom = ((float) Math.random()) * (_maxSplatterDistanceMultiplier -
                _minSplatterDistanceMultiplier) + _minSplatterDistanceMultiplier;
        float finalSplatterMaxDistanceMultiplier = (1 - _splatterRandomizeFactor) *
                splatterMaxDistanceMultiplierNonRandom + _splatterRandomizeFactor *
                splatterMaxDistanceMultiplierRandom;

        int finalSplatterNum = (int) ((1 - _splatterRandomizeFactor) *
                numSplatterNonRandom + _splatterRandomizeFactor * numSplatterRandom);
        for(int splatterNum = 0; splatterNum < finalSplatterNum; splatterNum++){
            float currSplatterDistanceMultiplier = ((float) Math.random()) * (finalSplatterMaxDistanceMultiplier
                    - _minSplatterDistanceMultiplier) + _minSplatterDistanceMultiplier;
            float currSplatterDistance = currSplatterDistanceMultiplier * radius;

            float distanceRatio = (currSplatterDistanceMultiplier - _minSplatterDistanceMultiplier) /
                    (_maxSplatterDistanceMultiplier - _minSplatterDistanceMultiplier);
            float splatterRadiusNonRandom = Math.max(_minBrushRadius, Math.min(_maxBrushRadius,
                    (1 - distanceRatio) * radius));
            float splatterRadiusRandom = Math.max(_minBrushRadius, Math.min(_maxBrushRadius,
                    ((float) Math.random()) * radius));
            float finalSplatterRadius = (1 - _splatterRandomizeFactor) * splatterRadiusNonRandom +
                    _splatterRandomizeFactor * splatterRadiusRandom;
            double angle = (Math.random() * Math.PI * 2);
            double xOffset = currSplatterDistance * Math.cos(angle), yOffset = currSplatterDistance *
                    Math.sin(angle);
            float currSplatterX = x+ ((float) xOffset), currSplatterY = y + ((float) yOffset);

            if(_calculateSplatterColorsIndependently){
                int tempColor = defaultColor;
                if(_useAverageColorWithinRadius){
                    tempColor = getColorFromImageWithinRadius(_imageView, bitmapPosition, currSplatterX,
                            currSplatterY, finalSplatterRadius, true);
                } else {
                    tempColor = getColorFromImageAtPoint(_imageView, bitmapPosition, currSplatterX, currSplatterY);
                }

                if(tempColor != defaultColor){
                    _paint.setColor(tempColor);
                }
            }

            _offScreenCanvas.drawCircle(currSplatterX, currSplatterY, finalSplatterRadius/2, _paint);
        }

        return true;
    }

    private boolean drawLine(float x1, float y1, float x2, float y2, float radius, Rect bitmapPosition){
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color1 = defaultColor, color2 = defaultColor;
        if(_useAverageColorWithinRadius){
            color1 = getColorFromImageWithinRadius(_imageView, bitmapPosition, x1, y1, radius, true);
            color2 = getColorFromImageWithinRadius(_imageView, bitmapPosition, x2, y2, radius, true);
        } else {
            color1 = getColorFromImageAtPoint(_imageView, bitmapPosition, x1, y1);
            color2 = getColorFromImageAtPoint(_imageView, bitmapPosition, x2, y2);
        }

        if(color1 == defaultColor) {
            color1 = _paint.getColor();
        }

        if(color2 == defaultColor) {
            color2 = _paint.getColor();
        }

        int avgR = (Color.red(color1) + Color.red(color2))/2;
        int avgG = (Color.green(color1) + Color.green(color2))/2;
        int avgB = (Color.blue(color1) + Color.blue(color2))/2;

        int avgColor = Color.argb(255, avgR, avgG, avgB);

        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setColor(avgColor);
        //_paint.setStrokeWidth(_defaultRadius);
        _paint.setStrokeWidth(radius);

        _offScreenCanvas.drawLine(x1, y1, x2, y2, _paint);

        return true;
    }

    private boolean drawSprayPaint(float x, float y, float radius, Rect bitmapPosition){
        float fillRadius = radius * _sprayPaintRadiusFillPercent;
        int defaultColor = Color.argb(0, 0, 0, 0);
        int color = defaultColor;
        if(_useAverageColorWithinRadius){
            color = getColorFromImageWithinRadius(_imageView, bitmapPosition, x, y, fillRadius, true);
        } else {
            color = getColorFromImageAtPoint(_imageView, bitmapPosition, x, y);
        }

        if(color == defaultColor){
            color = _paint.getColor();
        }

        _paint.setStyle(Paint.Style.FILL);
        _paint.setColor(color);
        _offScreenCanvas.drawCircle(x, y, fillRadius * _sprayPaintRadiusSizeMultiplier, _paint);

        Paint fleckPaint = new Paint();
        fleckPaint.setColor(color);
        fleckPaint.setStyle(Paint.Style.FILL);
        fleckPaint.setAlpha(255);
        fleckPaint.setAntiAlias(true);

        for(float xOffset = -radius * _sprayPaintRadiusSizeMultiplier; xOffset <= radius * _sprayPaintRadiusSizeMultiplier; xOffset++){
            for(float yOffset = -radius * _sprayPaintRadiusSizeMultiplier; yOffset <= radius * _sprayPaintRadiusSizeMultiplier; yOffset++){
                float offsetDistance = getDistance(0, 0, xOffset, yOffset);
                if(offsetDistance >= radius * _sprayPaintRadiusSizeMultiplier || offsetDistance < fillRadius * _sprayPaintRadiusSizeMultiplier){
                    continue;
                }
                float offsetRatio = (offsetDistance - fillRadius * _sprayPaintRadiusSizeMultiplier)/(radius *
                        _sprayPaintRadiusSizeMultiplier - fillRadius * _sprayPaintRadiusSizeMultiplier);
                boolean drawPixel = (1 - offsetRatio) + ((float) (Math.random() - 0.5)) > 0.5;
                if(drawPixel){
                    try {
                        float currFleckWidth = ((float) Math.random()) * (_maxSprayPaintFleckSize - _minSprayPaintFleckSize) +
                                _minSprayPaintFleckSize;

                        _offScreenCanvas.drawCircle(x + xOffset, y + yOffset, currFleckWidth/2, fleckPaint);
                        //_offScreenBitmap.setPixel((int) (x + xOffset), (int) (y + yOffset), color);
                    } catch (IllegalArgumentException e){
                        continue;
                    }
                }
            }
        }

        int numSprayPaintTrails = (int) ((_maxSprayPaintTrails - _minSprayPaintTrails) * ((radius -
                _minBrushRadius)/(_maxBrushRadius - _minBrushRadius))) + _minSprayPaintTrails;
        float trailEffectiveRadius = (radius + fillRadius * 3)/4;
        for(int i = 0; i < numSprayPaintTrails; i++){
            float startDistance = ((float) Math.random()) * trailEffectiveRadius * _sprayPaintRadiusSizeMultiplier;
            float angle = (float) (Math.random() * Math.PI * 2);
            float xOffset = (float) Math.cos(angle) * startDistance, yOffset = (float) Math.sin(angle) * startDistance;
            float trailRadius = (float) (Math.random() * (trailEffectiveRadius * _sprayPaintRadiusSizeMultiplier * _sprayPaintTrailRadiusSizeMultiplier));
            SprayPaintTrail s = new SprayPaintTrail(x + xOffset, y + yOffset, trailRadius, color, this);
            _sprayPaintTrails.add(0, s);
        }

        return true;
    }

    private boolean processMotionEventBrush(MotionEvent motionEvent){
        Rect bitmapPosition = getBitmapPositionInsideImageView(_imageView);
        boolean needInvalidate = false;
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float endX = motionEvent.getX(), endY = motionEvent.getY();
                float endBrushRadius = _defaultRadius;
                for(int i = 0; i < motionEvent.getHistorySize(); i++){
                    float currX = motionEvent.getHistoricalX(i), currY = motionEvent.getHistoricalY(i);
                    if(currX < 0 || currX >= _offScreenCanvas.getWidth() || currY < 0 || currY >= _offScreenCanvas.getHeight()){
                        continue;
                    }

                    float currBrushRadius = _defaultRadius;
                    endBrushRadius = currBrushRadius;

                    if(_useMotionSpeedForBrushStrokeSize && i > 0){
                        float prevX = motionEvent.getHistoricalX(i - 1), prevY = motionEvent.getHistoricalY(i - 1);
                        float distanceTraveled = getDistance(currX, currY, prevX, prevY);
                        float rawBrushRadius = distanceTraveled * _brushStrokeSizeSpeedMultiplier;
                        currBrushRadius = Math.max(_minBrushRadius, Math.min(_maxBrushRadius, rawBrushRadius));
                        float brushRadiusRatio = (currBrushRadius - _minBrushRadius)/(_maxBrushRadius - _minBrushRadius);
                        brushRadiusRatio = (float) Math.pow(brushRadiusRatio, _brushStrokeSizeSpeedPow);
                        currBrushRadius = brushRadiusRatio * (_maxBrushRadius - _minBrushRadius) + _minBrushRadius;
                        endBrushRadius = currBrushRadius;
                    }

                    switch (_brushType){
                        case Square:
                            needInvalidate = drawSquare(currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                            break;
                        case SquareSoft:
                            needInvalidate = drawSquareSoft(currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                            break;
                        case Circle:
                            needInvalidate = drawCircle(currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                            break;
                        case CircleSoft:
                            needInvalidate = drawCircleSoft(currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                            break;
                        case CircleSplatter:
                            needInvalidate = drawCircleSplatter(currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                            break;
                        case Line:
                            float prevX = currX, prevY = currY;
                            if(i > 0){
                                prevX = motionEvent.getHistoricalX(i - 1);
                                prevY = motionEvent.getHistoricalY(i - 1);
                            }
                            needInvalidate = drawLine(prevX, prevY, currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                            break;

                    }

                }

                switch (_brushType){
                    case Square:
                        needInvalidate = drawSquare(endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                        break;
                    case SquareSoft:
                        needInvalidate = drawSquareSoft(endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                        break;
                    case Circle:
                        needInvalidate = drawCircle(endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                        break;
                    case CircleSoft:
                        needInvalidate = drawCircleSoft(endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                        break;
                    case CircleSplatter:
                        needInvalidate = drawCircleSplatter(endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                        break;
                    case Line:
                        float prevX = endX, prevY = endY;
                        if(motionEvent.getHistorySize() > 0){
                            prevX = motionEvent.getHistoricalX(motionEvent.getHistorySize() - 1);
                            prevY = motionEvent.getHistoricalY(motionEvent.getHistorySize() - 1);
                        }
                        needInvalidate = drawLine(prevX, prevY, endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                        break;
                }
                break;
            default:
                break;
        }

        if(needInvalidate) {
            invalidate();
        }

        return true;
    }

    private boolean processMotionEventSprayPaint(MotionEvent motionEvent){
        Rect bitmapPosition = getBitmapPositionInsideImageView(_imageView);
        boolean needInvalidate = false;
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                float endX = motionEvent.getX(), endY = motionEvent.getY();
                float endBrushRadius = _defaultRadius;
                for(int i = 0; i < motionEvent.getHistorySize(); i++) {
                    float currX = motionEvent.getHistoricalX(i), currY = motionEvent.getHistoricalY(i);
                    if (currX < 0 || currX >= _offScreenCanvas.getWidth() || currY < 0 || currY >= _offScreenCanvas.getHeight()) {
                        continue;
                    }

                    float currBrushRadius = _defaultRadius;
                    endBrushRadius = currBrushRadius;

                    if (_useMotionSpeedForBrushStrokeSize && i > 0) {
                        float prevX = motionEvent.getHistoricalX(i - 1), prevY = motionEvent.getHistoricalY(i - 1);
                        float distanceTraveled = getDistance(currX, currY, prevX, prevY);
                        float rawBrushRadius = distanceTraveled * _brushStrokeSizeSpeedMultiplier;
                        currBrushRadius = Math.max(_minBrushRadius, Math.min(_maxBrushRadius, rawBrushRadius));
                        float brushRadiusRatio = (currBrushRadius - _minBrushRadius) / (_maxBrushRadius - _minBrushRadius);
                        brushRadiusRatio = (float) Math.pow(brushRadiusRatio, _brushStrokeSizeSpeedPow);
                        currBrushRadius = brushRadiusRatio * (_maxBrushRadius - _minBrushRadius) + _minBrushRadius;
                        endBrushRadius = currBrushRadius;
                    }

                    needInvalidate = drawSprayPaint(currX, currY, currBrushRadius, bitmapPosition) || needInvalidate;
                }
                needInvalidate = drawSprayPaint(endX, endY, endBrushRadius, bitmapPosition) || needInvalidate;
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }

        if(needInvalidate) {
            invalidate();
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){
        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        if(_imageView == null || _imageView.getDrawable() == null || _offScreenCanvas == null || _offScreenBitmap == null){
            return true;
        }

        if(_sprayPaintMode){
            return processMotionEventSprayPaint(motionEvent);
        } else {
            return processMotionEventBrush(motionEvent);
        }
    }

    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {
            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }
}

