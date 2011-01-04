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
package org.sonatype.nexus.templates;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;

/**
 * A template for creation of various objects.
 * 
 * @author cstamas
 */
public interface Template
{
    /**
     * Returns the originating template provider for this template.
     * 
     * @return
     */
    TemplateProvider getTemplateProvider();

    /**
     * The ID of this template.
     * 
     * @return
     */
    String getId();

    /**
     * The human description of this template.
     * 
     * @return
     */
    String getDescription();

    /**
     * Returns true if the supplied object does "fit" the target that this template creates (a la
     * class.isAssignableFrom(target)). The actual meaning of "fit" is left to given template and it's implementation,
     * how to "narrow" the selection.
     * 
     * @param target
     * @return
     */
    boolean targetFits( Object target );

    /**
     * Instantianates this template, creates resulting object (needs cast).
     * 
     * @return
     * @throws ConfigurationException
     * @throws IOException
     */
    Object create()
        throws ConfigurationException, IOException;
}
