package com.div.pechai;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LabelReader {

    public static String[] readLabelsFromFile(Context context, String filePath) {
        List<String> labelsList = new ArrayList<>();

        try {
            // Open the file using AssetManager
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            // Read each line from the file
            String line;
            while ((line = reader.readLine()) != null) {
                // Add each line to the list of labels
                labelsList.add(line.trim());
            }

            // Close the BufferedReader, InputStreamReader, and InputStream
            reader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert the list of labels to an array
        String[] labels = new String[labelsList.size()];
        labelsList.toArray(labels);

        return labels;
    }
}
