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