package common;

import logging.FOKLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * Created by Frederik on 28/11/2016.
 */
public class AppConfig {
    private static final FOKLogger log = new FOKLogger(AppConfig.class.getName());

    // Project setup
    public static URL getUpdateRepoBaseURL() {
        URL res = null;
        try {
            res = new URL("http://dl.bintray.com/vatbub/fokprojectsReleases");
        } catch (MalformedURLException e) {
            log.getLogger().log(Level.SEVERE, "An error occurred", e);
        }

        return res;
    }

    public static final String artifactID = "zorkClone";
    public static final String groupID = "com.github.vatbub";
    public static final String updateFileClassifier = "jar-with-dependencies";
}
