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
