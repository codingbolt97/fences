package com.philips.bootcamp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamUtils {
    
    public static String getStreamContents(InputStream is) {
        if (is != null) {
            StringBuilder fileContents = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                
                String line = null;
                while ((line  = reader.readLine()) != null) {
                    fileContents.append(line + "\n");
                }
            } catch (IOException ioe) {
                return null;
            }
            int length = fileContents.length();
            fileContents.delete(length-1, length);
            return fileContents.toString();
        }

        return null;
    }
}