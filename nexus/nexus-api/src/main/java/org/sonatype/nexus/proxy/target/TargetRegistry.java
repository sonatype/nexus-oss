package org.sonatype.nexus.proxy.target;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component responsible for holding target references and for matching them.
 * 
 * @author cstamas
 */
public interface TargetRegistry
{
    Target getRepositoryTarget( String id );

    void addRepositoryTarget( Target target );

    boolean removeRepositoryTarget( String id );

    TargetSet getTargetsForRepositoryPath( Repository repository, String path );
}
