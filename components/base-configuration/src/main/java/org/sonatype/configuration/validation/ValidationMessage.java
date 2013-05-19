/**
 * Copyright (c) 2007-2012 Sonatype, Inc. All rights reserved.
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
package org.sonatype.configuration.validation;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ValidationMessage
{
    /**
     * Message key.
     */
    private String key;

    /**
     * Message body.
     */
    private String message;

    /**
     * The short message, used to send back to UI for validation display
     */
    private String shortMessage;

    /**
     * The cause of validation problem, if any.
     */
    private Throwable cause;

    /**
     * Creates a validation message without a cause.
     * 
     * @param key
     * @param message
     */
    public ValidationMessage( String key, String message )
    {
        this( key, message, (Throwable) null );
    }

    /**
     * Creates a validation message without a cause.
     * 
     * @param key
     * @param message
     * @param shortMessage
     */
    public ValidationMessage( String key, String message, String shortMessage )
    {
        this( key, message, shortMessage, null );
    }

    /**
     * Creates a validation message with cause.
     * 
     * @param key
     * @param message
     * @param cause
     */
    public ValidationMessage( String key, String message, Throwable cause )
    {
        this( key, message, message, cause );
    }

    /**
     * Creates a validation message with cause.
     * 
     * @param key
     * @param message
     * @param shortMessage
     * @param cause
     */
    public ValidationMessage( String key, String message, String shortMessage, Throwable cause )
    {
        this.key = key;

        this.message = message;

        this.shortMessage = shortMessage;

        this.cause = cause;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey( String key )
    {
        this.key = key;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public String getShortMessage()
    {
        return shortMessage;
    }

    public void setShortMessage( String shortMessage )
    {
        this.shortMessage = shortMessage;
    }

    public Throwable getCause()
    {
        return cause;
    }

    public void setCause( Throwable cause )
    {
        this.cause = cause;
    }

    public String toString()
    {
        StringWriter sw = new StringWriter();

        sw.append( " o " ).append( getKey() ).append( " - " ).append( getMessage() );

        if ( getCause() != null )
        {
            sw.append( "\n" );

            sw.append( "   Cause:\n" );

            getCause().printStackTrace( new PrintWriter( sw ) );
        }

        return sw.toString();
    }
}
