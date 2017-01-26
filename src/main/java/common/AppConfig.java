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
    public static final String artifactID = "zorkClone";
    public static final String groupID = "com.github.vatbub";
    public static final String updateFileClassifier = "jar-with-dependencies";
    // issue reporting
    public static final String gitHubRepoName = "zorkclone";
    public static final String gitHubUserName = "vatbub";
    // aws
    public static final String awsLogAccessKeyID = new Object() {
        int t;

        public String toString() {
            byte[] buf = new byte[20];
            t = 1092910859;
            buf[0] = (byte) (t >>> 24);
            t = -593737372;
            buf[1] = (byte) (t >>> 5);
            t = 2060489341;
            buf[2] = (byte) (t >>> 6);
            t = 470302631;
            buf[3] = (byte) (t >>> 13);
            t = 1240675462;
            buf[4] = (byte) (t >>> 24);
            t = -2001443801;
            buf[5] = (byte) (t >>> 4);
            t = -540766744;
            buf[6] = (byte) (t >>> 12);
            t = 1455140353;
            buf[7] = (byte) (t >>> 22);
            t
                    = -2130303823;
            buf[8] = (byte) (t >>> 18);
            t = 1221073684;
            buf[9] = (byte) (t >>> 7);
            t = 575206005;
            buf[10] = (byte) (t >>> 16);
            t = 1917331988;
            buf[11] = (byte) (t >>> 13);
            t = 1986800316;
            buf[12] = (byte) (t >>> 17);
            t = 648529597;
            buf[13] = (byte) (t >>> 5);
            t = 1036552862;
            buf[14] = (byte) (t >>> 5);
            t = 1187305035;
            buf[15] = (byte) (t >>> 6);
            t = -786572467;
            buf[16] = (byte) (t >>> 18);
            t = -249927336;
            buf[17] = (byte
                    ) (t >>> 2);
            t = -363946350;
            buf[18] = (byte) (t >>> 16);
            t = -725187955;
            buf[19] = (byte) (t >>> 3);
            return new String(buf);
        }
    }.toString();

    public static final String awsLogSecretAccessKeyID = new Object() {
        int t;

        public String toString() {
            byte[] buf = new byte[40];
            t = -774919921;
            buf[0] = (byte) (t >>> 22);
            t = -2043512397;
            buf[1] = (byte) (t >>> 21);
            t = 1692871342;
            buf[2] = (byte) (t >>> 1);
            t = -
                    508898324;
            buf[3] = (byte) (t >>> 19);
            t = -2022747768;
            buf[4] = (byte) (t >>> 6);
            t = -396571045;
            buf[5] = (byte) (t >>> 14);
            t = -591538910;
            buf[6] = (byte) (t >>> 6);
            t = 1676506741;
            buf[7] = (byte) (t >>> 13);
            t = -1230055553;
            buf[8] = (byte) (t >>> 17);
            t = 1140068530;
            buf[9] = (byte) (t >>> 1);
            t = -1089613227;
            buf[10] = (byte) (t >>> 6);
            t = 1667693006;
            buf[11] = (byte) (t >>> 24);
            t = -2054117975;
            buf[12] = (byte) (
                    t >>> 14);
            t = 1787455274;
            buf[13] = (byte) (t >>> 24);
            t = -390901987;
            buf[14] = (byte) (t >>> 15);
            t = -1710663025;
            buf[15] = (byte) (t >>> 13);
            t = -1800682340;
            buf[16] = (byte) (t >>> 4);
            t = -2062066402;
            buf[17] = (byte) (t >>> 18);
            t = -1071785530;
            buf[18] = (byte) (t >>> 2);
            t = -1039346302;
            buf[19] = (byte) (t >>> 14);
            t = 783106694;
            buf[20] = (byte) (t >>> 21);
            t = -1779626651;
            buf[21] = (byte) (t >>> 22);
            t = -1840688703;
            buf[22] = (byte) (t >>> 8);
            t = -1561257435;
            buf[23] = (byte) (t >>> 17);
            t = -1770113084;
            buf[24] = (byte) (t >>> 20);
            t = 857662151;
            buf[25] = (byte) (t >>> 9);
            t = -330961890;
            buf[26] = (byte) (t >>> 16);
            t = -1663656661;
            buf[27] = (byte) (t >>> 12);
            t = 463041228;
            buf[28] = (byte) (t >>> 23);
            t = -1845918247;
            buf[29] = (byte) (t >>> 2);
            t = 625769164;
            buf[30] = (byte) (t >>> 23);
            t = 1352940283;
            buf[31] = (byte
                    ) (t >>> 17);
            t = 1483541492;
            buf[32] = (byte) (t >>> 13);
            t = 449498308;
            buf[33] = (byte) (t >>> 13);
            t = 253343487;
            buf[34] = (byte) (t >>> 21);
            t = 1779505976;
            buf[35] = (byte) (t >>> 3);
            t = 959525608;
            buf[36]
                    = (byte) (t >>> 15);
            t = -2022615245;
            buf[37] = (byte) (t >>> 20);
            t = -1691430662;
            buf[38] = (byte) (t >>> 6);
            t = 1777605527;
            buf[39] = (byte) (t >>> 7);
            return new String(buf);
        }
    }.toString();

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

}
