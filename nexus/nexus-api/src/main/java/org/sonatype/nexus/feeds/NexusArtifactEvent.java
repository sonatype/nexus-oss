/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.feeds;

import java.util.Date;
import java.util.Map;

import org.sonatype.nexus.artifact.NexusItemInfo;

/**
 * A class thet encapsulates a Nexus artifact event: caching, deploying, deleting or retrieving of it.
 * 
 * @author cstamas
 */
public class NexusArtifactEvent
{
    public static final String ACTION_CACHED = "cached";

    public static final String ACTION_DEPLOYED = "deployed";

    public static final String ACTION_DELETED = "deleted";

    public static final String ACTION_RETRIEVED = "retrieved";

    public static final String ACTION_BROKEN = "broken";

    public static final String ACTION_BROKEN_WRONG_REMOTE_CHECKSUM = "brokenWRC";

    /**
     * The artifactInfo about artifact.
     */
    private NexusItemInfo nexusItemInfo;

    /**
     * The date of the event.
     */
    private Date eventDate;

    /**
     * The context of the event (request).
     */
    private Map<String, Object> eventContext;

    /**
     * The action.
     */
    private String action;

    /**
     * The message.
     */
    private String message;

    public NexusItemInfo getNexusItemInfo()
    {
        return nexusItemInfo;
    }

    public void setNexusItemInfo( NexusItemInfo nexusItemInfo )
    {
        this.nexusItemInfo = nexusItemInfo;
    }

    public Date getEventDate()
    {
        return eventDate;
    }

    public void setEventDate( Date eventDate )
    {
        this.eventDate = eventDate;
    }

    public Map<String, Object> getEventContext()
    {
        return eventContext;
    }

    public void setEventContext( Map<String, Object> eventContext )
    {
        this.eventContext = eventContext;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction( String action )
    {
        this.action = action;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

}
