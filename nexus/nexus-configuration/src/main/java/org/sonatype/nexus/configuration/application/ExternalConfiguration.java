package org.sonatype.nexus.configuration.application;

public interface ExternalConfiguration
{
    /**
     * Returns true if external configuration holds some changes that are not persisted.
     * 
     * @return
     */
    boolean isDirty();

    /**
     * Resets the state of config "back to normal" (saved).
     */
    void unmarkDirty();
}
