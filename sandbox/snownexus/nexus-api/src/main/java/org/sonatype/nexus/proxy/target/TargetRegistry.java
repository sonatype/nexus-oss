package org.sonatype.nexus.proxy.target;

import java.util.Collection;
import java.util.Set;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurable;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component responsible for holding target references and for matching them.
 * 
 * @author cstamas
 */
public interface TargetRegistry
    extends Configurable
{
    /**
     * Gets the existing targets.
     * 
     * @return
     */
    Collection<Target> getRepositoryTargets();

    /**
     * Gets target by id.
     * 
     * @param id
     * @return
     */
    Target getRepositoryTarget( String id );

    /**
     * Adds new target.
     * 
     * @param target
     * @throws ConfigurationException
     */
    boolean addRepositoryTarget( Target target )
        throws ConfigurationException;

    /**
     * Removes target by id.
     * 
     * @param id
     * @return
     */
    boolean removeRepositoryTarget( String id );
    
    /**
     * This method will match all existing targets against a content class.  You will end up with a simple set
     * of matched targets.  The cardinality of this set is equal to existing targets cardinality.
     * 
     * @param contentClass
     * @return
     */
    Set<Target> getTargetsForContentClass( ContentClass contentClass );

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

    /**
     * This method will return true if the repository has any applicable target at all.
     * 
     * @param repository
     * @return
     */
    boolean hasAnyApplicableTarget( Repository repository );
}
