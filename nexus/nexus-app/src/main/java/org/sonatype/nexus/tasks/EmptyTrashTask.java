/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.tasks.descriptors.EmptyTrashTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Empty trash.
 */
@Component( role = SchedulerTask.class, hint = EmptyTrashTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class EmptyTrashTask
    extends AbstractNexusTask<Object>
{
    /**
     * System event action: empty trash
     */
    public static final String ACTION = "EMPTY_TRASH";

    public static final int DEFAULT_OLDER_THAN_DAYS = -1;

    /**
     * The Wastebasket component.
     */
    @Requirement( role = Wastebasket.class )
    private Wastebasket wastebasket;

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( getEmptyOlderCacheItemsThan() == DEFAULT_OLDER_THAN_DAYS )
        {
            wastebasket.purgeAll();
        }
        else
        {
            wastebasket.purgeAll( getEmptyOlderCacheItemsThan() * A_DAY );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Emptying Trash.";
    }

    public int getEmptyOlderCacheItemsThan()
    {
        String days = getParameters().get( EmptyTrashTaskDescriptor.OLDER_THAN_FIELD_ID );

        if ( StringUtils.isEmpty( days ) )
        {
            return DEFAULT_OLDER_THAN_DAYS;
        }

        return Integer.parseInt( days );
    }

    public void setEmptyOlderCacheItemsThan( int emptyOlderCacheItemsThan )
    {
        getParameters().put( EmptyTrashTaskDescriptor.OLDER_THAN_FIELD_ID, Integer.toString( emptyOlderCacheItemsThan ) );
    }
}
