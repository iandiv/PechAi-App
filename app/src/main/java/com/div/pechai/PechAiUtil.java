package com.div.pechai;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PechAiUtil {

    public static String[] readTextFileToArray(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            // Open the file from the assets folder
            InputStream inputStream = context.getAssets().open(fileName);

            // Use BufferedReader to read the file
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Read each line of the file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            // Close the BufferedReader
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Split the content into an array based on newline character
        return stringBuilder.toString().split("\n");
    }
    public static String[] readTextFile(String filePath) {
        ArrayList<String> lines = new ArrayList<>();

        try {
            File file = new File(filePath);

            if (file.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    // Add each line to the ArrayList
                    lines.add(line);
                }

                bufferedReader.close();
            } else {
                // Handle the case where the file doesn't exist
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IOException
        }

        // Convert the ArrayList to a String array
        String[] result = new String[lines.size()];
        lines.toArray(result);

        return result;
    }
    public static void copyAssetFileToExternalStorage(Context context, String assetFileName, String destinationDirectory) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        FileOutputStream out;
// If you have access to the external storage, do whatever you need

                try {
                    // Create the destination directory if it doesn't exist
                    File destinationDir = new File(context.getExternalMediaDirs()[0], destinationDirectory);
                    if (!destinationDir.exists()) {
                        destinationDir.mkdirs();
                       // Toast.makeText(context, "Created", Toast.LENGTH_SHORT).show();
                    }else{
                     //   Toast.makeText(context, "existed", Toast.LENGTH_SHORT).show();

                    }

                    // Get the InputStream from the asset file
                    in = assetManager.open(assetFileName);

                    // Create the OutputStream to the external storage destination
                    File outFile = new File(destinationDir, assetFileName);
                    out = new FileOutputStream(outFile);

                    // Copy the file
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }

                    // Close the streams
                    in.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }







    }

}
