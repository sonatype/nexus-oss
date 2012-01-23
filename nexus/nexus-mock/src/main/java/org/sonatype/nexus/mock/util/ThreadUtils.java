/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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