package com.cosmepics.susa.cosmepics;

import android.app.ActionBar;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;


public class PickColorActivity extends Activity {
    int globalPickedRGB=0;
    private Menu pickColorMenu;
    Bitmap globalBitmap, globalMagBitmap;
    ImageView globalIV;
    Point globalScreenSize;

    //Canvas mCanvas;
    //ScaleGestureDetector mScaleDetector;
    //private GestureDetectorCompat mDetector;
    //private float scale = 1f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_color);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO );
        globalScreenSize = getScreenSize();

/* used only for test
        touchedXY = (TextView) findViewById(R.id.xy);
        invertedXY = (TextView) findViewById(R.id.invertedxy);
        imgSize = (TextView) findViewById(R.id.size);
        vSize = (TextView) findViewById(R.id.vSize);
        colorRGB = (TextView) findViewById(R.id.colorrgb);
*/
        globalIV= (ImageView) findViewById(R.id.displayPicView);
        String mImagePath;
        String sourceType = getIntent().getStringExtra("IMAGE_SOURCE");
        switch (sourceType){
            case "Camera": {
                mImagePath = getIntent().getStringExtra("STRING_ADDRESS");
                displayPhoto(mImagePath);
                break;
            }
            case "Gallery": {
                mImagePath = getPathFromURI(Uri.parse(getIntent().getParcelableExtra("URI_ADDRESS").toString()));
                displayPhoto(mImagePath);
                break;
            }
            case "Resources": {
                displayRGBchart();
                break;
            }
        }
        globalIV.setOnTouchListener(imgOnTouchListener);

