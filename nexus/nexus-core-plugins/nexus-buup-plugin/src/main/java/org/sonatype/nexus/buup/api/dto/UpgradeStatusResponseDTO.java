package org.sonatype.nexus.buup.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "upgradeStatusResponse" )
public class UpgradeStatusResponseDTO
{
    private UpgradeStatusDTO data;

    public UpgradeStatusDTO getData()
    {
        return data;
    }

    public void setData( UpgradeStatusDTO data )
    {
        this.data = data;
    }
}
