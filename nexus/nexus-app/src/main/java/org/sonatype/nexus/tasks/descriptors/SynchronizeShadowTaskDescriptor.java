/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "SynchronizeShadow", description = "Synchronize Shadow Repository" )
public class SynchronizeShadowTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "SynchronizeShadowsTask";

    @Requirement( role = ScheduledTaskPropertyDescriptor.class, hint = "Shadow" )
    private ScheduledTaskPropertyDescriptor shadowId;

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Synchronize Shadow Repository";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        List<ScheduledTaskPropertyDescriptor> properties = new ArrayList<ScheduledTaskPropertyDescriptor>();

        properties.add( shadowId );

        return properties;
    }
}
