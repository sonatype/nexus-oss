package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractGroupRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MEMBER_REPOSITORIES = "memberRepositories";

    public AbstractGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public List<String> getMemberRepositoryIds()
    {
        return getCollection( getConfiguration(), MEMBER_REPOSITORIES );
    }

    public void setMemberRepositoryIds( List<String> vals )
    {
        setCollection( getConfiguration(), MEMBER_REPOSITORIES, vals );
    }

    public void removeMemberRepositoryId( String repositoryId )
    {
        removeFromCollection( getConfiguration(), MEMBER_REPOSITORIES, repositoryId );
    }
}
