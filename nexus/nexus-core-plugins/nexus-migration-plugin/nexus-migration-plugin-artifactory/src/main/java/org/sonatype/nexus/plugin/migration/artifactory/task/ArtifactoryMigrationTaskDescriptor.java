/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.task;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "ArtifactoryMigration", description = "Artifactory Migration" )
public class ArtifactoryMigrationTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    
    public static final String ID = "ArtifactoryMigrationTask";

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Artifactory Migration";
    }

    public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors()
    {
        return new ArrayList<ScheduledTaskPropertyDescriptor>();
    }

    @Override
    public boolean isExposed()
    {
        return false;
    }

    
    
}
