package org.sonatype.nexus.bundle.launcher;

/**
 * Generic runtime exception for bundle launcher.
 *
 * @author plynch
 */
public class NexusBundleLauncherException extends RuntimeException {

    public NexusBundleLauncherException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public NexusBundleLauncherException(String string) {
        super(string);
    }

}
