package com.div.pechai;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PhpApi {

    private static final String SERVER = "pechai.site";
    private static final String BASE_URL = "https://"+SERVER+"/read-api.php"; // Replace with your actual URL
    private static final String CLASSNAMES = "https://"+SERVER+"/classnames-api.php"; // Replace with your actual URL
    private static final String CONTRIBUTE = "https://"+SERVER+"/contribute-api.php"; // Replace with your actual URL

    private static final String VERSION = "https://"+SERVER+"/version-api.php"; // Replace with your actual URL

    static Context context;
    private static RequestQueue requestQueue;
    private static SharedPrefManager sharedPrefManager;
    private static boolean firstLaunch = false;

    public PhpApi(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        sharedPrefManager = new SharedPrefManager(context);
        this.context = context;
        firstLaunch = sharedPrefManager.getBool("firstLaunch");
    }

    private static void getData(String classname, DataReceivedCallback callback) {
        readData(classname, response -> {
//
            callback.onDataReceived(response);
        }, error -> {
            callback.onDataReceived(String.valueOf(error));

        });
    }

    private static void getDataFile(String classname, DataReceivedCallback callback) {
        try {
            // Read data from the local JSON file "file.json"
            InputStream inputStream = context.getAssets().open("file.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, "UTF-8");

            // Parse the JSON data and find the object with the specified "classname"
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String currentClassname = jsonObject.optString("classname");

                // Check if the current object matches the specified classname
                if (currentClassname.equals(classname)) {
                    // Send the JSON content of the matching object as a response
                    callback.onDataReceived(jsonObject.toString());
                    return; // Exit the loop since we found a match
                }
            }

            // If no matching object was found, you can handle it accordingly
            callback.onDataReceived("Classname not found in JSON");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            callback.onDataReceived("Error reading JSON file");
        }
    }
    public static void getAllData(Boolean fromdb) {
        if (fromdb) {
            getClassNames();
        } else {
            try {
//                Toast.makeText(context, "from Asset", Toast.LENGTH_SHORT).show();


                JSONArray jsonArray = loadJSONFromAsset(context, "file.json");

                String[] classNames = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = null;

                    jsonObject = jsonArray.getJSONObject(i);

                    String classes = jsonObject.optString("classname");

                    String json = jsonObject.toString();
                    sharedPrefManager.saveString(classes, json);

                    classNames[i] = classes;
                }

                sharedPrefManager.saveString("classNames", new JSONArray(classNames).toString());
                firstLaunch = true;
                sharedPrefManager.saveBool("firstLaunch", firstLaunch);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void getClassNames() {
        readClassnames(response -> {

            try {

//                Toast.makeText(context, "from DB", Toast.LENGTH_SHORT).show();

                JSONArray jsonArray = new JSONArray(response);
                String[] classNames = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = null;
                    jsonObject = jsonArray.getJSONObject(i);
                    String classes = jsonObject.optString("classname");

                    getData(classes, new PhpApi.DataReceivedCallback() {
                        @Override
                        public void onDataReceived(String data) {
                            sharedPrefManager.saveString(classes, data);
//                                Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
                        }
                    });
                    classNames[i] = classes;
                }
                String val = new JSONArray(classNames).toString();
                if(!val.isEmpty()){
                    sharedPrefManager.saveString("classNames", val);
                }



            } catch (JSONException e) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
//                throw new RuntimeException(e);
            }

        }, error -> {

        });
    }

    private static void readClassnames(final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CLASSNAMES,
                response -> {


                    Log.d("readClassnames", response);
                    listener.onResponse(response);
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Log.e("Error", "HTTP status code: " + statusCode);
                    }
                    // Handle errors
//                    Log.e("Error", "Error occurred", error);
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Add parameters for the 'read' operation
                Map<String, String> params = new HashMap<>();
                params.put("read", "true");

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private static void readData(String data, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL,
                response -> {
                    // Handle the response from the server

                    Log.d("Response", response);
                    listener.onResponse(response);
                },
                error -> {
                    // Handle errors
                    Log.e("Error", "Error occurred", error);
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Add parameters for the 'read' operation
                Map<String, String> params = new HashMap<>();
                params.put("read", "true");
                params.put("disease", data);

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    public void uploadImage(Bitmap bitmap,String classname) {
        upload(bitmap,classname, response -> {

        }, error -> {

        });
    }
    private static void upload(Bitmap bitmap,String classname, final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false); // Prevent users from dismissing the dialog
        progressDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Handle the OK button click
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, CONTRIBUTE,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        response = jsonObject.getString("Message");
                    }catch (JSONException e) {
                        response = "Error: Check your Internet.";
                    }
                    Log.d("Response", response);
                    listener.onResponse(response);
                    progressDialog.dismiss();

                    builder.setMessage(response);
                    builder.show();
                },
                error -> {
                    // Handle errors

                    builder.setMessage(error.toString());
                    builder.show();
                    Log.e("Error", "Error occurred", error);
                    errorListener.onErrorResponse(error);
                    progressDialog.dismiss();
                } ) {
            @Override
            protected Map<String, String> getParams() {
                // Add parameters for the 'read' operation
                Map<String, String> params = new HashMap<>();
                params.put("submit", "true");
                params.put("classname", classname);
                params.put("description", "");
                params.put("image", imageToBase64(bitmap));

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private static String imageToBase64(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static JSONArray loadJSONFromAsset(Context context, String filename) {
        JSONArray jsonArray = null;
        try {
            // Open the JSON file from the assets folder
            InputStream inputStream = context.getAssets().open(filename);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            // Convert the input stream content to a JSON string
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Parse the JSON string into a JSONArray
            jsonArray = new JSONArray(json);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public boolean isFirstLaunch() {
        return firstLaunch;
    }

    public void setTextFromDB(TextView txt, String key, String valueOf, DataLoadCallback callback) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                String data = sharedPrefManager.getStringIgnoreCase(key);

                if (data != null && !data.isEmpty()) {
                    try {
                        txt.setText(new JSONObject(data).optString(valueOf, txt.getText().toString()));

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    txt.setText(txt.getText().toString());
                }
                if (callback != null) {
                    callback.onDataLoad();
                }

            }
        }, 500);


    }

    private static void readVersion(final Response.Listener<String> listener, final Response.ErrorListener errorListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, VERSION,
                response -> {
                    // Handle the response from the server
                    Log.d("Response Version", response);
                    listener.onResponse(response);
                },
                error -> {
                    // Handle errors
                    Log.e("Error Version", "Error occurred", error);
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Add parameters for the 'read' operation
                Map<String, String> params = new HashMap<>();
                params.put("read", "true");
                return params;
            }
        };

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }

    public static void getVersion(DataVersionReceivedCallback callback) {
        readVersion(response -> {
            // Parse the JSON response to get the latest
            try {
                JSONObject jsonResponse = new JSONObject(response);
                String latest_model= jsonResponse.getString("latest_model");
                String latest_label= jsonResponse.getString("latest_label");

                callback.onDataVersionReceived(latest_model,latest_label);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onDataVersionReceived("Error parsing JSON","Error parsing JSON");
            }
        }, error -> {
            callback.onDataVersionReceived(String.valueOf(error),String.valueOf(error));
        });
    }
    // Define a callback interface
    public interface DataReceivedCallback {
        void onDataReceived(String data);

    }
    public interface DataVersionReceivedCallback {
        void onDataVersionReceived(String latestModel, String latestLabel);
    }
    public interface DataLoadCallback {

        void onDataLoad();
    }
}
