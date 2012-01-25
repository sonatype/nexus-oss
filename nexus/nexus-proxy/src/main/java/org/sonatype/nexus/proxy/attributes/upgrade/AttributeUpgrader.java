package org.sonatype.nexus.proxy.attributes.upgrade;

public interface AttributeUpgrader
{
    boolean isLegacyAttributesDirectoryPresent();

    boolean isUpgradeNeeded();

    boolean isUpgradeRunning();
    
    boolean isUpgradeFinished();

    int getCurrentUps();

    int getLimiterUps();

    void setLimiterUps( int limit );

    void upgrade();
}
