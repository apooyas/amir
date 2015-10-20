package com.cosmepics.susa.cosmepics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class IntentActivity extends Activity {


	private static final int ACTION_TAKE_PHOTO = 1;
	private static final int ACTION_CHOOSE_PIC = 2;
	private static final int ACTION_CHOOSE_VIDEO = 3;
    private static final int ACTION_LOAD_CHART = 4;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";

/*
	private static final String VIDEO_STORAGE_KEY = "viewvideo";
	private static final String VIDEOVIEW_VISIBILITY_STORAGE_KEY = "videoviewvisibility";
	private VideoView mVideoView;
	private Uri mVideoUri;
*/

	public String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intent_activity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemID = item.getItemId();
        switch (itemID){
            case R.id.barPhoto:{
                dispatchTakePhotoIntent(ACTION_TAKE_PHOTO);
                break;
            }
            case R.id.barPic:{
                dispatchChoosePicIntent(ACTION_CHOOSE_PIC);
                break;
            }
            case R.id.barColorWheel:{
                dispatchChartPicIntent(ACTION_LOAD_CHART);
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private File getAlbumDir() {
		File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getString(R.string.album_name));
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("Cosmepics", "failed to create directory in External storage");
						return null;
					}
				}
			}
			
		} else {
            storageDir = this.getFilesDir();
			//Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}
		
		return storageDir;
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp;
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}

    //add the taken photo into phone gallery
	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentPhotoPath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
	}

	private void dispatchTakePhotoIntent(int actionCode) {

		Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
			}
		startActivityForResult(takePhotoIntent, actionCode);
	}

    private void dispatchChartPicIntent(int actionCode){
        Uri RGBchartUri = Uri.parse("test");
        RGBchartUri = Uri.parse("android.resource://com.cosmepics.susa.cosmepics/" + R.drawable.color_chart);

/*
        if (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
            RGBchartUri = Uri.parse("android.resource://com.cosmepics.susa.cosmepics/" + R.drawable.color_chart);}
        else{
            RGBchartUri = Uri.parse("android.resource://com.cosmepics.susa.cosmepics/" + R.drawable.color_chart_land);}
        }
*/
        Intent displayChartIntent = new Intent(this, PickColorActivity.class);
        displayChartIntent.putExtra("URI_ADDRESS", RGBchartUri);
        displayChartIntent.putExtra("IMAGE_SOURCE","Resources");
        startActivity(displayChartIntent);
    }

    private void dispatchChoosePicIntent(int actionCode){
        Intent choosePicIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(choosePicIntent, actionCode);
    }
    /*
	private void dispatchTakeVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		startActivityForResult(takeVideoIntent, ACTION_CHOOSE_VIDEO);
	}
	*/

	private void ChoosePic(Intent intent) {
        //Uri mChosenPicUri = intent.getData();
        Intent displayPicIntent = new Intent(this, PickColorActivity.class);
        displayPicIntent.putExtra("URI_ADDRESS", intent.getData());
        displayPicIntent.putExtra("IMAGE_SOURCE","Gallery");
        startActivity(displayPicIntent);
	}

	private void handleCameraPhoto() {

		if (mCurrentPhotoPath != null) {
            //add the newly taken photo to the phone gallery and return contentURI for that
            galleryAddPic();

            Intent displayPicIntent = new Intent(this, PickColorActivity.class);
            displayPicIntent.putExtra("IMAGE_SOURCE","Camera");
            displayPicIntent.putExtra("STRING_ADDRESS", mCurrentPhotoPath);
            startActivity(displayPicIntent);
		}

	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case ACTION_TAKE_PHOTO: {
                    if (resultCode == RESULT_OK) {
                        handleCameraPhoto();
                    }
                    break;
                } // ACTION_TAKE_PHOTO

                case ACTION_CHOOSE_PIC: {
                    if (resultCode == RESULT_OK) {
                        ChoosePic(data);
                    }
                    break;
                } // ACTION_CHOOSE_PIC

/*
		case ACTION_CHOOSE_VIDEO: {
			if (resultCode == RESULT_OK) {
				chooseVideo(data);
			}
			break;
*/

                // ACTION_CHOOSE_VIDEO
            } // switch
        //} //if
	}

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

}