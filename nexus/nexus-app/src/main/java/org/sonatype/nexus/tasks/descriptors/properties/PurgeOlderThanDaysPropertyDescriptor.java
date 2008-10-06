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
package org.sonatype.nexus.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ScheduledTaskPropertyDescriptor.class, hint = "PurgeOlderThanDays", instantiationStrategy = "per-lookup" )
public class PurgeOlderThanDaysPropertyDescriptor
    extends AbstractNumberPropertyDescriptor
{
    public PurgeOlderThanDaysPropertyDescriptor()
    {
        setHelpText( "Set the number of days, to purge items from Timeline that are older then the given number of days." );
        setRequired( true );
    }
 
    public String getId()
    {
        return "purgeOlderThan";
    }

    public String getName()
    {
        return "Purge older items than (days)";
    }
}
