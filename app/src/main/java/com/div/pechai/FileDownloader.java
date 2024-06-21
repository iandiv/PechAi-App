package com.div.pechai;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDownloader {

    private static final String TAG = FileDownloader.class.getSimpleName();
    private static int extractNumber(String modelName) {
        // Use a regular expression to extract the numeric part
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(modelName);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            // Handle the case where no numeric part is found
            return 0; // or throw an exception, depending on your requirements
        }
    }
    public static void downloadFile(Context context, String url, String fileName, DownloadCallback callback) {
        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Downloading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Download file using AsyncTask
        new DownloadFileTask(context, progressDialog, fileName,callback).execute(url);
    }

    public interface DownloadCallback {
        void onDownloadComplete(String filePath);

    }
    private static class DownloadFileTask extends AsyncTask<String, Integer, String> {
        private final SharedPrefManager spm;
        private Context context;
        private ProgressDialog progressDialog;
        private String fileName;
        private final DownloadCallback downloadCallback;
        DownloadFileTask(Context context, ProgressDialog progressDialog, String fileName, DownloadCallback callback) {
            this.context = context;
            this.progressDialog = progressDialog;
            this.fileName = fileName;
            this.downloadCallback = callback;
        spm = new SharedPrefManager(context);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                URL downloadUrl = new URL(url);

                HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
                connection.connect();

                // Get file length
                int fileLength = connection.getContentLength();

                // Create a folder in external storage
                File pechaiFolder = new File(context.getExternalMediaDirs()[0] + "/PechAI");
                if (!pechaiFolder.exists()) {
                    pechaiFolder.mkdirs();
                }

                // Create file
                File outputFile = new File(pechaiFolder, fileName);
                FileOutputStream fos = new FileOutputStream(outputFile);

                // Download input stream
                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int total = 0;
                int count;

                while ((count = is.read(buffer)) != -1) {
                    total += count;
                    // Publish the progress
                    publishProgress((int) (total * 100 / fileLength));
                    fos.write(buffer, 0, count);
                }

                // Close streams
                fos.close();
                is.close();
                connection.disconnect();

                return outputFile.getAbsolutePath();

            } catch (IOException e) {
                Log.e(TAG, "Error downloading file: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if(result!=null){
                Toast.makeText(context, "Downloaded Successfuly: " /* + result*/, Toast.LENGTH_SHORT).show();
            if (downloadCallback != null) {
                downloadCallback.onDownloadComplete(result);
            }
            } else {
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show();
            }


    }
    }
}
