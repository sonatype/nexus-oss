package org.sonatype.nexus.buup.api.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( value = "upgradeFormRequest" )
public class UpgradeFormRequestDTO
{
    private UpgradeFormDTO data;

    public UpgradeFormDTO getData()
    {
        return data;
    }

    public void setData( UpgradeFormDTO data )
    {
        this.data = data;
    }
}
