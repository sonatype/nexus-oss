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

import java.util.Collection;
import java.util.Collections;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * An event fired on configuration change (upon succesful save). This event is meant for component outside of
 * "configuration framework", for any other component interested in configuration change (like feed generators, mail
 * senders, etc).
 * 
 * @author cstamas
 */
public class ConfigurationChangeEvent
    extends ConfigurationEvent
{
    private final Collection<Configurable> changes;

    private final String userId;

    public ConfigurationChangeEvent( ApplicationConfiguration configuration, Collection<Configurable> changes, String userId )
    {
        super( configuration );

        if ( changes == null )
        {
            changes = Collections.emptyList();
        }

        this.changes = Collections.unmodifiableCollection( changes );

        this.userId = userId;
    }

    public Collection<Configurable> getChanges()
    {
        return changes;
    }

    public String getUserId()
    {
        return userId;
    }
}
