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
package org.sonatype.nexus.configuration.validator;

import java.io.StringWriter;

import org.sonatype.nexus.configuration.ConfigurationException;

/**
 * Thrown when some semantical error is detected in validated configuration.
 * 
 * @author cstamas
 */
public class InvalidConfigurationException
    extends ConfigurationException
{
    private static final long serialVersionUID = 7888058531204836852L;

    /**
     * The validation response.
     */
    private ValidationResponse validationResponse;

    public InvalidConfigurationException()
    {
        this( "Configuration is invalid!" );
    }

    public InvalidConfigurationException( String msg )
    {
        super( msg );
    }

    public InvalidConfigurationException( String msg, Throwable t )
    {
        super( msg, t );
    }

    public InvalidConfigurationException( ValidationResponse validationResponse )
    {
        this();

        this.validationResponse = validationResponse;
    }

    public ValidationResponse getValidationResponse()
    {
        return validationResponse;
    }

    public String getMessage()
    {
        StringWriter sw = new StringWriter();

        sw.append( super.getMessage() );

        if ( getValidationResponse() != null )
        {
            if ( getValidationResponse().getValidationErrors().size() > 0 )
            {
                sw.append( "\nValidation errors follows:\n" );

                for ( ValidationMessage error : getValidationResponse().getValidationErrors() )
                {
                    sw.append( error.toString() );
                }
                sw.append( "\n" );
            }

            if ( getValidationResponse().getValidationWarnings().size() > 0 )
            {
                sw.append( "\nValidation warnings follows:\n" );

                for ( ValidationMessage warning : getValidationResponse().getValidationWarnings() )
                {
                    sw.append( warning.toString() );
                }
                sw.append( "\n" );
            }
        }

        return sw.toString();
    }

}
