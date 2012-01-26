package org.sonatype.nexus.proxy.attributes.upgrade;

/**
 * JMX interface for managing and monitoring Attribute Upgrade.
 * 
 * @author cstamas
 * @since 2.0
 */
public interface AttributeUpgraderMBean
{
    /**
     * Returns true if folder holding legacy attributes is present.
     * 
     * @return {@code true} if legacy attributes directory is present in Nexus workdir.
     */
    boolean isLegacyAttributesDirectoryPresent();

    /**
     * Returns true if {@link #isLegacyAttributesDirectoryPresent()} returns true, and those folders are not marked as
     * "done".
     * 
     * @return {@code true} if {@link #isLegacyAttributesDirectoryPresent()} returns true, and those folders are not
     *         marked as "done".
     */
    boolean isUpgradeNeeded();

    /**
     * Returns true if the UpgraderThread is started, and is alive.
     * 
     * @return {@code true} if the UpgraderThread is started, and is alive, hence, upgrade is underway.
     */
    boolean isUpgradeRunning();

    /**
     * Returns true if {@link #isUpgradeRunning()} returns false, and {@link #isUpgradeNeeded()()} returns {@code false}
     * .
     * 
     * @return {@code true} if {@link #isUpgradeRunning()} returns false, and {@link #isUpgradeNeeded()()} returns
     *         {@code false}
     */
    boolean isUpgradeFinished();

    /**
     * Returns the UPS of currently executing upgrade (if {@link #isUpgradeRunning()} returns true). Otherwise, it
     * returns -1.
     * 
     * @return if upgrade is underway, the current amount of performed upgrade operations per second, -1 otherwise.
     */
    int getCurrentUps();

    /**
     * Returns the maximum UPS of last executing (or executed) upgrade. Without upgrade running, it returns -1.
     * 
     * @return
     */
    int getMaximumUps();

    /**
     * Returns the max UPS of currently executing upgrade (if {@link #isUpgradeRunning()} returns true). Otherwise, it
     * returns the value of parameter (ie. "this would be it if it would running").
     * 
     * @return the max UPS of currently executing upgrade (if {@link #isUpgradeRunning()} returns true). Otherwise, it
     *         returns the value of parameter (ie. "this would be it if it would running").
     */
    int getLimiterUps();

    /**
     * Sets the max UPS of currently executing upgrade (if {@link #isUpgradeRunning()} returns true). Otherwise, it is
     * setting the parameter (ie. "this would be it if it would running").
     */
    void setLimiterUps( int limit );

    /**
     * Starts the attributes upgrade if needed (will perform the needed checks and might not start it if decided as not
     * needed).
     */
    void upgradeAttributes();

}
