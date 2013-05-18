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

import java.util.ArrayList;
import java.util.List;

/**
 * A validation response, returned after configuration validation from validator.
 * 
 * @author cstamas
 */
public class ValidationResponse
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
     * @param validationResponse
     */
    public void append( ValidationResponse validationResponse )
    {
        for ( ValidationMessage msg : validationResponse.getValidationErrors() )
        {
            if ( getValidationError( msg.getKey() ) != null )
            {
                msg.setKey( msg.getKey() + "(" + key++ + ")" );
            }

            addValidationError( msg );
        }

        for ( ValidationMessage msg : validationResponse.getValidationWarnings() )
        {
            if ( getValidationWarning( msg.getKey() ) != null )
            {
                msg.setKey( msg.getKey() + "(" + key++ + ")" );
            }

            addValidationWarning( msg );
        }

        setValid( isValid() && validationResponse.isValid() );

        setModified( isModified() || validationResponse.isModified() );
    }

    public void setContext( ValidationContext context )
    {
        this.context = context;
    }

    public ValidationContext getContext()
    {
        return context;
    }
}
