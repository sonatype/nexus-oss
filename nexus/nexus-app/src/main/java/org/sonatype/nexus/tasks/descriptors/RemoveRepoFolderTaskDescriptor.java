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
