package org.sonatype.nexus.plugins.p2.repository;

import java.util.Map;

public class P2RepositoryGeneratorConfiguration
{

    public static final String REPO_OR_GROUP_ID = "repoOrGroup";

    private final String repositoryId;

    public P2RepositoryGeneratorConfiguration( final Map<String, String> properties )
    {
        repositoryId = repository( properties );
    }

    public String repositoryId()
    {
        return repositoryId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( repositoryId == null ) ? 0 : repositoryId.hashCode() );
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final P2RepositoryGeneratorConfiguration other = (P2RepositoryGeneratorConfiguration) obj;
        if ( repositoryId == null )
        {
            if ( other.repositoryId != null )
            {
                return false;
            }
        }
        else if ( !repositoryId.equals( other.repositoryId ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append( "P2RepositoryGeneratorConfiguration [" );
        if ( repositoryId != null )
        {
            builder.append( "repositoryId=" );
            builder.append( repositoryId );
        }
        builder.append( "]" );
        return builder.toString();
    }

    private static String repository( final Map<String, String> properties )
    {
        String repositoryId = properties.get( REPO_OR_GROUP_ID );
        repositoryId = repositoryId.replaceFirst( "repo_", "" );
        repositoryId = repositoryId.replaceFirst( "group_", "" );
        return repositoryId;
    }

}
