package org.sonatype.nexus.test.booter;

/**
 * The interface to "boot" (control lifecycle) of the Nexus being under test in the ITs.
 * 
 * @author cstamas
 */
public interface NexusBooter
{
    /**
     * Starts one instance of Nexus bundle. May be invoked only once, or after {@link #stopNexus()} is invoked only,
     * otherwise will throw IllegalStateException.
     * 
     * @param testId
     * @throws Exception
     */
    public void startNexus( final String testId )
        throws Exception;

    /**
     * Stops, and cleans up the started Nexus instance. May be invoked any times, it will NOOP if not needed to do
     * anything. Will try to ditch the used classloader. The {@link #clean()} method will be invoked on every invocation
     * of this method, making it more plausible for JVM to recover/GC all the stuff from memory in case of any glitch.
     * 
     * @throws Exception
     */
    public void stopNexus()
        throws Exception;
}
