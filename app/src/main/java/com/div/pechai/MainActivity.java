package com.div.pechai;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.div.ricecare.ml.Model;
//import com.div.ricecare.ml.Model;
//import com.div.ricecare.ml.Model;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String CONTRIBUTE = "https://pechai.site/contribute.php"; // Replace with your actual URL
    private static final String SERVER = "pechai.site";
    private static PhpApi phpApi;
    TextView result, accuracy;
    ImageView imageView, arrowImage;
    int imageSize = 224; //default
    //    Button capture;
    private String imageFilePath = "";
    private Bitmap bitmap;
    private ContentValues values;
    private Uri imageUri;
    private LinearLayout resultLayout;
    private LinearLayout captureLayout;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private AppBarLayout appbar_layout;
    private NestedScrollView nestedscrollview;
    private Button btnupd;
    private TextView txtInfo, txtCaus, txtRec;
    private TextView txtInfo_ceb, txtCaus_ceb, txtRec_ceb;
    private SharedPrefManager spm;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ImageButton notif;
    private TextView accu;
    private LinearLayout infocon,cebuanoLayout, englishLayout;
    private TextView txtversion;
    private Chip chipEng,chipCeb;

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();

        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    //    public void classifyImages(String imagePath) {
//        Interpreter.Options options = new Interpreter.Options();
//        GpuDelegate delegate = new GpuDelegate();
//        options.addDelegate(delegate);
//        Interpreter interpreter = null;
//        try {
//            interpreter = new Interpreter(FileUtil.loadMappedFile(this, "model_1.tflite"), options);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        // Load and preprocess the image
//
//
//        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
//        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(224 * 224 * 3 * 4).order(ByteOrder.nativeOrder());
//        inputBuffer.rewind();
//        bitmap.copyPixelsToBuffer(inputBuffer);
//        inputBuffer.rewind();
//
//        // Create input and output tensors
//        TensorBuffer inputTensor = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
//        inputTensor.loadBuffer(inputBuffer);
//
//        TensorBuffer outputTensor = TensorBuffer.createFixedSize(new int[]{1, 3}, DataType.FLOAT32);
//
//        // Run the inference
//        interpreter.run(inputTensor.getBuffer(), outputTensor.getBuffer().rewind());
//
//        // Get the predicted label and accuracy
//        float[] output = outputTensor.getFloatArray();
//        int predictedIndex = 0;
//        float maxProbability = 0.0f;
//        for (int i = 0; i < output.length; i++) {
//            if (output[i] > maxProbability) {
//                predictedIndex = i;
//                maxProbability = output[i];
//            }
//        }
//
//        String[] labels = {"Cabbage Moth", "Damping off", "Diamondback Moth"};
//        String predictedDisease = labels[predictedIndex];
//        float acc = maxProbability * 100.0f;
//
//        // Print the predicted label and accuracy
//        System.out.println("Predicted Disease: " + predictedDisease);
//        System.out.println("Accuracy: " + accuracy);
//
//
//// Print the predicted label and accuracy
//        collapsingToolbarLayout.setTitle(predictedDisease);
//
//
//        accuracy.setText("Accuracy: " + acc + "%");
//
//
//// Cleanup
//        interpreter.close();
//    }
    private static String imageToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private static int extractNumber(String modelName) {
        // Use a regular expression to extract the numeric part
        if(modelName.contains("model_")) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(modelName);

            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            } else {
                // Handle the case where no numeric part is found
                return 0; // or throw an exception, depending on your requirements
            }
        } else {
            return 0;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // In Activity's onCreate() for instance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        setContentView(R.layout.main);
//        PechAiUtil.copyAssetFileToExternalStorage(this, "model_1.tflite", "PechAI");
//        PechAiUtil.copyAssetFileToExternalStorage(this, "labels_1.txt", "PechAI");
        requestPermissions();
        spm = new SharedPrefManager(this);
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri image = data.getData();
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                        getContentResolver(), image);


                                imageView.setImageBitmap(bitmap);
                                displayImage(imageView);

                                if (imageView != null) {
                                    Drawable drawable = imageView.getDrawable();

                                    if (drawable != null && drawable.getConstantState() != null) {
                                        if (getResources().getDrawable(R.drawable.pechay).getConstantState().equals(drawable.getConstantState())) {

                                        } else {
                                            new ClassifyTask().execute(bitmap);

                                            captureLayout.setVisibility(View.GONE);
                                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {

                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);


                            imageView.setImageBitmap(bitmap);
                            displayImage(imageView);

                            if (imageView != null) {
                                Drawable drawable = imageView.getDrawable();

                                if (drawable != null && drawable.getConstantState() != null) {
                                    if (getResources().getDrawable(R.drawable.pechay).getConstantState().equals(drawable.getConstantState())) {

                                    } else {
                                        new ClassifyTask().execute(bitmap);

                                        captureLayout.setVisibility(View.GONE);
                                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                });


        phpApi = new PhpApi(this);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);
        imageView.setTag("default");
        resultLayout = findViewById(R.id.resultLayout);
        captureLayout = findViewById(R.id.captureLayout);
        captureLayout.setVisibility(View.VISIBLE);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        appbar_layout = findViewById(R.id.appbar_layout);
        nestedscrollview = findViewById(R.id.nestedscrollview);
//        imageViewCard = findViewById(R.id.imageViewCard);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        CollapsingToolbarLayout.LayoutParams layoutParams = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
        int actionBarHeight = obtainStyledAttributes(new int[]{android.R.attr.actionBarSize}).getDimensionPixelSize(0, -1);
        layoutParams.height = actionBarHeight + getStatusBarHeight();
        toolbar.setLayoutParams(layoutParams);


        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                captureLayout.setVisibility(View.VISIBLE);
                onBackBtn();


            }
        });
        // Set the initial title color
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.BLACK);
        if(captureLayout.getVisibility()==View.VISIBLE){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        }else{
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        }
        // Add an offset changed listener to AppBarLayout
        appbar_layout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // If the toolbar is fully collapsed

                Drawable drawable = toolbar.getNavigationIcon();
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
//COLLAPSED
                    if (drawable != null) {
                        drawable.setTint(Color.BLACK);
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }
                } else {
// EXPANDED if (verticalOffset == 0)
                    if (drawable != null) {
                        drawable.setTint(Color.WHITE);
                        if(captureLayout.getVisibility()==View.VISIBLE){
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                        }else{
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                        }

                    }
                } 
