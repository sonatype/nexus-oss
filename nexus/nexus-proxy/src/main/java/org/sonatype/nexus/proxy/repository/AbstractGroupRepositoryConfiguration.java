package org.sonatype.nexus.proxy.repository;

import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractGroupRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MEMBER_REPOSITORIES = "memberRepositories";

    public AbstractGroupRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public boolean membersChanged()
    {
        String oldConf = getCollection( getConfiguration( false ), MEMBER_REPOSITORIES ).toString();

        String newConf = getCollection( getConfiguration( true ), MEMBER_REPOSITORIES ).toString();

        return !StringUtils.equals( oldConf, newConf );
    }

    public List<String> getMemberRepositoryIds()
    {
        return getCollection( getConfiguration( false ), MEMBER_REPOSITORIES );
    }

    public void setMemberRepositoryIds( List<String> ids )
    {
        setCollection( getConfiguration( true ), MEMBER_REPOSITORIES, ids );
    }

    public void clearMemberRepositoryIds()
    {
        List<String> empty = Collections.emptyList();

        setCollection( getConfiguration( true ), MEMBER_REPOSITORIES, empty );
    }

    public void addMemberRepositoryId( String repositoryId )
    {
        addToCollection( getConfiguration( true ), MEMBER_REPOSITORIES, repositoryId, true );
    }

    public void removeMemberRepositoryId( String repositoryId )
    {
        removeFromCollection( getConfiguration( true ), MEMBER_REPOSITORIES, repositoryId );
    }
}
