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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractValidationResponse
    implements ValidationResponse
{
    /**
     * A simple counter to enumerate messages.
     */
    private int key = 1;

    /**
     * A flag to mark is the config valid (usable) or not.
     */
    private boolean valid = true;

    /**
     * A flag to mark is the config modified during validation or not.
     */
    private boolean modified = false;

    /**
     * List of validation errors.
     */
    private List<ValidationMessage> validationErrors;

    /**
     * List of valiation warnings.
     */
    private List<ValidationMessage> validationWarnings;

    /**
     * Context for validators to communicate.
     */
    private ValidationContext context;

    public boolean isValid()
    {
        return valid;
    }

    public void setValid( boolean valid )
    {
        this.valid = valid;
    }

    public boolean isModified()
    {
        return modified;
    }

    public void setModified( boolean modified )
    {
        this.modified = modified;
    }

    public List<ValidationMessage> getValidationErrors()
    {
        if ( validationErrors == null )
        {
            validationErrors = new ArrayList<ValidationMessage>();
        }
        return validationErrors;
    }
    
    public ValidationMessage getValidationError( String key )
    {
        if ( validationErrors != null )
        {
            for ( ValidationMessage vm : validationErrors )
            {
                if ( vm.getKey().equals( key ) )
                {
                    return vm;
                }
            }
        }
        
        return null;
    }

    public void setValidationErrors( List<ValidationMessage> validationErrors )
    {
        this.validationErrors = validationErrors;

        valid = validationErrors == null || validationErrors.size() == 0;
    }

    public void addValidationError( ValidationMessage message )
    {
        getValidationErrors().add( message );

        this.valid = false;
    }

    public void addValidationError( String message )
    {
        ValidationMessage e = new ValidationMessage( String.valueOf( key++ ), message );

        addValidationError( e );
    }

    public void addValidationError( String message, Throwable t )
    {
        ValidationMessage e = new ValidationMessage( String.valueOf( key++ ), message, t );

        addValidationError( e );
    }
    
    public ValidationMessage getValidationWarning( String key )
    {
        if ( validationWarnings != null )
        {
            for ( ValidationMessage vm : validationWarnings )
            {
                if ( vm.getKey().equals( key ) )
                {
                    return vm;
                }
            }
        }
        
        return null;
    }

    public List<ValidationMessage> getValidationWarnings()
    {
        if ( validationWarnings == null )
        {
            validationWarnings = new ArrayList<ValidationMessage>();
        }
        return validationWarnings;
    }

    public void setValidationWarnings( List<ValidationMessage> validationWarnings )
    {
        this.validationWarnings = validationWarnings;
    }

    public void addValidationWarning( ValidationMessage message )
    {
        getValidationWarnings().add( message );
    }

    public void addValidationWarning( String message )
    {
        ValidationMessage e = new ValidationMessage( String.valueOf( key++ ), message );

        addValidationWarning( e );
    }

    /**
     * A method to append a validation response to this validation response. The errors list and warnings list are
     * simply appended, and the isValid is logically AND-ed and isModified is logically OR-ed.
     * 
     * @param response
     */
    public void append( ValidationResponse response )
    {
        for ( ValidationMessage msg : response.getValidationErrors() )
        {
            if ( getValidationError( msg.getKey() ) != null )
            {
                msg.setKey( msg.getKey() + "(" + key++ + ")" );
            }

            addValidationError( msg );
        }

        for ( ValidationMessage msg : response.getValidationWarnings() )
        {
            if ( getValidationWarning( msg.getKey() ) != null )
            {
                msg.setKey( msg.getKey() + "(" + key++ + ")" );
            }

            addValidationWarning( msg );
        }

        setValid( isValid() && response.isValid() );

        setModified( isModified() || response.isModified() );
    }

    public void setContext( ValidationContext ctx )
    {
        this.context = ctx;
    }

    public ValidationContext getContext()
    {
        if ( context == null )
        {
            context = doGetContext();
        }

        return context;
    }
    
    protected abstract ValidationContext doGetContext();
}
