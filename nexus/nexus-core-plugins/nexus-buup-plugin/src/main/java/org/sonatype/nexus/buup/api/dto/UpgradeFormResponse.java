package org.sonatype.nexus.buup.api.dto;

import java.util.ArrayList;
import java.util.List;

public class UpgradeFormResponse
{
    private String upgradeProcessStatus;

    private List<String> errors;

    public String getUpgradeProcessStatus()
    {
        return upgradeProcessStatus;
    }

    public void setUpgradeProcessStatus( String upgradeProcessStatus )
    {
        this.upgradeProcessStatus = upgradeProcessStatus;
    }

    public List<String> getErrors()
    {
        if ( errors == null )
        {
            errors = new ArrayList<String>();
        }
        
        return errors;
    }

    public void setErrors( List<String> errors )
    {
        this.errors = errors;
    }
}
