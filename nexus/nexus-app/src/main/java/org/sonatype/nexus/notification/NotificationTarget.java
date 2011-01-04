/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.notification;

import java.util.HashSet;
import java.util.Set;

public class NotificationTarget
{
    private String targetId;

    private final Set<String> targetRoles;

    private final Set<String> targetUsers;

    private final Set<String> externalTargets;

    public NotificationTarget()
    {
        this.targetRoles = new HashSet<String>();

        this.targetUsers = new HashSet<String>();

        this.externalTargets = new HashSet<String>();
    }

    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId( String targetId )
    {
        this.targetId = targetId;
    }

    public Set<String> getTargetRoles()
    {
        return targetRoles;
    }

    public Set<String> getTargetUsers()
    {
        return targetUsers;
    }

    public Set<String> getExternalTargets()
    {
        return externalTargets;
    }
}
