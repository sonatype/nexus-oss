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
package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;

@Component( role = ScheduledTaskDescriptor.class, hint = "RebuildAttributes", description = "Rebuild Repository Attributes" )
public class RebuildAttributesTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "RebuildAttributesTask";

    public static final String REPO_OR_GROUP_FIELD_ID = "repositoryOrGroupId";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField( REPO_OR_GROUP_FIELD_ID,
                                                                                       FormField.MANDATORY );

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Rebuild Repository Attributes";
    }

    public List<FormField> formFields()
    {
        List<FormField> fields = new ArrayList<FormField>();

        fields.add( repoField );

        return fields;
    }

    @Override
    public boolean isExposed()
    {
        return false;
    }
}
