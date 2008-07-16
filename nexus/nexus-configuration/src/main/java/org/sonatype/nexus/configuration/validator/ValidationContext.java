package org.sonatype.nexus.configuration.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidationContext
{
    private List<String> existingRepositoryIds;

    private List<String> existingRepositoryShadowIds;

    private List<String> existingRepositoryGroupIds;

    private List<String> existingPathMappingIds;

    private List<String> existingRealms;

    public void addExistingRepositoryIds()
    {
        if ( this.existingRepositoryIds == null )
        {
            this.existingRepositoryIds = new ArrayList<String>();
        }
    }

    public void addExistingRepositoryShadowIds()
    {
        if ( this.existingRepositoryShadowIds == null )
        {
            this.existingRepositoryShadowIds = new ArrayList<String>();
        }
    }

    public void addExistingRepositoryGroupIds()
    {
        if ( this.existingRepositoryGroupIds == null )
        {
            this.existingRepositoryGroupIds = new ArrayList<String>();
        }
    }

    public void addExistingPathMappingIds()
    {
        if ( this.existingPathMappingIds == null )
        {
            this.existingPathMappingIds = new ArrayList<String>();
        }
    }

    public void addExistingRealms()
    {
        if ( this.existingRealms == null )
        {
            this.existingRealms = new ArrayList<String>();
        }
    }

    public List<String> getExistingRepositoryIds()
    {
        return existingRepositoryIds;
    }

    public List<String> getExistingRepositoryShadowIds()
    {
        return existingRepositoryShadowIds;
    }

    public List<String> getExistingRepositoryGroupIds()
    {
        return existingRepositoryGroupIds;
    }

    public List<String> getExistingPathMappingIds()
    {
        return existingPathMappingIds;
    }

    public List<String> getExistingRealms()
    {
        return existingRealms;
    }
}
