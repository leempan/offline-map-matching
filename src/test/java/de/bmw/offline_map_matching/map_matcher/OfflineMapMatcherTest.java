/**
 * Copyright (C) 2015, BMW Car IT GmbH
 * Author: Stefan Holder (stefan.holder@bmw.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bmw.offline_map_matching.map_matcher;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;

import de.bmw.hmm.TimeStep;

public class OfflineMapMatcherTest {

    @Test
    /**
     * The test scenario is depicted in ./OfflineMapMatcherTest.png. GPS measurements are indicated
     * with diagonal crosses whereas matching candidates are marked on the road segments.
     * All road segments can be driven in both directions.
     */
    public void testMapMatching() {
        List<TimeStep<RoadPosition, GpsMeasurement>> timeSteps = new ArrayList<>();

        /*
         *  Edge id and fraction of road position denotes their indexes to easily identify them
         *  when debugging.
         */
        GpsMeasurement gps1 = new GpsMeasurement(seconds(0), 10, 10);
        RoadPosition rp11 = new RoadPosition(1, 1, gps1);
        RoadPosition rp12 = new RoadPosition(1, 2, gps1);
        timeSteps.add( new TimeStep<>(gps1, rp11, rp12) );

        GpsMeasurement gps2 = new GpsMeasurement(seconds(1), 30, 20);
        RoadPosition rp21 = new RoadPosition(2, 1, gps2);
        RoadPosition rp22 = new RoadPosition(2, 2, gps2);
        timeSteps.add( new TimeStep<>(gps2, rp21, rp22) );

        GpsMeasurement gps3 = new GpsMeasurement(seconds(2), 30, 40);
        RoadPosition rp31 = new RoadPosition(3, 1, gps3);
        RoadPosition rp32 = new RoadPosition(3, 2, gps3);
        RoadPosition rp33 = new RoadPosition(3, 3, gps3);
        timeSteps.add( new TimeStep<>(gps3, rp31, rp32, rp33) );

        GpsMeasurement gps4 = new GpsMeasurement(seconds(3), 10, 70);
        RoadPosition rp41 = new RoadPosition(4, 1, gps4);
        RoadPosition rp42 = new RoadPosition(4, 2, gps4);
        timeSteps.add( new TimeStep<>(gps4, rp41, rp42) );

        final PrecomputedSpatialMetrics metrics = new PrecomputedSpatialMetrics();
        metrics.addGpsDistance(rp11, 10.0);
        metrics.addGpsDistance(rp12, 50.0);
        metrics.addGpsDistance(rp21, 10.0);
        metrics.addGpsDistance(rp22, 30.0);
        metrics.addGpsDistance(rp31, 10.0);
        metrics.addGpsDistance(rp32, 10.0);
        metrics.addGpsDistance(rp33, 30.0);
        metrics.addGpsDistance(rp41, 10.0);
        metrics.addGpsDistance(rp42, 50.0);

        metrics.addLinearDistance(gps1, gps2, distance(20, 10));
        metrics.addLinearDistance(gps2, gps3, 20);
        metrics.addLinearDistance(gps3, gps4, distance(20, 30));

        metrics.addRouteLength(rp11, rp21, 10.0);
        metrics.addRouteLength(rp11, rp22, 110.0);
        metrics.addRouteLength(rp12, rp21, 110.0);
        metrics.addRouteLength(rp12, rp22, 10.0);

        metrics.addRouteLength(rp21, rp31, 20.0);
        metrics.addRouteLength(rp21, rp32, 40.0);
        metrics.addRouteLength(rp21, rp33, 80.0);
        metrics.addRouteLength(rp22, rp31, 80.0);
        metrics.addRouteLength(rp22, rp32, 60.0);
        metrics.addRouteLength(rp22, rp33, 20.0);

        metrics.addRouteLength(rp31, rp41, 30.0);
        metrics.addRouteLength(rp31, rp42, 70.0);
        metrics.addRouteLength(rp32, rp41, 30.0);
        metrics.addRouteLength(rp32, rp42, 50.0);
        metrics.addRouteLength(rp33, rp41, 70.0);
        metrics.addRouteLength(rp33, rp42, 30.0);

        List<RoadPosition> roadPositions = OfflineMapMatcher.mapMatchObservations(timeSteps,
                metrics);
        assertEquals(Arrays.asList(rp11, rp21, rp31, rp41), roadPositions);
    }

    @Test
    public void testHmmBreak() {
        List<TimeStep<RoadPosition, GpsMeasurement>> timeSteps = new ArrayList<>();

        GpsMeasurement gps1 = new GpsMeasurement(seconds(0), 10, 10);
        timeSteps.add( new TimeStep<RoadPosition, GpsMeasurement>(gps1) );

        List<RoadPosition> roadPositions = OfflineMapMatcher.mapMatchObservations(timeSteps,
                new PrecomputedSpatialMetrics());
        assertEquals(null, roadPositions);

    }


    private Date seconds(int seconds) {
        Calendar c = new GregorianCalendar(2014, 1, 1);
        c.add(Calendar.SECOND, seconds);
        return c.getTime();
    }

    private double distance(double deltaX, double deltaY) {
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

}