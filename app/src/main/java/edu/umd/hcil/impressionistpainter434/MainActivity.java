package edu.umd.hcil.impressionistpainter434;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity implements OnMenuItemClickListener, SeekBar.OnSeekBarChangeListener {

    private static int RESULT_LOAD_IMAGE = 1;
    private  ImpressionistView _impressionistView;

    // These images are downloaded and added to the Android Gallery when the 'Download Images' button is clicked.
    // This was super useful on the emulator where there are no images by default
    private static String[] IMAGE_URLS ={
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/BoliviaBird_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/BolivianDoor_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/MinnesotaFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PeruHike_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/ReginaSquirrel_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreDog_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreStreet_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreStreet_PhotoByJonFroehlich2(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/SucreWine_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/WashingtonStateFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/JonILikeThisShirt_Medium.JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/JonUW_(853x1280).jpg",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/MattMThermography_Medium.jpg",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PinkFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PinkFlower2_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/PurpleFlowerPlusButterfly_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/WhiteFlower_PhotoByJonFroehlich(Medium).JPG",
            "http://www.cs.umd.edu/class/spring2016/cmsc434/assignments/IA08-AndroidII/Images/YellowFlower_PhotoByJonFroehlich(Medium).JPG",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _impressionistView = (ImpressionistView)findViewById(R.id.viewImpressionist);
        ImageView imageView = (ImageView)findViewById(R.id.viewImage);
        _impressionistView.setImageView(imageView);
        SeekBar sprayPaintModeIntensitySeekBar = (SeekBar) findViewById(R.id.seekBarSprayPaintIntensity);
        sprayPaintModeIntensitySeekBar.setOnSeekBarChangeListener(this);
    }

    public void onButtonClickClear(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Clear Painting?")
                .setMessage("Do you really want to clear your painting?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(MainActivity.this, "Painting cleared", Toast.LENGTH_SHORT).show();
                        _impressionistView.clearPainting();
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }

    public void onButtonClickSetBrush(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    public void onButtonClickSavePainting(View v){
        Bitmap painting = _impressionistView.getCurrentPainting();
        if(painting == null){
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar calendar = new GregorianCalendar();
        String filename = "Impressionist painting ";
        filename += calendar.get(Calendar.YEAR) + "-";
        filename += calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " ";
        filename += "at " + calendar.get(Calendar.HOUR) + "." + calendar.get(Calendar.MINUTE) + "." +
                calendar.get(Calendar.SECOND);
        String description = "Image created by ImpressionistPainter434 on " + calendar.toString();


        String savedPath = MediaStore.Images.Media.insertImage(getContentResolver(), painting, filename, description);
        if(savedPath == null){
            Toast.makeText(this, "Error: painting could not be saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Saved painting to \"" + savedPath + "\"", Toast.LENGTH_SHORT).show();
        }
    }

    public void onButtonClickColorSampling(View v){
        boolean newValue = !_impressionistView.getUseAverageColorSampling();
        if(newValue){
            Toast.makeText(this, "Color Sampling Mode: Accurate", Toast.LENGTH_SHORT).show();
            try {
                ((Button) findViewById(R.id.buttonColorSampling)).setText(R.string.color_sampling_average);
            } catch (Exception e){
                e.printStackTrace();
                return;
            }
        } else {
            Toast.makeText(this, "Color Sampling Mode: Fast", Toast.LENGTH_SHORT).show();
            try {
                ((Button) findViewById(R.id.buttonColorSampling)).setText(R.string.color_sampling_point);
            } catch (Exception e){
                e.printStackTrace();
                return;
            }
        }
        _impressionistView.setUseAverageColorSampling(newValue);
    }

    public void updateSprayPaintTools(){
        try {
            View sprayPaintButton = findViewById(R.id.buttonSprayPaint);
            View sprayPaintSeekBar = findViewById(R.id.sprayPaintIntensityContainer);
            if (_impressionistView.getSprayPaintMode()) {
                sprayPaintButton.animate().alpha(0.25f);
                sprayPaintSeekBar.animate().alpha(1.0f);
            } else {
                sprayPaintButton.animate().alpha(1.0f);
                sprayPaintSeekBar.animate().alpha(0f);
            }
        } catch (NullPointerException e){
            return;
        }
    }

    public void onButtonClickSprayPaint(View v){
        boolean newMode = !_impressionistView.getSprayPaintMode();
        String newModeString;
        int toastLength;
        if(newMode){
            newModeString = "ON (Tap button again or select a brush to deactivate)";
            toastLength = Toast.LENGTH_LONG;
        } else {
            newModeString = "OFF";
            toastLength = Toast.LENGTH_SHORT;
        }
        Toast.makeText(this, "Spray Paint Mode: " + newModeString, toastLength).show();
        _impressionistView.setSprayPaintMode(newMode);
        updateSprayPaintTools();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuCircle:
                Toast.makeText(this, "Circle Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Circle);
                _impressionistView.setSprayPaintMode(false);
                updateSprayPaintTools();
                return true;
            case R.id.menuSquare:
                Toast.makeText(this, "Square Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Square);
                _impressionistView.setSprayPaintMode(false);
                updateSprayPaintTools();
                return true;
            case R.id.menuLine:
                Toast.makeText(this, "Line Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.Line);
                _impressionistView.setSprayPaintMode(false);
                updateSprayPaintTools();
                return true;
            case R.id.menuCircleSplatter:
                Toast.makeText(this, "Circle Splatter Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.CircleSplatter);
                _impressionistView.setSprayPaintMode(false);
                updateSprayPaintTools();
                return true;
            case R.id.menuSquareSoft:
                Toast.makeText(this, "Soft Square Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.SquareSoft);
                _impressionistView.setSprayPaintMode(false);
                updateSprayPaintTools();
                return true;
            case R.id.menuCircleSoft:
                Toast.makeText(this, "Soft Circle Brush", Toast.LENGTH_SHORT).show();
                _impressionistView.setBrushType(BrushType.CircleSoft);
                _impressionistView.setSprayPaintMode(false);
                updateSprayPaintTools();
                return true;
        }
        return false;
    }


    /**
     * Downloads test images to use in the assignment. Feel free to use any images you want. I only made this
     * as an easy way to get images onto the emulator.
     *
     * @param v
     */
    public void onButtonClickDownloadImages(View v){

        // Without this call, the app was crashing in the onActivityResult method when trying to read from file system
        FileUtils.verifyStoragePermissions(this);

        // Amazing Stackoverflow post on downloading images: http://stackoverflow.com/questions/15549421/how-to-download-and-save-an-image-in-android
        final BasicImageDownloader imageDownloader = new BasicImageDownloader(new BasicImageDownloader.OnImageLoaderListener() {

            @Override
            public void onError(String imageUrl, BasicImageDownloader.ImageError error) {
                Log.v("BasicImageDownloader", "onError: " + error);
            }

            @Override
            public void onProgressChange(String imageUrl, int percent) {
                Log.v("BasicImageDownloader", "onProgressChange: " + percent);
            }

            @Override
            public void onComplete(String imageUrl, Bitmap downloadedBitmap) {
                File externalStorageDirFile = Environment.getExternalStorageDirectory();
                String externalStorageDirStr = Environment.getExternalStorageDirectory().getAbsolutePath();
                boolean checkStorage = FileUtils.checkPermissionToWriteToExternalStorage(MainActivity.this);
                String guessedFilename = URLUtil.guessFileName(imageUrl, null, null);

                // See: http://developer.android.com/training/basics/data-storage/files.html
                // Get the directory for the user's public pictures directory.
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), guessedFilename);
                try {
                    boolean compressSucceeded = downloadedBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
                    FileUtils.addImageToGallery(file.getAbsolutePath(), getApplicationContext());
                    Toast.makeText(getApplicationContext(), "Saved to " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        for(String url: IMAGE_URLS){
            imageDownloader.download(url, true);
        }
    }

    /**
     * Loads an image from the Gallery into the ImageView
     *
     * @param v
     */
    public void onButtonClickLoadImage(View v){

        // Without this call, the app was crashing in the onActivityResult method when trying to read from file system
        FileUtils.verifyStoragePermissions(this);

        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    /**
     * Called automatically when an image has been selected in the Gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imageView = (ImageView) findViewById(R.id.viewImage);

                // destroy the drawing cache to ensure that when a new image is loaded, its cached
                imageView.destroyDrawingCache();
                imageView.setImageBitmap(bitmap);
                imageView.setDrawingCacheEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(seekBar.getId() == R.id.seekBarSprayPaintIntensity){
            float progressRatio = ((float) seekBar.getProgress())/((float) seekBar.getMax());
            _impressionistView.setSprayPaintEffectIntensity(progressRatio);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
