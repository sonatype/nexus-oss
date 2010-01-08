package org.sonatype.nexus.buup.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "upgradeStatus" )
public class UpgradeStatusDTO
{
    private String upgradeStatus;

    public UpgradeStatusDTO()
    {
    }

    public UpgradeStatusDTO( String upgradeStatus )
    {
        this.upgradeStatus = upgradeStatus;
    }

    public String getUpgradeStatus()
    {
        return upgradeStatus;
    }

    public void setUpgradeStatus( String upgradeStatus )
    {
        this.upgradeStatus = upgradeStatus;
    }
}
