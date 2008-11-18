package org.sonatype.nexus.proxy.target;

import java.util.Set;

import org.sonatype.nexus.proxy.registry.ContentClass;
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

    /**
     * This method will match all existing targets against a content class and a path. You will end up with simple set
     * of matched targets. The cardinality of this set is equal to existing targets cardinality.
     * 
     * @param contentClass
     * @param path
     * @return
     */
    Set<Target> getTargetsForContentClassPath( ContentClass contentClass, String path );

    /**
     * This method will match all existing targets against a repository and a path. You will end up with a TargetSet,
     * that contains TargetMatches with references to Target and Repository.
     * 
     * @param repository
     * @param path
     * @return
     */
    TargetSet getTargetsForRepositoryPath( Repository repository, String path );
}
