package com.taptester.tappapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.android.gms.plus.PlusShare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

//Created 06.08.2014 by Constantine

public class ShareActivity extends ActionBarActivity {

    private UiLifecycleHelper uiHelper;
    public final String consumer_key = "IyCvzlPWB2ZMvJCx1Bo8M6u44";
    public final String secret_key = "E1sPV4P6piw7HGQ0WwuQED802epGbfI7b6Nhsa5DP98w0mSu00";

    public static final String CONSUMER_KEY = "IyCvzlPWB2ZMvJCx1Bo8M6u44";
    public static final String CONSUMER_SECRET= "E1sPV4P6piw7HGQ0WwuQED802epGbfI7b6Nhsa5DP98w0mSu00";

    public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
    public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";

    public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";

    final public static String  CALLBACK_SCHEME = "x-latify-oauth-twitter";
    final public static String  CALLBACK_URL = CALLBACK_SCHEME + "://callback";

    File casted_image;
    Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", "Drive app");
        params.putString("caption", "Dialing and navigating have never been easier");
        params.putString("description", "Dialing and navigating have never been easier: a simple tap or slide of\n" +
                "your finger anywhere on the screen will dial your chosen contact or\n" +
                "navigate to your chosen destination");
        //params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "https://pp.vk.me/c625721/v625721167/24c6/3eP-lq5_ut8.jpg");

        WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(ShareActivity.this,
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            // When the story is posted, echo the success
                            // and the post Id.
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(ShareActivity.this,
                                        "Posted story, id: " + postId,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(ShareActivity.this.getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(ShareActivity.this.getApplicationContext(),
                                    "Publish cancelled",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(ShareActivity.this.getApplicationContext(),
                                    "Error posting story",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    public void shareFacebook(View v) {
        if (Session.getActiveSession() != null && Session.getActiveSession().isOpened()) {
            publishFeedDialog();
        }
        else {

            Session session = Session.getActiveSession();
            if (!session.isOpened() && !session.isClosed()) {

                //          List<String> permissions = new ArrayList<String>();
                //            permissions.add("email");

                session.openForRead(new Session.OpenRequest(this)
                        //                .setPermissions(permissions)
                        .setCallback(mFacebookCallback));
            } else {
                Session.openActiveSession(ShareActivity.this, true, mFacebookCallback);
            }
        }
    }

    private Session.StatusCallback mFacebookCallback = new Session.StatusCallback() {
        @Override
        public void
        call(final Session session, final SessionState state, final Exception exception) {

            if (state.isOpened()) {
                String facebookToken = session.getAccessToken();
                Log.i("MainActivityFaceBook", facebookToken);
                Request.newMeRequest(session, new Request.GraphUserCallback() {

                    @Override
                    public void onCompleted(GraphUser user,
                                            com.facebook.Response response) {
                        publishFeedDialog();
                    }
                }).executeAsync();
                //Prefs.setStringProperty(getActivity(), R.string.key_prefs_facebook_token, facebookToken);
            }
        }
    };

    public void shareMail(View v) {

        //Uri imageUri = Uri.parse("android.resource://" + getPackageName()
        //        + "/" + R.drawable.info);
        //Log.d("URI", String.valueOf(imageUri));

        FileOutputStream outStream;
        File file;
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.info);
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();

        file = new File(extStorageDirectory, "INFO.PNG");
        try {
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent email = new Intent(Intent.ACTION_SEND);
        String shareText = "Dialing and navigating have never been easier: a simple tap or slide of your finger anywhere on the screen will dial your chosen contact or navigate to your chosen destination\n" +
                "Setting up:\n" +
                "Easily set your most frequent contacts and destinations and you are ready to go.\n" +
                "For example: 1 TAP dials mom, 2 TAPS dials work," +
                "a slide up navigates you home…\n" +
                "If you wish you can record your command, which will sound every time you use the" +
                "same TAP/Slide, this will give you a confirmation without looking at the screen… \n\n" +
                "\n" +
                "Example: if 1 TAP dials mom, just record \"dialing mom\" \n" +
                " 1. Choose EDIT to set your TAP commands. (Click and hold edit button)\n" +
                " 2. Click the chosen TAP Slide\n" +
                " 3. Choose command from dialog box\n" +
                "    1. Dial > Choose contact from your contacts\n" +
                "    2. GPS > Set destination in GPS application\n" +
                "    3. Record > No further action (Auto rec.)\n" +
                "4. Optional: Record your command which will sound when you Tap/slide, for example: \"dialing home\" or \"navigating to the office”\n ";
        email.setType("application/octet-stream");
        //email.putExtra(Intent.EXTRA_EMAIL, new String[] { "" });
        email.putExtra(Intent.EXTRA_SUBJECT, "Use DriveApp");
        email.putExtra(Intent.EXTRA_TEXT, shareText);
        email.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    public void shareTwitter(View v) {

        Uri imageUri = Uri.parse("android.resource://" + getPackageName()
                + "/drawable/" + "info");
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, "I'm using Drive App!!!");
        tweetIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        tweetIntent.setType("image/*");

        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
            startActivity(tweetIntent);
        }else{
            Toast.makeText(this, "Twitter app isn't found", Toast.LENGTH_LONG).show();
        }

    }

    public void googleShare(View v) {

        Uri imageUri = Uri.parse("android.resource://" + getPackageName()
                + "/drawable/" + "info");

        Intent shareIntent = new PlusShare.Builder(this)
                .setType("text/plain")
                .setText("Dialing and navigating have never been easier: a simple tap or slide of your finger anywhere on the screen will dial your chosen contact or navigate to your chosen destination")
                .addStream(imageUri)
                .getIntent();

        startActivityForResult(shareIntent, 0);

    }

    public void shareLinkedIn(View v) {

        Uri imageUri = Uri.parse("android.resource://" + getPackageName()
                + "/drawable/" + "info");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("*/*");
        String shareText = "Dialing and navigating have never been easier: a simple tap or slide of your finger anywhere on the screen will dial your chosen contact or navigate to your chosen destination";
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(shareIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.linkedin.android")){
                shareIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
            startActivity(shareIntent);
        }else{
            Toast.makeText(this, "Linked In wasn't found on your device", Toast.LENGTH_LONG).show();
        }



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null
                && activeNetworkInfo.isConnectedOrConnecting();
    }

    public File String_to_File(String img_url) {

        try {
            File rootSdDirectory = Environment.getExternalStorageDirectory();

            casted_image = new File(rootSdDirectory, "attachment.jpg");
            if (casted_image.exists()) {
                casted_image.delete();
            }
            casted_image.createNewFile();

            FileOutputStream fos = new FileOutputStream(casted_image);

            URL url = new URL(img_url);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();
            InputStream in = connection.getInputStream();

            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = in.read(buffer)) > 0) {
                fos.write(buffer, 0, size);
            }
            fos.close();
            return casted_image;

        } catch (Exception e) {

            System.out.print(e);
            // e.printStackTrace();

        }
        return casted_image;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
