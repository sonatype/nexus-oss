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
package org.sonatype.nexus;

import java.util.Date;

import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * Nexus system state object. It gives small amount of important infos about Nexus Application.
 * 
 * @author cstamas
 */
public class SystemStatus
{
    /**
     * The Nexus Application version.
     */
    private String version = "unknown";

    /**
     * The Nexus Application edition.
     */
    private String edition = "OSS";

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
     * The validation response of the configuration.
     */
    private ValidationResponse configurationValidationResponse;

    /**
     * Other error cause that blocked startup.
     */
    private Throwable errorCause;

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getEdition()
    {
        return edition;
    }

    public void setEdition( String edition )
    {
        this.edition = edition;
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

    public ValidationResponse getConfigurationValidationResponse()
    {
        return configurationValidationResponse;
    }

    public void setConfigurationValidationResponse( ValidationResponse configurationValidationResponse )
    {
        this.configurationValidationResponse = configurationValidationResponse;
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

}