//                else {
////BETWEEN
//                    if (drawable != null) {
//                        drawable.setTint(Color.BLACK);
//                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//                    }
//                }
//                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
//                    // Set the title color to white only once when fully collapsed
//
//
//
//                } else {
//
//
//                }
            }
        });
//        capture = findViewById(R.id.capture);
//        choose = findViewById(R.id.choose);


        accuracy = findViewById(R.id.accuracy);
        accu = findViewById(R.id.accu);
        infocon = findViewById(R.id.infocon);
        englishLayout = findViewById(R.id.englishLayout);
        cebuanoLayout = findViewById(R.id.cebuanoLayout);
        englishLayout.setVisibility(View.GONE);
        chipCeb = findViewById(R.id.chipCeb);
        chipEng = findViewById(R.id.chipEng);

        if(spm.getBool("onEng")){
            onEnglish();
        }else{
            onCebuano();

        }

        txtInfo = findViewById(R.id.txtInfo);
        txtCaus = findViewById(R.id.txtCaus);
        txtRec = findViewById(R.id.txtRec);

        txtInfo_ceb = findViewById(R.id.txtInfo_ceb);
        txtCaus_ceb = findViewById(R.id.txtCaus_ceb);
        txtRec_ceb = findViewById(R.id.txtRec_ceb);

        txtversion = findViewById(R.id.txtversion);


        notif = findViewById(R.id.notif);
        notif.setImageResource(R.drawable.notif);
        notif.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                if (spm.getBool("firstLaunch")) {
                Toast.makeText(MainActivity.this, " Current Version: " + spm.getInt("modelVersion"), Toast.LENGTH_SHORT).show();


                PhpApi.getVersion(new PhpApi.DataVersionReceivedCallback() {
                        @Override
                        public void onDataVersionReceived(String model, String label) {
                            if (extractNumber(model) > spm.getInt("modelVersion")) {
                                notif.setImageResource(R.drawable.notif_red);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("A newer version of model is available. ");

                                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        FileDownloader.downloadFile(MainActivity.this, "https://" + SERVER + "/model_uploads/" + model, model,new FileDownloader.DownloadCallback() {
                                            @Override
                                            public void onDownloadComplete(String filePath) {
                                                // Handle download success
                                                spm.saveInt("modelVersion", extractNumber(model));
                                                txtversion.setText("Version 1.0 \nModel Version: "+((double)spm.getInt("modelVersion")));

                                                spm.saveString("modelFile", model);
                                            }
                                        });
                                        FileDownloader.downloadFile(MainActivity.this, "https://" + SERVER + "/model_uploads/" + label, label,new FileDownloader.DownloadCallback() {
                                            @Override
                                            public void onDownloadComplete(String filePath) {
                                                // Handle download success

                                                spm.saveString("labelFile", label);
                                            }
                                        });

                                        notif.setImageResource(R.drawable.notif);
                                        dialog.dismiss();
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Handle the OK button click

                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                                Toast.makeText(MainActivity.this, " New Version " + extractNumber(model) + " Available", Toast.LENGTH_SHORT).show();


                            }

//                Log.d("ReceivedData", "Latest ID: " + data);
                        }
                    });
//                }
            }
        });
        if (spm.getBool("firstLaunch")) {

            if (NetworkUtils.isInternetAvailable(this)) {
                PhpApi.getAllData(true);
                Toast.makeText(MainActivity.this, " Current Version: " + spm.getInt("modelVersion"), Toast.LENGTH_SHORT).show();
                txtversion.setText("Version 1.0 \nModel Version: "+((double)spm.getInt("modelVersion")));
                PhpApi.getVersion(new PhpApi.DataVersionReceivedCallback() {
                    @Override
                    public void onDataVersionReceived(String model, String label) {
                        if (extractNumber(model) > spm.getInt("modelVersion")) {
                            notif.setImageResource(R.drawable.notif_red);

                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            NotificationCompat.Builder builder = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                int importance = NotificationManager.IMPORTANCE_HIGH;
                                NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
                                notificationChannel.setShowBadge(true);
                                notificationManager.createNotificationChannel(notificationChannel);

                                builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
                            } else {
                                builder = new NotificationCompat.Builder(getApplicationContext());
                            }
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                            builder = builder

                                    .setContentIntent(pendingIntent)
                                    .setSmallIcon(R.mipmap.ic_launcher)
//                .setColor(ContextCompat.getColor(this, R.color.))
                                    .setContentTitle(getString(R.string.app_name))
                                    .setTicker("New Version Available")
                                    .setContentText("Update model to the  newer version.")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH) // Set the priority here
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setAutoCancel(true);
                            notificationManager.notify(143, builder.build());
                            Toast.makeText(MainActivity.this, " New Version " + extractNumber(model) + " Available", Toast.LENGTH_SHORT).show();

                        }

                Log.d("MODEL", "NEW VERSION: " +model);
                    }
                });

            }
        }else{
            phpApi.getAllData(false);
            PechAiUtil.copyAssetFileToExternalStorage(MainActivity.this, "model_1.tflite", "PechAI");
            PechAiUtil.copyAssetFileToExternalStorage(MainActivity.this, "labels_1.txt", "PechAI");
            spm.saveString("modelFile", "model_1.tflite");
            spm.saveString("labelFile", "labels_1.txt");
            spm.saveBool("firstLaunch", true);
        }



    }

    @Override
    public void onBackPressed() {
        onBackBtn();
    }

    private void onBackBtn(){

        if (captureLayout.getVisibility() == View.GONE) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do you want to leave this page?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Handle the OK button click
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

                    captureLayout.setVisibility(View.VISIBLE);
                    appbar_layout.setExpanded(true);
                    nestedscrollview.scrollTo(0, 0);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Handle the OK button click

                    dialog.dismiss();
                }
            });
            builder.show();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Do you want to exit?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Handle the OK button click
                    finish();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Handle the OK button click

                    dialog.dismiss();
                }
            });
            builder.show();

        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


            }
        }
    }

    private void displayImage(ImageView img) {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();

        display.getMetrics(outMetrics);
        int screenHeight = outMetrics.heightPixels;
        img.getLayoutParams().height = (int) (screenHeight * 0.6);

        Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        bm = Bitmap.createScaledBitmap(bm, imageSize, imageSize, false);


    }

    private String getImagePathFromUri(Uri uri) {
        String imagePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            imagePath = cursor.getString(columnIndex);
            cursor.close();
        }
        return imagePath;
    }

    public void onChoose(View v) {
        if (!spm.getBool("firstLaunch")) {
//                Toast.makeText(MainActivity.this, "First Launch" + spm.getBool("firstLaunch"), Toast.LENGTH_SHORT).show();
            Log.d("firstLaunch", "" + spm.getBool("firstLaunch"));
            spm.saveInt("modelVersion", extractNumber("model_1.tflite"));
//
//            PechAiUtil.copyAssetFileToExternalStorage(MainActivity.this, "model_1.tflite", "PechAI");
//            PechAiUtil.copyAssetFileToExternalStorage(MainActivity.this, "labels_1.txt", "PechAI");
            spm.saveString("modelFile", "model_1.tflite");
            spm.saveString("labelFile", "labels_1.txt");
            spm.saveBool("firstLaunch", true);
        }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(galleryIntent);
                } else {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

                }
            }