        // Listener for pinch zoom
        //mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        //iv.setOnTouchListener(imgOnTouchListener);

    }


    private void displayRGBchart(){
        try {
            globalIV.setImageResource(R.drawable.color_chart);
            globalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.color_chart);
        }
        catch (Exception e) {
            System.out.println("Exception in displayRGBChart()");
            e.printStackTrace();
        }
    }

    private void displayPhoto(String imagePath) { //Displays optimized size image file

        try {
            //Get path of the image file

            Point mImageSize = getImageSize(imagePath);

            // Set bitmap options to scale the image decode target
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = getScaleFactor(mImageSize, globalScreenSize);
            //bmOptions.inPurgeable = true;
            Bitmap mBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
            Matrix rotateMatrix = new Matrix();
            int rotation = getPhotoOrientation(imagePath);
            rotateMatrix.preRotate(rotation);
            globalBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), rotateMatrix, true);

            //Display the fitted image
            if (globalBitmap != null) {
                globalIV.setImageDrawable(new BitmapDrawable(getResources(), globalBitmap));
            }
        }
        catch (Exception e) {
            System.out.println("Exception in displayPhoto()");
            e.printStackTrace();
        }

    }

    private Point getImageSize(String imageFilePath) { //Get the size of the image from gallery or camera
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFilePath,bmOptions);

        Point size=new Point();
        size.x = bmOptions.outWidth;
        size.y = bmOptions.outHeight;
        return (size);
    }

    private Point getScreenSize() {//Get the size of screen
        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        return (screenSize);
    }

    private int getScaleFactor(Point imageSize, Point screenSize){

        // Figure out which way needs to be reduced less in combination of screen orientation vs image orientation
        int scaleFactor;
        scaleFactor = Math.min(Math.max(imageSize.x,imageSize.y)/Math.max(screenSize.x,screenSize.y),Math.min(imageSize.x, imageSize.y)/Math.min(screenSize.x, screenSize.y));
        scaleFactor = (scaleFactor>1)? scaleFactor:1;
        return (scaleFactor);
    }

    public static int getPhotoOrientation(String imagePath){
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    View.OnTouchListener imgOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float[] touchedPoint = new float[]{event.getX(),event.getY()};
            int maxWidth = globalBitmap.getWidth();
            int maxHeight = globalBitmap.getHeight();
            final float[] magSquareAnchor = new float[]{0,0};
            float[] touchedSpotAnchor=new float[]{0,0};

            //aligning touched point coordinates with the scale image coordinates by inverting
            Matrix invertMatrix = new Matrix();
            globalIV.getImageMatrix().invert(invertMatrix);
            invertMatrix.mapPoints(touchedPoint);

            //Just break if user touches outside the boundary of the image
            if ((touchedPoint[0]>maxWidth)
                    || (touchedPoint[1]>maxHeight)
                    || (touchedPoint[0]<0)
                    || (touchedPoint[1])<0){
                return false;
            }

            //Visual settings --> all settings to be moved into resources
            //my paint
            Paint myPaint = new Paint();
            myPaint.setARGB(255, 255, 255, 255);
            myPaint.setStyle(Paint.Style.STROKE);

            // dynamically set the stroke width per screen density
            myPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.strokeWidth));

            myPaint.setAntiAlias(true);
            myPaint.setDither(true);
            myPaint.setStrokeJoin(Paint.Join.ROUND);
            myPaint.setStrokeCap(Paint.Cap.ROUND);
            int touchedSpotSize = getResources().getInteger(R.integer.touchedSpotSize);
            int spotFrameStroke = getResources().getInteger(R.integer.spotFrameStroke);
            int globalMagGlassSize= (int) getResources().getDimension(R.dimen.magGlassSize);
            int magTileSize = globalMagGlassSize/touchedSpotSize;

            //Define magnifier array to hold color tiles
            final magTile[][] magArray = new magTile[touchedSpotSize][touchedSpotSize];

            //setting up the touched spot
            RectF touchedSpot = new RectF();

            //handles situations where edge of the picture is touched
            if (touchedPoint[0] < touchedSpotSize){
                touchedSpot.left=0;
                touchedSpot.right=touchedSpotSize-1;
                touchedPoint[0]=touchedSpotSize/2;

            }
            else if(maxWidth - touchedPoint[0] < touchedSpotSize) {
                touchedSpot.left = maxWidth-touchedSpotSize;
                touchedSpot.right = maxWidth;
                touchedPoint[0]= maxWidth - touchedSpotSize/2;
            }
            else {
                touchedSpot.left=touchedPoint[0]-touchedSpotSize/2;
                touchedSpot.right=touchedPoint[0]+touchedSpotSize/2;
            }

            if (touchedPoint[1] < touchedSpotSize){
                touchedSpot.top=0;
                touchedSpot.bottom=touchedSpotSize-1;
                touchedPoint[1]= touchedSpotSize/2;
            }
            else if(maxHeight - touchedPoint[1] < touchedSpotSize) {
                touchedSpot.top = maxHeight-touchedSpotSize;
                touchedSpot.bottom = maxHeight;
                touchedPoint[1]=maxHeight - touchedSpotSize/2;
            }
            else {
                touchedSpot.top=touchedPoint[1]-touchedSpotSize/2;
                touchedSpot.bottom=touchedPoint[1]+touchedSpotSize/2;
            }

            //Create a new image bitmap and attach a brand new canvas to it
            //Bitmap tempBitmap = Bitmap.createBitmap(globalScreenSize.x,globalScreenSize.y, Bitmap.Config.ARGB_8888);
            Bitmap tempBitmap = globalBitmap.copy(Bitmap.Config.RGB_565,true);
            Canvas tempCanvas = new Canvas(tempBitmap);


            //Draw the image bitmap into the canvas
            tempCanvas.drawBitmap(globalBitmap, 0, 0, null);

            //Draw square around touched spot
            myPaint.setStrokeWidth(spotFrameStroke);
            tempCanvas.drawRect(touchedSpot.left-spotFrameStroke,touchedSpot.top-spotFrameStroke,touchedSpot.right+spotFrameStroke,touchedSpot.bottom+spotFrameStroke,myPaint);


            //Setup magnifier glass
            Bitmap globalMagGlassBitmap = Bitmap.createBitmap(globalMagGlassSize, globalMagGlassSize, Bitmap.Config.ARGB_8888);
            Canvas magCanvas = new Canvas(globalMagGlassBitmap);
            int touchedRGB=0;
            myPaint.setStyle(Paint.Style.FILL);

            //draw tiles in the magnifier glass
            for (int i = 0; i < touchedSpotSize; i++){
                for (int j = 0; j < touchedSpotSize; j++){
                    touchedRGB = tempBitmap.getPixel((int) touchedPoint[0] - touchedSpotSize / 2 + i, (int) touchedPoint[1] - touchedSpotSize / 2 + j);
                    myPaint.setColor(touchedRGB);
                    magArray[i][j]= new magTile(i * magTileSize, j * magTileSize, (i + 1) * magTileSize, (j + 1) * magTileSize, touchedRGB);
                    magCanvas.drawRect(magArray[i][j].getTile().left,magArray[i][j].getTile().top,magArray[i][j].getTile().right,magArray[i][j].getTile().bottom,myPaint);
                }
            }

            //initializing the magnifier Bitmap to be used to refresh the magnifier image view when retouched
            globalMagBitmap = globalMagGlassBitmap;

            //alignment of magnifying glass
            RelativeLayout.LayoutParams magGlassLayoutParams =  new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (touchedSpot.left > maxWidth/2){
                magGlassLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                touchedSpotAnchor[0] = touchedSpot.left-spotFrameStroke;
                magSquareAnchor[0]=globalMagGlassSize;
            }
            else{
                magGlassLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                touchedSpotAnchor[0] = touchedSpot.right+spotFrameStroke;
                magSquareAnchor[0]=globalScreenSize.x-globalMagGlassSize;
            }

            if (touchedSpot.top > maxHeight/2){
                magGlassLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                touchedSpotAnchor[1] = touchedSpot.top-spotFrameStroke;
                magSquareAnchor[1]=globalMagGlassSize;
            }
            else{
                magGlassLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                touchedSpotAnchor[1] = touchedSpot.bottom+spotFrameStroke;
                magSquareAnchor[1]=globalScreenSize.y-globalMagGlassSize;
            }

            invertMatrix.mapPoints(magSquareAnchor);

            ImageButton magGlass = (ImageButton) findViewById(R.id.magGlass);
            magGlass.setLayoutParams(magGlassLayoutParams);

            //Draw connecting line
            myPaint.setARGB(255, 255, 255, 255);
            tempCanvas.drawLine(touchedSpotAnchor[0],touchedSpotAnchor[1],
                    magSquareAnchor[0],magSquareAnchor[1],myPaint);

            //Attach the canvas to the ImageView
            globalIV.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

            //Attach the magCanvas to the magnifier glass
            magGlass.setImageDrawable(new BitmapDrawable(getResources(), globalMagGlassBitmap));
            magGlass.setVisibility(View.VISIBLE);
            magGlass.bringToFront();


            //Handle magnifier glass interactions
            View.OnTouchListener magListener=new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    float[] magTouchedPoint = new float[]{event.getX(),event.getY()};
                    Paint magPaint = new Paint();
                    magPaint.setARGB(255, 255, 255, 255);
                    magPaint.setStyle(Paint.Style.STROKE);
                    magPaint.setStrokeWidth(2);
                    int globalMagGlassSize = (int) getResources().getDimension(R.dimen.magGlassSize);
                    int touchedSpotSize = getResources().getInteger(R.integer.touchedSpotSize);
                    int magTileSize = globalMagGlassSize/touchedSpotSize;

                    //RelativeLayout globalMagGlassLayout = (RelativeLayout) findViewById(R.id.picColorContainer);

                    //No invert matrix is required for the magnifier glass but globalMagGlassSize must be in dp to scale according to device size
                    ImageView iv ;
                    iv = (ImageView) findViewById(R.id.magGlass);
                    /*Bitmap magBitmap = new Bitmap();
                    magBitmap.recycle();*/
                    Bitmap magBitmap = Bitmap.createBitmap(globalMagBitmap);
                    //magBitmap =((BitmapDrawable)iv.getDrawable()).getBitmap();
                    Canvas magCanvas = new Canvas(magBitmap);

                    //Identifying which tile was touched by determining x,y index of the tile
                    int tileIndex1, tileIndex2;
                    tileIndex1 = (int)(magTouchedPoint[0]/magTileSize);
                    tileIndex2 = (int)(magTouchedPoint[1]/magTileSize);

                    //Ensuring the tileIndex is within magArray's bound
                    if (tileIndex1<0){
                        tileIndex1=0;} else
                            if (tileIndex1>=magArray.length){
                                tileIndex1=magArray.length-1;
                            }
                    if (tileIndex2<0){
                        tileIndex2=0;} else
                            if (tileIndex2>=magArray[0].length) {
                                tileIndex2 = magArray[0].length - 1;
                            }

                    //Deselect previously selected tile, if exists
                    for (int i=0;i<touchedSpotSize;i++)
                        for(int j=0;j<touchedSpotSize;j++){
                            magArray[i][j].isSelected=false;
                        }

                    //set new tile to Selected=true
                    magArray[tileIndex1][tileIndex2].isSelected = true;

                    //highlight new selected tile
                    magCanvas.drawRect(magArray[tileIndex1][tileIndex2].getTile().left,magArray[tileIndex1][tileIndex2].getTile().top,magArray[tileIndex1][tileIndex2].getTile().right,magArray[tileIndex1][tileIndex2].getTile().bottom,magPaint);
                    iv.setImageDrawable(new BitmapDrawable(getResources(), magBitmap));


                    //call the next step in the process and pass RGB code of the selected tile to it
                    globalPickedRGB = magArray[tileIndex1][tileIndex2].color;
                    //invalidateOptionsMenu();
                    pickColorMenu.findItem(R.id.color_box).getActionView().setBackgroundColor(globalPickedRGB);
                    pickColorMenu.findItem(R.id.color_box).setVisible(true);
                    pickColorMenu.findItem(R.id.action_ok).setVisible(true);
                    pickColorMenu.findItem(R.id.action_ok).setEnabled(true);


                    //Just for test for Payam ============================================
                   /* Button okButton =  (Button)findViewById(R.id.okButton);
                    okButton.setVisibility(View.VISIBLE);
                    okButton.bringToFront();
                    okButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            dispatchSearchProduct();
                        }
                    });*/
                    //Just for test for Payam ============================================

                    return true;
                }
            };
            magGlass.setOnTouchListener(magListener);

            return true;
        }
    };



    public void dispatchSearchProduct(){
        Intent searchProductsIntent = new Intent(getApplicationContext(), pListActivity.class);
        searchProductsIntent.putExtra("mColor", globalPickedRGB);
        startActivity(searchProductsIntent);
    }


