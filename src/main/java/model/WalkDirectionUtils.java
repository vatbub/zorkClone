package model;

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


import javafx.scene.shape.Line;

/**
 * Utilities for {@link WalkDirection}{@code s}
 */
public class WalkDirectionUtils {
    public static WalkDirection invert(WalkDirection in) {
        if (in == WalkDirection.NORTH) {
            return WalkDirection.SOUTH;
        } else if (in == WalkDirection.NORTH_EAST) {
            return WalkDirection.SOUTH_WEST;
        } else if (in == WalkDirection.EAST) {
            return WalkDirection.WEST;
        } else if (in == WalkDirection.SOUTH_EAST) {
            return WalkDirection.NORTH_WEST;
        } else if (in == WalkDirection.SOUTH) {
            return WalkDirection.NORTH;
        } else if (in == WalkDirection.SOUTH_WEST) {
            return WalkDirection.NORTH_EAST;
        } else if (in == WalkDirection.WEST) {
            return WalkDirection.EAST;
        } else if (in == WalkDirection.NORTH_WEST) {
            return WalkDirection.SOUTH_EAST;
        } else {
            // in is WalkDirection.NONE, but for the compiler, it needs to be else and not else if
            return WalkDirection.NONE;
        }
    }

    public static WalkDirection getFromLineAngle(double lineAngle){
        if (lineAngle <= (Math.PI / 8.0) && lineAngle >= (-Math.PI / 8.0)) {
            // north
            return WalkDirection.NORTH;
        } else if (lineAngle < (Math.PI * 3.0 / 8.0) && lineAngle > (Math.PI / 8.0)) {
            // ne
            return WalkDirection.NORTH_EAST;
        } else if (lineAngle <= (Math.PI * 5.0 / 8.0) && lineAngle >= (Math.PI * 3.0 / 8.0)) {
            // e
            return WalkDirection.EAST;
        } else if (lineAngle < (Math.PI * 7.0 / 8.0) && lineAngle > (Math.PI * 5.0 / 8.0)) {
            // se
            return WalkDirection.SOUTH_EAST;
        } else if ((lineAngle <= (-Math.PI * 7.0 / 8.0) && lineAngle >= (-Math.PI)) || (lineAngle <= (Math.PI) && lineAngle >= (Math.PI * 7.0 / 8.0))) {
            // s
            return WalkDirection.SOUTH;
        } else if (lineAngle < (-Math.PI * 5.0 / 8.0) && lineAngle > (-Math.PI * 7.0 / 8.0)) {
            // sw
            return WalkDirection.SOUTH_WEST;
        } else if (lineAngle <= (-Math.PI * 3.0 / 8.0) && lineAngle >= (-Math.PI * 5.0 / 8.0)) {
            // w
            return WalkDirection.WEST;
        } else  {
            // ... if (lineAngle < (-Math.PI * 1.0 / 8.0) && lineAngle > (-Math.PI * 3.0 / 8.0))
            // but the compiler requires else...
            // nw
            return WalkDirection.NORTH_WEST;
        }
    }

    public static WalkDirection getFromLine(Line line){
        double lineAngle = Math.atan2(line.getEndX() - line.getStartX(), line.getStartY() - line.getEndY());
        return getFromLineAngle(lineAngle);
    }
}
