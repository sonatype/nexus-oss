package org.sonatype.nexus.plugins.lvo.api.dto;

public class LvoConfigDTO
{
    private boolean enabled;
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }
}