/*

    @ Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        return true;
    }



    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            mMatrix.setScale(scale, scale);
            iv.setImageMatrix(mMatrix);
            return true;
        }
    }

    private void fitImage(){

        Drawable drawing = iv.getDrawable();
        if (drawing == null) {
            return; // Checking for null & return, as suggested in comments
        }
        Bitmap bitmap = ((BitmapDrawable)drawing).getBitmap();

        //Get display size of the device
        Display mDisplay = getWindowManager().getDefaultDisplay();
        Point mSize= new Point();
        mDisplay.getSize(mSize);
        int displayW = mSize.x;
        int displayH = mSize.y;

        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = Math.min(displayH, displayW);

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        // Apply the scaled bitmap
        iv.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
        params.width = width;
        params.height = height;
        iv.setLayoutParams(params);

    }


*/


    private String getPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result= cursor.getString(column_index);
        cursor.close();
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.pickColorMenu = menu;
        getMenuInflater().inflate(R.menu.menu_pic_color, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id==R.id.action_ok){
            dispatchSearchProduct();
        }
        return super.onOptionsItemSelected(item);
    }
}



/*
            Getting image orientation by querying MediaStore


            File f=new File(imagePath);
            Uri imageUri = Uri.parse(String.valueOf(f.toURI()));
            Cursor mCursor = getApplicationContext().getContentResolver().query(imageUri,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
            Cursor mCursor3 = getApplicationContext().getContentResolver().query(tempUri3,
                    new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
            if (mCursor.getCount() == 1) {
                mCursor.moveToFirst();
                int orientation =  mCursor.getInt(0);
                rotateMatrix.preRotate(orientation);
            }*/