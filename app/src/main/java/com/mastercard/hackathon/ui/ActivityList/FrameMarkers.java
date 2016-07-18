/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.mastercard.hackathon.ui.ActivityList;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.Marker;
import com.vuforia.MarkerTracker;
import com.vuforia.State;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vec2F;
import com.vuforia.Vuforia;
import com.mastercard.hackathon.SampleApplicationControl;
import com.mastercard.hackathon.SampleApplicationException;
import com.mastercard.hackathon.SampleApplicationSession;
import com.mastercard.hackathon.renderers.FrameMarkerRenderer;
import com.mastercard.hackathon.utils.LoadingDialogHandler;
import com.mastercard.hackathon.utils.SampleApplicationGLView;
import com.mastercard.hackathon.utils.Texture;
import com.mastercard.hackathon.R;
import com.mastercard.hackathon.ui.SampleAppMenu.SampleAppMenu;
import com.mastercard.hackathon.ui.SampleAppMenu.SampleAppMenuGroup;
import com.mastercard.hackathon.ui.SampleAppMenu.SampleAppMenuInterface;


// The main activity for the FrameMarkers sample. 
public class FrameMarkers extends Activity implements SampleApplicationControl,
        SampleAppMenuInterface {
    private static final String LOGTAG = "FrameMarkers";

    SampleApplicationSession vuforiaAppSession;

    // Our OpenGL view:
    private SampleApplicationGLView mGlView;

    // Our renderer:
    private FrameMarkerRenderer mRenderer;

    // The textures we will use for rendering:
    private Vector<Texture> mTextures;


    private Marker marker;

    private GestureDetector mGestureDetector;

    private SampleAppMenu mSampleAppMenu;

    private RelativeLayout mUILayout;
    private View mBottomBar;
    private View mCameraButton;
    private PopupWindow infoPopupWndow;

    private int numProducts = 2;

    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
            this);

    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;

    boolean mIsDroidDevice = false;

    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);

        vuforiaAppSession = new SampleApplicationSession(this);

        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mGestureDetector = new GestureDetector(this, new GestureListener());

        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();

        mIsDroidDevice = Build.MODEL.toLowerCase().startsWith(
                "droid");
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable() {
                public void run() {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }

    }

    // Adds the Overlay view to the GLView
    private void addOverlayView(boolean initLayout) {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
                R.layout.camera_overlay_payments, null, false);

        mUILayout.setVisibility(View.VISIBLE);

        // If this is the first time that the application runs then the
        // uiLayout background is set to BLACK color, will be set to
        // transparent once the SDK is initialized and camera ready to draw
        if (initLayout) {
            mUILayout.setBackgroundColor(Color.BLACK);
        }

        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        // Gets a reference to the bottom navigation bar
        mBottomBar = mUILayout.findViewById(R.id.bottom_bar);

        // Gets a reference to the Camera button
        mCameraButton = mUILayout.findViewById(R.id.camera_button);

        // Gets a reference to the loading dialog container
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_layout);

        initializeBuildTargetModeViews();

        mUILayout.bringToFront();
    }

    // Initialize views
    private void initializeBuildTargetModeViews() {
        // Shows the bottom bar
        mBottomBar.setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(View.VISIBLE);
    }

    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures() {
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/watch-simple-tex.png",
                getAssets()));
        mTextures.add(Texture.loadTextureFromApk("FrameMarkers/TextureTeapotRed.png",
                getAssets()));
    }


    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume() {
        Log.d(LOGTAG, "onResume");
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null) {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOGTAG, "CHANGE");
        Toast.makeText(this, "ORIENTATION", Toast.LENGTH_LONG).show();
        /*
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
        */
    }


    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause() {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        if (mGlView != null) {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }

        try {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }
    }


    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy() {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();

        try {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e) {
            Log.e(LOGTAG, e.getString());
        }

        // Unload texture:
        mTextures.clear();
        mTextures = null;

        System.gc();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {


        return mGestureDetector.onTouchEvent(event);
    }


    private void startLoadingAnimation() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                null, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);

        // Shows the loading indicator at start
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }


    // Initializes AR application components.
    private void initApplicationAR() {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        mRenderer = new FrameMarkerRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        addOverlayView(true);
    }

    public void onBuyClick(View v) {
        Intent intent = new Intent(this, ChoiceActivity.class);
        int index = mRenderer.getProductIndex();
        intent.putExtra("product_name", getProductNames()[index]);
        intent.putExtra("product_price", getProductPrices()[index]);
        intent.putExtra("product_description", getProductDescriptions()[index]);
        startActivity(intent);
    }

    public void onLeftClick(View v) {
        int index = mRenderer.getProductIndex();
        index--;
        if (index < 0) {
            index = numProducts - 1;
        }
        TextView textView = (TextView) findViewById(R.id.product_name_price);
        textView.setText("€" + getProductPrices()[index] + " - " + getProductNames()[index]);
        mRenderer.setProductIndex(index);
    }

    public void onRightClick(View v) {
        int index = mRenderer.getProductIndex();
        index++;
        if (index == numProducts) {
            index = 0;
        }

        TextView textView = (TextView) findViewById(R.id.product_name_price);
        textView.setText("€" + getProductPrices()[index] + " - " + getProductNames()[index]);
        mRenderer.setProductIndex(index);
    }

    public void onInfoClick(View v) {

        mUILayout.setVisibility(View.INVISIBLE);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View popupView = layoutInflater.inflate(R.layout.info_popup, null);
        infoPopupWndow = new PopupWindow(
                popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        TextView textView = (TextView) popupView.findViewById(R.id.product_name);
        textView.setText(getProductNames()[mRenderer.getProductIndex()]);

        textView = (TextView) popupView.findViewById(R.id.product_price);
        textView.setText("€" + getProductPrices()[mRenderer.getProductIndex()]);

        textView = (TextView) popupView.findViewById(R.id.product_description);
        textView.setText(getProductDescriptions()[mRenderer.getProductIndex()]);

        infoPopupWndow.showAtLocation(popupView , Gravity.CENTER, 0, 0);
    }

    public void onInfoCloseClick(View v) {
        mUILayout.setVisibility(View.VISIBLE);
        infoPopupWndow.dismiss();
    }

    public void onCameraClick(View v) {
        mRenderer.takeScreenShot();
    }

    @Override
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        // Initialize the marker tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        Tracker trackerBase = trackerManager.initTracker(MarkerTracker
                .getClassType());
        MarkerTracker markerTracker = (MarkerTracker) (trackerBase);

        if (markerTracker == null) {
            Log.e(
                    LOGTAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }

        return result;

    }


    @Override
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
                .getTracker(MarkerTracker.getClassType());
        if (markerTracker == null)
            return false;

        marker = markerTracker.createFrameMarker(0, "MarkerQ", new Vec2F(
                50, 50));
        if (marker == null) {
            Log.e(LOGTAG, "Failed to create frame marker Q.");
            return false;
        }
        Log.i(LOGTAG, "Successfully initialized MarkerTracker.");
        return true;
    }


    @Override
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
                .getTracker(MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.start();

        return result;
    }


    @Override
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        MarkerTracker markerTracker = (MarkerTracker) tManager
                .getTracker(MarkerTracker.getClassType());
        if (markerTracker != null)
            markerTracker.stop();

        return result;
    }


    @Override
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        return result;
    }


    @Override
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(MarkerTracker.getClassType());

        return result;
    }


    @Override
    public void onInitARDone(SampleApplicationException exception) {

        if (exception == null) {
            initApplicationAR();

            mRenderer.mIsActive = true;

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e) {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (!result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            mSampleAppMenu = new SampleAppMenu(this, this, "Frame Markers",
                    mGlView, mUILayout, null);
            setSampleAppMenuSettings();

        } else {
            Log.e(LOGTAG, exception.getString());
            showInitializationErrorMessage(exception.getString());
        }
    }


    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message) {
        final String errorMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        FrameMarkers.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }


    @Override
    public void onVuforiaUpdate(State state) {
    }

    final public static int CMD_BACK = -1;

    // This method sets the menu's settings
    private void setSampleAppMenuSettings() {
        SampleAppMenuGroup group;

        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem(getString(R.string.menu_back), -1);

        mSampleAppMenu.attachMenu();
    }


    @Override
    public boolean menuProcess(int command) {
        boolean result = true;

        switch (command) {
            case CMD_BACK:
                finish();
                break;

        }

        return result;
    }


    private String[] getProductNames() {
        String[] names = new String[2];
        names[0] = "Designer Watch";
        names[1] = "Teapot";
        return names;
    }

    private double[] getProductPrices() {
        double[] prices = new double[2];
        prices[0] = 149.99;
        prices[1] = 39.99;
        return prices;
    }

    private String[] getProductDescriptions() {
        String[] descriptions = new String[2];
        descriptions[0] = "Hand-crafted by the blind watchmakers of southern Italy," +
                " this elegant chronograph is the perfect addition to any collection.";
        descriptions[1] = "This postminimalism-inspired teapot was designed" +
                " by famed sculptor Fran Stalinovskovich" +
                " and adds a touch of class to any teatime.";
        return descriptions;
    }

}
