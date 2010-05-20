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
package org.sonatype.nexus;

import java.util.Date;

/**
 * Nexus system state object. It gives small amount of important infos about Nexus Application.
 * 
 * @author cstamas
 * @author damian
 */
public class SystemStatus
{
    /**
     * The Application Name
     */
    private String appName = "Sonatype Nexus Maven Repository Manager";

    /**
     * The Formatted Application Name, used whenever possible
     */
    private String formattedAppName = "Sonatype Nexus&trade;";

    /**
     * The Nexus Application version.
     */
    private String version = "unknown";

    /**
     * The Nexus Java API version (not the REST API!).
     */
    private String apiVersion = "unknown";

    /**
     * The Nexus Application edition for display in UI.
     */
    private String editionLong = "Open Source";

    /**
     * The Nexus Application edition for user agent
     */
    private String editionShort = "OSS";

    /**
     * The Nexus Application state.
     */
    private SystemState state;

    /**
     * The Nexus operation mode.
     */
    private OperationMode operationMode;

    /**
     * The time this instance of Nexus was started.
     */
    private Date initializedAt;

    /**
     * The time this instance of Nexus was started.
     */
    private Date startedAt;

    /**
     * The timestamp of last config change.
     */
    private Date lastConfigChange;

    /**
     * Is this 1st start of Nexus?
     */
    private boolean firstStart;

    /**
     * Was it an instance upgrade?
     */
    private boolean instanceUpgraded;

    /**
     * If instanceUpgraded, was there also a configuration upgrade?
     */
    private boolean configurationUpgraded;

    /**
     * Other error cause that blocked startup.
     */
    private Throwable errorCause;

    public String getAppName()
    {
        return appName;
    }

    public void setAppName( String appName )
    {
        this.appName = appName;
    }

    public String getFormattedAppName()
    {
        return formattedAppName;
    }

    public void setFormattedAppName( String formattedAppName )
    {
        this.formattedAppName = formattedAppName;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getApiVersion()
    {
        return apiVersion;
    }

    public void setApiVersion( String version )
    {
        this.apiVersion = version;
    }

    public String getEditionLong()
    {
        return editionLong;
    }

    public void setEditionLong( String editionUI )
    {
        this.editionLong = editionUI;
    }

    public String getEditionShort()
    {
        return editionShort;
    }

    public void setEditionShort( String editionUserAgent )
    {
        this.editionShort = editionUserAgent;
    }

    public SystemState getState()
    {
        return state;
    }

    public void setState( SystemState status )
    {
        this.state = status;
    }

    public OperationMode getOperationMode()
    {
        return operationMode;
    }

    public void setOperationMode( OperationMode operationMode )
    {
        this.operationMode = operationMode;
    }

    public Date getInitializedAt()
    {
        return initializedAt;
    }

    public void setInitializedAt( Date initializedAt )
    {
        this.initializedAt = initializedAt;
    }

    public Date getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt( Date startedAt )
    {
        this.startedAt = startedAt;
    }

    public Date getLastConfigChange()
    {
        return lastConfigChange;
    }

    public void setLastConfigChange( Date lastConfigChange )
    {
        this.lastConfigChange = lastConfigChange;
    }

    public Throwable getErrorCause()
    {
        return errorCause;
    }

    public void setErrorCause( Throwable errorCause )
    {
        this.errorCause = errorCause;
    }

    public boolean isFirstStart()
    {
        return firstStart;
    }

    public void setFirstStart( boolean firstStart )
    {
        this.firstStart = firstStart;
    }

    public boolean isInstanceUpgraded()
    {
        return instanceUpgraded;
    }

    public void setInstanceUpgraded( boolean instanceUpgraded )
    {
        this.instanceUpgraded = instanceUpgraded;
    }

    public boolean isConfigurationUpgraded()
    {
        return configurationUpgraded;
    }

    public void setConfigurationUpgraded( boolean configurationUpgraded )
    {
        this.configurationUpgraded = configurationUpgraded;
    }
    
    public boolean isNexusStarted()
    {
        return SystemState.STARTED.equals( getState() );
    }
}
