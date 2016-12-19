package model;

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
