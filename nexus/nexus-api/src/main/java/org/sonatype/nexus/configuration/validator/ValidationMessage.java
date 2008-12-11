/**
 * Sonatype Nexus™ [Open Source Version].
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
package org.sonatype.nexus.configuration.validator;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Validation message is one message (error/warning) about the validity of the validated configuration.
 * 
 * @author cstamas
 */
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
        this( key, message, ( Throwable ) null );
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
        this ( key, message, shortMessage, null );
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
