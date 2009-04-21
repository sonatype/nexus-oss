/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.jsecurity.realms.tools;

import java.io.StringWriter;

import org.sonatype.jsecurity.realms.validator.ValidationMessage;
import org.sonatype.jsecurity.realms.validator.ValidationResponse;

public class InvalidConfigurationException
    extends Exception
{
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
