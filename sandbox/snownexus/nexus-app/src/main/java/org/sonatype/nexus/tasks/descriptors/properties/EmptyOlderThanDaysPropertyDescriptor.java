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
package org.sonatype.nexus.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;

/**
 * 
 * @author Juven Xu
 *
 */
@Component( role = ScheduledTaskPropertyDescriptor.class, hint = "EmptyOlderThanDays", instantiationStrategy = "per-lookup" )
public class EmptyOlderThanDaysPropertyDescriptor
    extends AbstractNumberPropertyDescriptor
{

    public static final String ID = "EmptyTrashItemsOlderThan";

    public EmptyOlderThanDaysPropertyDescriptor()
    {
        setHelpText( "Set the number of days, to purge all items that were trashed before the given number of days." );
        setRequired( false );
    }

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Purge items older than (days)";
    }

}
