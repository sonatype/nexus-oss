/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.tasks.descriptors;

import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "EmptyTrash", description="Empty Trash" )
public class EmptyTrashTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{    
    public static final String ID = "EmptyTrashTask";
    
    public String getId()
    {
        return ID;
    }
    
    public String getName()
    {
        return "Empty Trash";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        return Collections.emptyList();
    }
}
