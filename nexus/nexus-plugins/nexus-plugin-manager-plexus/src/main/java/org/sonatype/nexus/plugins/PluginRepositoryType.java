package org.sonatype.nexus.plugins;

/**
 * Model describing what repository type did plugin contribute to Nexus core.
 * 
 * @author cstamas
 */
public class PluginRepositoryType
{
    private String componentContract;

    private String componentName;

    private String pathPrefix;
    
    private int repositoryMaxInstanceCount;

    public PluginRepositoryType( String componentContract, String componentName, String pathPrefix, int repositoryMaxInstanceCount )
    {
        this.componentContract = componentContract;
        
        this.componentName = componentName;

        this.pathPrefix = pathPrefix;
        
        this.repositoryMaxInstanceCount = repositoryMaxInstanceCount;
    }

    public String getComponentContract()
    {
        return componentContract;
    }

    public void setComponentContract( String componentContract )
    {
        this.componentContract = componentContract;
    }

    public String getComponentName()
    {
        return componentName;
    }

    public void setComponentName( String componentName )
    {
        this.componentName = componentName;
    }

    public String getPathPrefix()
    {
        return pathPrefix;
    }

    public void setPathPrefix( String pathPrefix )
    {
        this.pathPrefix = pathPrefix;
    }

    public int getRepositoryMaxInstanceCount()
    {
        return repositoryMaxInstanceCount;
    }

    public void setRepositoryMaxInstanceCount( int repositoryMaxInstanceCount )
    {
        this.repositoryMaxInstanceCount = repositoryMaxInstanceCount;
    }
}
