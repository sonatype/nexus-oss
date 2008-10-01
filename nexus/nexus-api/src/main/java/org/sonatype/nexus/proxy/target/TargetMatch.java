package org.sonatype.nexus.proxy.target;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A class that holds the target and repository for a query.
 * 
 * @author cstamas
 */
public class TargetMatch
{
    private final Target target;

    private final Repository repository;

    public TargetMatch( Target target, Repository repository )
    {
        super();

        this.target = target;

        this.repository = repository;
    }

    public Target getTarget()
    {
        return target;
    }

    public Repository getRepository()
    {
        return repository;
    }
}
