package common;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import logging.FOKLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * The config of this application
 */
public class AppConfig {
    // Project setup
    public static URL getUpdateRepoBaseURL() {
        URL res = null;
        try {
            res = new URL("http://dl.bintray.com/vatbub/fokprojectsReleases");
        } catch (MalformedURLException e) {
            FOKLogger.log(AppConfig.class.getName(), Level.SEVERE, "An error occurred", e);
        }

        return res;
    }

    public static final String artifactID = "zorkClone";
    public static final String groupID = "com.github.vatbub";
    public static final String updateFileClassifier = "jar-with-dependencies";
}