//        }
    }


    public void onUpdate(View v) {



        phpApi.setTextFromDB(txtInfo, collapsingToolbarLayout.getTitle().toString(), "information",
                new PhpApi.DataLoadCallback() {
                    @Override
                    public void onDataLoad() {
//                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
        phpApi.setTextFromDB(txtCaus, collapsingToolbarLayout.getTitle().toString(), "causes",
                new PhpApi.DataLoadCallback() {
                    @Override
                    public void onDataLoad() {
//                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
        phpApi.setTextFromDB(txtRec, collapsingToolbarLayout.getTitle().toString(), "recommendation",
                new PhpApi.DataLoadCallback() {
                    @Override
                    public void onDataLoad() {
//                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
        phpApi.setTextFromDB(txtInfo_ceb, collapsingToolbarLayout.getTitle().toString(), "information_ceb",
                new PhpApi.DataLoadCallback() {
                    @Override
                    public void onDataLoad() {
//                                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
        phpApi.setTextFromDB(txtCaus_ceb, collapsingToolbarLayout.getTitle().toString(), "causes_ceb",
                new PhpApi.DataLoadCallback() {
                    @Override
                    public void onDataLoad() {
//                                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
        phpApi.setTextFromDB(txtRec_ceb, collapsingToolbarLayout.getTitle().toString(), "recommendation_ceb",
                new PhpApi.DataLoadCallback() {
                    @Override
                    public void onDataLoad() {
//                                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onUpload(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Contribute");
        builder.setMessage("Uploading an image to our server will help us improve our AI model.");

        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Handle the OK button click
                phpApi.uploadImage(bitmap, collapsingToolbarLayout.getTitle().toString());

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Handle the OK button click

                dialog.dismiss();
            }
        });
        builder.show();

    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void classify(View v) {

    }

    public void onCapture(View v) {
        if (!spm.getBool("firstLaunch")) {
//                Toast.makeText(MainActivity.this, "First Launch" + spm.getBool("firstLaunch"), Toast.LENGTH_SHORT).show();
            Log.d("firstLaunch", "" + spm.getBool("firstLaunch"));
            spm.saveInt("modelVersion", extractNumber("model_1.tflite"));

            PechAiUtil.copyAssetFileToExternalStorage(MainActivity.this, "model_1.tflite", "PechAI");
            PechAiUtil.copyAssetFileToExternalStorage(MainActivity.this, "labels_1.txt", "PechAI");
            spm.saveString("modelFile", "model_1.tflite");
            spm.saveString("labelFile", "labels_1.txt");
            spm.saveBool("firstLaunch", true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        startActivityForResult(cameraIntent, 1);

                values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                startActivityForResult(intent, 1);
                takePictureLauncher.launch(intent);

            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);


            }
        }

    }


    class ClassifyTask extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Bitmap image = bitmaps[0];
            int imageSize = 224;
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);



            Interpreter interpreter = null;
            try {
                String modelPath = new File(getExternalMediaDirs()[0], "PechAI/" + spm.getString("modelFile")).getAbsolutePath();


                Interpreter.Options options = new Interpreter.Options();

                FileInputStream modelInputStream = new FileInputStream(new File(modelPath));
                FileChannel fileChannel = modelInputStream.getChannel();
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                interpreter = new Interpreter(mappedByteBuffer, options);



                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3).order(ByteOrder.nativeOrder());
                int[] intValue = new int[imageSize * imageSize];

                image.getPixels(intValue, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

                for (int val : intValue) {
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
//                for (int val : intValue) {
//                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f ));
//                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f ));
//                    byteBuffer.putFloat((val & 0xFF) * (1.f ));
//                }

                inputFeature0.loadBuffer(byteBuffer);

                String labelsPath = new File(getExternalMediaDirs()[0], "PechAI/" + spm.getString("labelFile")).getAbsolutePath();
                String[] labels = PechAiUtil.readTextFile(labelsPath);
                Log.d("labels", Arrays.toString(labels));

                float[][] outputFeature0 = new float[1][labels.length];
                interpreter.run(inputFeature0.getBuffer(), outputFeature0);

                int maxIndex = 0;
                float maxVal = outputFeature0[0][0];
                for (int i = 1; i < outputFeature0[0].length; i++) {
                    if (outputFeature0[0][i] > maxVal) {
                        maxVal = outputFeature0[0][i];
                        maxIndex = i;
                    }
                }

                // Get the predicted disease and accuracy
                String predictedDisease = labels[maxIndex];
                float acc = maxVal * 100.0f;

                return predictedDisease + "," + (int) acc;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                // Close the interpreter to release resources
                if (interpreter != null) {
                    interpreter.close();
                }
            }


        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                String[] parts = result.split(",");
                String predictedDisease = parts[0];
                String acc = parts[1];


                if (predictedDisease.equals("Invalid") ) {
                    infocon.setVisibility(View.GONE);
//                    Toast.makeText(MainActivity.this, acc+" true "+ predictedDisease, Toast.LENGTH_LONG).show();

                    collapsingToolbarLayout.setTitle("Invalid");

                    accu.setText("Result");
                    accuracy.setText("Invalid image");

                }else{
//                    Toast.makeText(MainActivity.this, acc+" false "+ predictedDisease, Toast.LENGTH_LONG).show();
                    if(predictedDisease.equals("Healthy")){
                        infocon.setVisibility(View.GONE);
                    }else {
                        infocon.setVisibility(View.VISIBLE);
                    }
                    collapsingToolbarLayout.setTitle(predictedDisease);
                    accu.setText("Result");
                    accuracy.setText("Confidence: " + acc + "%");

                }
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//


                        phpApi.setTextFromDB(txtInfo, collapsingToolbarLayout.getTitle().toString(), "information",
                                new PhpApi.DataLoadCallback() {
                                    @Override
                                    public void onDataLoad() {
//                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        phpApi.setTextFromDB(txtCaus, collapsingToolbarLayout.getTitle().toString(), "causes",
                                new PhpApi.DataLoadCallback() {
                                    @Override
                                    public void onDataLoad() {
//                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        phpApi.setTextFromDB(txtRec, collapsingToolbarLayout.getTitle().toString(), "recommendation",
                                new PhpApi.DataLoadCallback() {
                                    @Override
                                    public void onDataLoad() {
//                        Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        phpApi.setTextFromDB(txtInfo_ceb, collapsingToolbarLayout.getTitle().toString(), "information_ceb",
                                new PhpApi.DataLoadCallback() {
                                    @Override
                                    public void onDataLoad() {
//                                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        phpApi.setTextFromDB(txtCaus_ceb, collapsingToolbarLayout.getTitle().toString(), "causes_ceb",
                                new PhpApi.DataLoadCallback() {
                                    @Override
                                    public void onDataLoad() {
//                                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        phpApi.setTextFromDB(txtRec_ceb, collapsingToolbarLayout.getTitle().toString(), "recommendation_ceb",
                                new PhpApi.DataLoadCallback() {
                                    @Override
                                    public void onDataLoad() {
//                                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                    }
                                });

//                    }
//                }, 2000);
            } else {
                // Handle error case
            }
        }
    }
    public void onCeb(View v){
        onCebuano();

    }
    public void onCebuano(){

        chipEng.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.whitedark)));
        chipEng.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.black)));
        chipCeb.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.darkestGreen)));
        chipCeb.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
        if(cebuanoLayout.getVisibility()==View.GONE){
            cebuanoLayout.setVisibility(View.VISIBLE);
            englishLayout.setVisibility(View.GONE);
            spm.saveBool("onEng",false);
        }
    }
    public void onEng(View v){

        onEnglish();
    }
    public void onEnglish(){
        chipEng.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.darkestGreen)));
        chipEng.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));

        chipCeb.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.whitedark)));
        chipCeb.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.black)));

        if(englishLayout.getVisibility()==View.GONE){
            englishLayout.setVisibility(View.VISIBLE);
            cebuanoLayout.setVisibility(View.GONE);
            spm.saveBool("onEng",true);
        }
    }
//    class ImageProcessingTask extends AsyncTask<Uri, Void, Bitmap> {
//
//        @Override
//        protected Bitmap doInBackground(Uri... params) {
//            try {
//                Bitmap thumbnail = MediaStore.Images.Media.getBitmap(
//                        getContentResolver(), imageUri);
////                thumbnail = Bitmap.createScaledBitmap(thumbnail, (int) (thumbnail.getWidth() * 0.9), (int) (thumbnail.getHeight() * 0.9), false);
//
//                return thumbnail;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            if (bitmap != null) {
//                // Set the ImageView bitmap and layout params on the UI thread
//                imageView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        imageView.setImageBitmap(bitmap);
//
//                        Display display = getWindowManager().getDefaultDisplay();
//                        DisplayMetrics outMetrics = new DisplayMetrics();
//
//                        display.getMetrics(outMetrics);
//                        int screenHeight = outMetrics.heightPixels;
//
//
//                        imageView.getLayoutParams().height = (int) (screenHeight * 0.8);
//
//
//                    }
//                });
//
////                Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
////                bm = Bitmap.createScaledBitmap(bm, imageSize, imageSize, false);
//
////                classifyImage(bm);
////                captureLayout.setVisibility(View.GONE);
//
//
////
//
//            }
//        }
//    }
//

}
