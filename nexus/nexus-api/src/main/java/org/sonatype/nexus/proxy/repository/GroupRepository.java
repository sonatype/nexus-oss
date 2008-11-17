package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * A group repository is simply as it's name says, a repository that is backed by a group of other repositories. There
 * is one big constraint, they are READ ONLY. Usually, if you try a write/delete operation against this kind of
 * repository, you are doing something wrong. Deploys/writed and deletes should be done directly against the
 * hosted/proxied repositories, not against these "aggregated" ones.
 * 
 * @author cstamas
 */
public interface GroupRepository
    extends Repository
{
    /**
     * Returns the content class of this repository group.
     * 
     * @return
     * @throws NoSuchRepositoryGroupException
     */
    ContentClass getRepositoryGroupContentClass();

    /**
     * Returns the list of Repositories that are group members in this GroupRepository. The repo order within list is
     * repo rank, so processing is possible by simply iterating over resulting list.
     * 
     * @return a List<Repository>
     */
    List<Repository> getMemberRepositories();
}
