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
package org.sonatype.nexus.tools.migration;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.configuration.model.Configuration;

public class MigrationResult
{

    /**
     * Migration success status.
     */
    private boolean succesful;

    /**
     * Resulting Nexus Configuration or null.
     */
    private final Configuration configuration;

    /**
     * Errors in order as they appeared.
     */
    private List<Exception> exceptions;

    /**
     * Different back-end messages (warnings?) in order as they appeared.
     */
    private List<String> messages;

    public MigrationResult( Configuration configuration )
    {
        super();
        this.succesful = false;
        this.configuration = configuration;
        this.exceptions = new ArrayList<Exception>();
        this.messages = new ArrayList<String>();
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public List<Exception> getExceptions()
    {
        return exceptions;
    }

    public void setExceptions( List<Exception> errors )
    {
        this.exceptions = errors;
    }

    public List<String> getMessages()
    {
        return messages;
    }

    public void setMessages( List<String> messages )
    {
        this.messages = messages;
    }

    public boolean isSuccesful()
    {
        return succesful;
    }

    public void setSuccesful( boolean succesful )
    {
        this.succesful = succesful;
    }

}
