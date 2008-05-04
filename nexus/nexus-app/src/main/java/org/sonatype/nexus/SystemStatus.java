/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
    private String version;

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

}
