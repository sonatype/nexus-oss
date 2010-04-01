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
