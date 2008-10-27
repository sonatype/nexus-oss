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

package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

/**
 * @author Juven Xu
 */
@Component( role = ScheduledTaskDescriptor.class, hint = "RemoveRepoFolder", description = "Remove Repository Folder" )
public class RemoveRepoFolderTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{

    public static final String ID = "RemoveRepoFolderTask";

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Remove Repository Folder";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();

        return properties;
    }

    /**
     * This task is invisible to users
     */
    public boolean isExposed()
    {
        return false;
    }
}
