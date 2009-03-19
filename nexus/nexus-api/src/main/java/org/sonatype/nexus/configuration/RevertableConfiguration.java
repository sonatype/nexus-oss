package org.sonatype.nexus.configuration;

/**
 * Revertable configuration is a configuration that is changeable, but may be be reverted (rollback the changes).
 * 
 * @author cstamas
 */
public interface RevertableConfiguration
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
    void applyChanges();

    /**
     * Resets the state of config "back to normal" (saved).
     */
    void rollbackChanges();

    /**
     * Returns the actual configuration.
     */
    Object getConfiguration( boolean forWrite );
}
