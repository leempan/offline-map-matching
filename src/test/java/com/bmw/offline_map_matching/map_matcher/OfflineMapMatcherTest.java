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

package com.bmw.offline_map_matching.map_matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;

import com.bmw.hmm_lib.MostLikelySequence;
import com.bmw.hmm_lib.TimeStep;
import com.bmw.offline_map_matching.default_types.DefaultTemporalMetrics;
import com.bmw.offline_map_matching.default_types.EuclideanSpatialMetrics;
import com.bmw.offline_map_matching.default_types.GpsMeasurement;
import com.bmw.offline_map_matching.default_types.RoadPosition;
import com.bmw.offline_map_matching.map_matcher.OfflineMapMatcher;
import com.bmw.offline_map_matching.map_matcher.PrecomputedSpatialMetrics;

public class OfflineMapMatcherTest {

    @Test
    /**
     * The test scenario is depicted in ./OfflineMapMatcherTest.png.
     * All road segments can be driven in both directions. The orientation of road segments
     * is needed to determine the fractions of a road positions.
     */
    public void testMapMatching() {
        List<TimeStep<RoadPosition, GpsMeasurement>> timeSteps = new ArrayList<>();

        GpsMeasurement gps1 = new GpsMeasurement(seconds(0), 10, 10);
        RoadPosition rp11 = new RoadPosition(1, 1.0 / 5.0, 20.0, 10.0);
        RoadPosition rp12 = new RoadPosition(2, 1.0 / 5.0, 60.0, 10.0);
        timeSteps.add( new TimeStep<>(gps1, rp11, rp12) );

        GpsMeasurement gps2 = new GpsMeasurement(seconds(1), 30, 20);
        RoadPosition rp21 = new RoadPosition(1, 2.0 / 5.0, 20.0, 20.0);
        RoadPosition rp22 = new RoadPosition(2, 2.0 / 5.0, 60.0, 20.0);
        timeSteps.add( new TimeStep<>(gps2, rp21, rp22) );

        GpsMeasurement gps3 = new GpsMeasurement(seconds(2), 30, 40);
        RoadPosition rp31 = new RoadPosition(1, 5.0 / 6.0, 20.0, 40.0);
        RoadPosition rp32 = new RoadPosition(3, 1.0 / 4.0, 30.0, 50.0);
        RoadPosition rp33 = new RoadPosition(2, 5.0 / 6.0, 60.0, 40.0);
        timeSteps.add( new TimeStep<>(gps3, rp31, rp32, rp33) );

        GpsMeasurement gps4 = new GpsMeasurement(seconds(3), 10, 70);
        RoadPosition rp41 = new RoadPosition(4, 2.0 / 3.0, 20.0, 70.0);
        RoadPosition rp42 = new RoadPosition(5, 2.0 / 3.0, 60.0, 70.0);
        timeSteps.add( new TimeStep<>(gps4, rp41, rp42) );

        final EuclideanSpatialMetrics spatialMetrics = new EuclideanSpatialMetrics();
        spatialMetrics.addRouteLength(rp11, rp21, 10.0);
        spatialMetrics.addRouteLength(rp11, rp22, 110.0);
        spatialMetrics.addRouteLength(rp12, rp21, 110.0);
        spatialMetrics.addRouteLength(rp12, rp22, 10.0);

        spatialMetrics.addRouteLength(rp21, rp31, 20.0);
        spatialMetrics.addRouteLength(rp21, rp32, 40.0);
        spatialMetrics.addRouteLength(rp21, rp33, 80.0);
        spatialMetrics.addRouteLength(rp22, rp31, 80.0);
        spatialMetrics.addRouteLength(rp22, rp32, 60.0);
        spatialMetrics.addRouteLength(rp22, rp33, 20.0);

        spatialMetrics.addRouteLength(rp31, rp41, 30.0);
        spatialMetrics.addRouteLength(rp31, rp42, 70.0);
        spatialMetrics.addRouteLength(rp32, rp41, 30.0);
        spatialMetrics.addRouteLength(rp32, rp42, 50.0);
        spatialMetrics.addRouteLength(rp33, rp41, 70.0);
        spatialMetrics.addRouteLength(rp33, rp42, 30.0);

        List<RoadPosition> roadPositions = OfflineMapMatcher.computeMostLikelySequence(timeSteps,
                new DefaultTemporalMetrics(), spatialMetrics).sequence;
        assertEquals(Arrays.asList(rp11, rp21, rp31, rp41), roadPositions);
    }

    @Test
    public void testHmmBreak() {
        List<TimeStep<RoadPosition, GpsMeasurement>> timeSteps = new ArrayList<>();

        GpsMeasurement gps1 = new GpsMeasurement(seconds(0), 10, 10);
        timeSteps.add( new TimeStep<RoadPosition, GpsMeasurement>(gps1) );

        MostLikelySequence<RoadPosition, GpsMeasurement> mls =
                OfflineMapMatcher.computeMostLikelySequence(timeSteps,
                new DefaultTemporalMetrics(),
                new PrecomputedSpatialMetrics<RoadPosition, GpsMeasurement>());
        assertTrue(mls.isBroken);
    }


    private Date seconds(int seconds) {
        Calendar c = new GregorianCalendar(2014, 1, 1);
        c.add(Calendar.SECOND, seconds);
        return c.getTime();
    }

}
