package org.sonatype.nexus.configuration;

import org.sonatype.configuration.ConfigurationException;

/**
 * Revertable configuration is a configuration that is changeable, but may be be reverted (rollback the changes). The
 * changes are "visible" only after applyChanges() call.
 * 
 * @author cstamas
 */
public interface RevertableConfiguration
{
    /**
     * Returns true if this configuration holds some changes that are not persisted.
     * 
     * @return
     */
    boolean isDirty();

    /**
     * Validatyes the changes, if any.
     */
    void validateChanges()
        throws ConfigurationException;
    
    /**
     * Commits the changes. Resets the state of config "back to normal" (saved). Will call validateChanges() if needed.
     */
    void commitChanges()
        throws ConfigurationException;

    /**
     * Rollbacks the changes. Resets the state of config "back to normal" (saved).
     */
    void rollbackChanges();
}
