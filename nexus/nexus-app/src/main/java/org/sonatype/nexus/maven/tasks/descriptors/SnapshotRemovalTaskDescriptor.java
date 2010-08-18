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
package org.sonatype.nexus.maven.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;

@Component( role = ScheduledTaskDescriptor.class, hint = "SnapshotRemoval", description = "Remove Snapshots From Repository" )
public class SnapshotRemovalTaskDescriptor
    extends AbstractScheduledTaskDescriptor
{
    public static final String ID = "SnapshotRemoverTask";

    public static final String REPO_OR_GROUP_FIELD_ID = "repositoryOrGroupId";

    public static final String MIN_TO_KEEP_FIELD_ID = "minSnapshotsToKeep";

    public static final String KEEP_DAYS_FIELD_ID = "removeOlderThanDays";

    public static final String REMOVE_WHEN_RELEASED_FIELD_ID = "removeIfReleaseExists";

    private final RepoOrGroupComboFormField repoField = new RepoOrGroupComboFormField( REPO_OR_GROUP_FIELD_ID,
                                                                                       FormField.MANDATORY );

    private final NumberTextFormField minToKeepField =
        new NumberTextFormField( MIN_TO_KEEP_FIELD_ID, "Minimum snapshot count",
                                 "Minimum number of snapshots to keep for one GAV.", FormField.OPTIONAL );

    private final NumberTextFormField keepDaysField =
        new NumberTextFormField(
                                 KEEP_DAYS_FIELD_ID,
                                 "Snapshot retention (days)",
                                 "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep.",
                                 FormField.OPTIONAL );

    private final CheckboxFormField removeWhenReleasedField =
        new CheckboxFormField(
                               REMOVE_WHEN_RELEASED_FIELD_ID,
                               "Remove if released",
                               "The job will purge all snapshots that have a corresponding released artifact (same version not including the -SNAPSHOT).",
                               FormField.OPTIONAL );

    public String getId()
    {
        return ID;
    }

    public String getName()
    {
        return "Remove Snapshots From Repository";
    }

    public List<FormField> formFields()
    {
        List<FormField> fields = new ArrayList<FormField>();

        fields.add( repoField );
        fields.add( minToKeepField );
        fields.add( keepDaysField );
        fields.add( removeWhenReleasedField );

        return fields;
    }
}
