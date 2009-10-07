package org.sonatype.nexus.plugins.lvo.api.dto;

public class LvoConfigRequest
{
    private LvoConfigDTO data;

    public LvoConfigDTO getData()
    {
        return data;
    }

    public void setData( LvoConfigDTO data )
    {
        this.data = data;
    }
}
