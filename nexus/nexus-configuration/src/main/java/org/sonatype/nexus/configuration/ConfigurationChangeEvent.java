/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.configuration;

import java.util.Collection;
import java.util.Collections;

import org.jsecurity.subject.Subject;
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

    private final Subject subject;

    public ConfigurationChangeEvent( ApplicationConfiguration configuration, Collection<Configurable> changes,
        Subject subject )
    {
        super( configuration );

        if ( changes == null )
        {
            changes = Collections.emptyList();
        }

        this.changes = Collections.unmodifiableCollection( changes );

        this.subject = subject;
    }

    public Collection<Configurable> getChanges()
    {
        return changes;
    }

    public Subject getSubject()
    {
        return subject;
    }
}
