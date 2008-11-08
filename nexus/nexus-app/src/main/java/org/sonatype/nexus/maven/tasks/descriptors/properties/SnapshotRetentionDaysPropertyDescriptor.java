/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.maven.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractNumberPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskPropertyDescriptor.class, hint = "SnapshotRetentionDays", instantiationStrategy = "per-lookup" )
public class SnapshotRetentionDaysPropertyDescriptor
    extends AbstractNumberPropertyDescriptor
{
    public static final String ID = "removeOlderThanDays";
    
    public SnapshotRetentionDaysPropertyDescriptor()
    {
        setHelpText( "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep." );
        setRequired( false );
    }
 
    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Snapshot retention (days)";
    }
}
