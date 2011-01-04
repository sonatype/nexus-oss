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
package org.sonatype.nexus.configuration;

import org.sonatype.configuration.ConfigurationException;


/**
 * A Configurable component.
 * 
 * @author cstamas
 */
public interface Configurable
{
    /**
     * Returns the current core configuration of the component.May return null if there is not config object set.
     * 
     * @return
     */
    CoreConfiguration getCurrentCoreConfiguration();

    /**
     * Sets the configuration object and calls configure(). A shortcut for setCurrentConfiguration(config) and then
     * configure() calls.
     * 
     * @param config
     * @throws ConfigurationException
     */
    void configure( Object config )
        throws ConfigurationException;

    /**
     * Returns true if there are some unsaved changes.
     * 
     * @return
     */
    boolean isDirty();

    /**
     * Commits the changes. Resets the state of config "back to normal" (saved).
     */
    boolean commitChanges()
        throws ConfigurationException;

    /**
     * Rollbacks the changes. Resets the state of config "back to normal" (saved).
     */
    boolean rollbackChanges();
    
    /**
     * A simple short name.
     */
    String getName();
}
