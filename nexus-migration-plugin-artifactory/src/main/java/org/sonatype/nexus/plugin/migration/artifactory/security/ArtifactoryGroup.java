package org.sonatype.nexus.plugin.migration.artifactory.security;

public class ArtifactoryGroup
{
    private String name;

    private String description;

    public ArtifactoryGroup( String name )
    {
        this.name = name;
    }

    public ArtifactoryGroup( String name, String description )
    {
        this.name = name;

        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description = description;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( !( obj instanceof ArtifactoryGroup ) )
        {
            return false;
        }

        ArtifactoryGroup group = (ArtifactoryGroup) obj;

        return name.equals( group.getName() ) && description.equals( group.getDescription() );
    }

}
