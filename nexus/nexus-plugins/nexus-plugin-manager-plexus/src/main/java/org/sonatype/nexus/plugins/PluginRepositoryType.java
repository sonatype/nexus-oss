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

    public PluginRepositoryType( String componentContract, String componentName, String pathPrefix )
    {
        this.componentContract = componentContract;

        this.pathPrefix = pathPrefix;
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
}
