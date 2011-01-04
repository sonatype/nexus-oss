/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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
     * Gets the existing targets. It returns an umodifiable collection. To modify targets, use methods below
     * (add/remove).
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
     * This method will match all existing targets against a content class. You will end up with a simple set of matched
     * targets. The cardinality of this set is equal to existing targets cardinality.
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
