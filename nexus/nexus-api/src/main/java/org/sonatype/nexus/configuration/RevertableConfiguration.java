package org.sonatype.nexus.configuration;

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
     * Commits the changes. Resets the state of config "back to normal" (saved).
     */
    void commitChanges();

    /**
     * Rollbacks the changes. Resets the state of config "back to normal" (saved).
     */
    void rollbackChanges();
}
