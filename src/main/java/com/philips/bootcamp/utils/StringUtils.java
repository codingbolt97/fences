package com.philips.bootcamp.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class StringUtils {
    private StringUtils() {
    }

    public static String getProjectNameFromHttpLink(String link) {
        if (link == null)
            return null;

        String projectName = null;
        try {
            new URL(link);
            int indexOfLastForwardSlash = link.lastIndexOf("/");
            int indexOfLastPeriod = link.lastIndexOf(".");
            if (indexOfLastForwardSlash != -1 && indexOfLastPeriod != -1) {
                projectName = link.substring(indexOfLastForwardSlash + 1, indexOfLastPeriod);
            }
        } catch (MalformedURLException e) {
            projectName = null;
        }

        return projectName;
        
    }
}