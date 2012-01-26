package org.sonatype.nexus.proxy.attributes.upgrade;

import javax.management.StandardMBean;

public class DefaultAttributeUpgraderMBean
    extends StandardMBean
    implements AttributeUpgraderMBean
{
    private final AttributeUpgrader attributeUpgrader;

    protected DefaultAttributeUpgraderMBean( final AttributeUpgrader attributeUpgrader )
    {
        super( AttributeUpgraderMBean.class, false );
        this.attributeUpgrader = attributeUpgrader;
    }

    @Override
    public boolean isLegacyAttributesDirectoryPresent()
    {
        return attributeUpgrader.isLegacyAttributesDirectoryPresent();
    }

    @Override
    public boolean isUpgradeNeeded()
    {
        return attributeUpgrader.isUpgradeNeeded();
    }

    @Override
    public boolean isUpgradeRunning()
    {
        return attributeUpgrader.isUpgradeRunning();
    }

    @Override
    public boolean isUpgradeFinished()
    {
        return attributeUpgrader.isUpgradeFinished();
    }

    @Override
    public int getCurrentUps()
    {
        return attributeUpgrader.getCurrentUps();
    }

    @Override
    public int getMaximumUps()
    {
        return attributeUpgrader.getMaximumUps();
    }

    @Override
    public int getLimiterUps()
    {
        return attributeUpgrader.getLimiterUps();
    }

    @Override
    public void setLimiterUps( int limit )
    {
        attributeUpgrader.setLimiterUps( limit );
    }

    @Override
    public void upgradeAttributes()
    {
        attributeUpgrader.upgradeAttributes( true );
    }
}
