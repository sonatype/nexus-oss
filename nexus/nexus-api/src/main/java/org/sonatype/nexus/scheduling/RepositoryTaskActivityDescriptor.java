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
package org.sonatype.nexus.scheduling;

import java.util.Set;

/**
 * An activity descriptor for tasks that are running against one or set of Repositories.
 * 
 * @author cstamas
 */
public interface RepositoryTaskActivityDescriptor
    extends TaskActivityDescriptor
{
    /**
     * Regarding repository content: create = creates new files/content in repository, update = modifies the content of
     * existing content, delete = removes content from repository.
     * 
     * @author cstamas
     */
    public enum ModificationOperator
    {
        create, update, delete
    };

    /**
     * Regarding repository attributes (they are 1:1 to content always): extend = creates new values in attributes (ie.
     * adds custom attributes), refresh = updates/freshens current attributes, lessen = removes existing attributes.
     * 
     * @author cstamas
     */
    public enum AttributesModificationOperator
    {
        extend, refresh, lessen
    };

    /**
     * Returns true if it will scan/walk repository.
     * 
     * @return
     */
    boolean isScanningRepository();

    /**
     * Will scan/walk the repository. Naturally, it implies READ operation happening against repository. If returned
     * null, it will not scan.
     */
    String getRepositoryScanningStartingPath();

    /**
     * Will apply these <b>modify</b> operations to the content of the repository.
     * 
     * @return
     */
    Set<ModificationOperator> getContentModificationOperators();

    /**
     * Will apply these attribute <b>modify</b> operations to the attributes of the repository.
     * 
     * @return
     */
    Set<AttributesModificationOperator> getAttributesModificationOperators();
}
