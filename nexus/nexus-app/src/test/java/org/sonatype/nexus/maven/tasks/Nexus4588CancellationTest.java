/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.maven.tasks;

import java.lang.reflect.Method;

import org.junit.Test;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.scheduling.CancellableProgressListenerWrapper;
import org.sonatype.scheduling.ProgressListener;
import org.sonatype.scheduling.TaskInterruptedException;
import org.sonatype.scheduling.TaskUtil;

/**
 * See Nexus-4588, testing SnapshotRemover cancellation. Note: Snapshot remover task is just a "thin wrapper" doing
 * not much around the SnapshotRemover that is a singleto component in system. Hence, to make this test more
 * simpler, we test the SnapshotRemover component, without the "tasks" fuss.
 *
 * @author: cstamas
 */
public class Nexus4588CancellationTest
    extends AbstractMavenRepoContentTests
{

    @Test( expected = TaskInterruptedException.class )
    public void testNexus4588()
        throws Exception
    {
        fillInRepo();

        setUpProgressListener();

        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), 1, 10, true );

        TaskUtil.getCurrentProgressListener().cancel();

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
    }

    @Test( expected = TaskInterruptedException.class )
    public void testNexus4588After1stWalk()
        throws Exception
    {
        fillInRepo();

        setUpProgressListener();

        SnapshotRemovalRequest snapshotRemovalRequest =
            new SnapshotRemovalRequest( snapshots.getId(), 1, 10, true );

        // activate the molester
        // the molester will cancel the task once it receives cache expired event, which is sent
        // after 1st pass. This is an implementation details, so this test is actually fragile
        // against SnapshotRemover component implementation changes
        ( (Nexus4588CancellationEventInspector) lookup( EventInspector.class, "nexus4588" ) ).setActive( true );

        SnapshotRemovalResult result = defaultNexus.removeSnapshots( snapshotRemovalRequest );
    }

    public static void setUpProgressListener()
        throws Exception
    {
        Method setCurrentMethod = TaskUtil.class.getDeclaredMethod( "setCurrent", ProgressListener.class );

        setCurrentMethod.setAccessible( true );

        setCurrentMethod.invoke( null, new CancellableProgressListenerWrapper( null ) );
    }
}
