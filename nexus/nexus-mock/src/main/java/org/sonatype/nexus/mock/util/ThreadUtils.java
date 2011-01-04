/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock.util;

import java.util.concurrent.TimeUnit;

public class ThreadUtils {
    public static interface WaitCondition {
        public boolean checkCondition(long elapsedTimeInMs);
    }

    public static void sleep(TimeUnit timeUnit, long duration) {
        try {
            timeUnit.sleep(duration);
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static boolean waitFor(WaitCondition condition) {
        boolean result = false;
        if (condition != null) {
            long startTime = System.currentTimeMillis();

            while (!(result = condition.checkCondition(System.currentTimeMillis() - startTime))) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return result;
    }

    public static boolean waitFor(WaitCondition condition, TimeUnit timeUnit, long timeoutDuration) {
        long timeout = timeUnit.toMillis(timeoutDuration);

        boolean result = false;
        if (condition != null) {
            long startTime = System.currentTimeMillis();
            long curTime = startTime;

            while (!(result = condition.checkCondition(curTime - startTime)) && (curTime - startTime < timeout)) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                curTime = System.currentTimeMillis();
            }
        }

        return result;
    }

    public static boolean waitFor(WaitCondition condition, TimeUnit timeUnitTimeout, long timeoutDuration, TimeUnit timeUnitSleep, long sleepDuration) {
        long timeout = timeUnitTimeout.toMillis(timeoutDuration);
        long sleepBetween = timeUnitSleep.toMillis(sleepDuration);

        boolean result = false;
        if (condition != null) {
            long startTime = System.currentTimeMillis();
            long curTime = startTime;

            while (!(result = condition.checkCondition(curTime - startTime)) && (curTime - startTime < timeout)) {
                try {
                    Thread.sleep(sleepBetween);
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                curTime = System.currentTimeMillis();
            }
        }

        return result;
    }
}