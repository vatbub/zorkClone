package view;

/*-
 * #%L
 * Zork Clone
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
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


import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A list of {@link ConnectionLine}s that can find a line by its start and and room
 */
@SuppressWarnings("unused")
public class ConnectionLineList extends CopyOnWriteArrayList<ConnectionLine> {
    public ConnectionLineList() {
        super();
    }

    public ConnectionLineList(Collection<? extends ConnectionLine> c) {
        super(c);
    }

    /**
     * Searches for the {@link ConnectionLine} that connects the specified rooms.
     *
     * @param startRoom The start room of the line to look for
     * @param endRoom   The end room of the line to look for
     * @return The the {@link ConnectionLine} that connects the specified rooms or {@code null} if no such line was not found.
     */
    @SuppressWarnings("unused")
    @Nullable
    public ConnectionLine findByStartAndEndRoom(RoomRectangle startRoom, RoomRectangle endRoom) {
        for (ConnectionLine line : this) {
            if (line.getStartRoom() == startRoom && line.getEndRoom() == endRoom) {
                return line;
            }
        }

        // nothing found
        return null;
    }

    /**
     * Searches for the {@link ConnectionLine} that connects the specified rooms.
     * This method actually ignores the direction of the line which means that it
     * will find a line even if the specified startRoom is the lines endRoom and
     * the specified endRoom is the lines startRoom.
     *
     * @param startRoom The start room of the line to look for
     * @param endRoom   The end room of the line to look for
     * @return The the {@link ConnectionLine} that connects the specified rooms or {@code null} if no such line was not found.
     */
    @Nullable
    public ConnectionLine findByStartAndEndRoomIgnoreLineDirection(RoomRectangle startRoom, RoomRectangle endRoom) {
        for (ConnectionLine line : this) {
            if ((line.getStartRoom() == startRoom && line.getEndRoom() == endRoom) || line.getStartRoom() == endRoom && line.getEndRoom() == startRoom) {
                return line;
            }
        }

        // nothing found
        return null;
    }

    /**
     * Checks if the angle of all lines in this list matches their preferred angle exactly.
     *
     * @return {@code true} if the angle of all lines matches their preferred angle exactly, {@code false} if at least one angle does not match
     */
    public boolean allLinesMatchPreferredAngle() {
        return allLinesMatchPreferredAngle(0);
    }

    /**
     * Checks if the angle of all lines in this list matches their preferred angle within the specified tolerance.
     *
     * @param tolerance The angle tolerance that is still tolerated
     * @return {@code true} if the angle of all lines matches their preferred angle within the specified tolerance, {@code false} if at least one angle does not match
     */
    @SuppressWarnings("SameParameterValue")
    public boolean allLinesMatchPreferredAngle(double tolerance) {
        return getLinesThatDoNotMatchPreferredAngle(tolerance).size() == 0;
    }

    public ConnectionLineList getLinesThatDoNotMatchPreferredAngle() {
        return getLinesThatDoNotMatchPreferredAngle(0);
    }

    public ConnectionLineList getLinesThatDoNotMatchPreferredAngle(double tolerance) {
        ConnectionLineList res = new ConnectionLineList();

        for (ConnectionLine line : this) {
            if (Math.abs(line.getAngle() - line.getPreferredAngle()) > tolerance) {
                // out of tolerance
                res.add(line);
            }
        }

        return res;
    }

    /**
     * Calculates a score of how well all lines in this list match their preferred angle. This score is calculated using the following formula:<br>
     * <math xmlns='http://www.w3.org/1998/Math/MathML'>
     * <mrow>
     * <munderover>
     * <mo>&#8721;</mo>
     * <mrow>
     * <mi>k</mi>
     * <mo>=</mo>
     * <mn>0</mn>
     * </mrow>
     * <mn>this.size()-1</mn>
     * </munderover>
     * <semantics>
     * <mrow>
     * <mo>&#10072;</mo>
     * <mrow>
     * <mrow>
     * <mo>this.get(k).getAngle()</mo>
     * </mrow>
     * <mo>-</mo>
     * <mrow>
     * <mo>this.get(k).getPreferredAngle()</mo>
     * </mrow>
     * </mrow>
     * <mo>&#10072;</mo>
     * </mrow>
     * </semantics>
     * </mrow>
     * </math><br>
     * A lower the result, the better all lines match their preferred angle.
     *
     * @return A score of how well all lines in this list match their preferred angle.
     */
    public double getConnectivityScore() {
        double res = 0;

        for (ConnectionLine line : this) {
            res = res + Math.abs(line.getAngle() - line.getPreferredAngle());
        }

        return res;
    }
}
