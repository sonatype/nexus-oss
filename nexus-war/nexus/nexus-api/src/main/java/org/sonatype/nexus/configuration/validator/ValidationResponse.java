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

import java.util.List;

/**
 * A validation response, returned after configuration validation from validator.
 * 
 * @author cstamas
 */
public interface ValidationResponse
{
    boolean isValid();

    void setValid( boolean valid );

    boolean isModified();

    void setModified( boolean modified );

    List<ValidationMessage> getValidationErrors();

    void setValidationErrors( List<ValidationMessage> validationErrors );

    void addValidationError( ValidationMessage message );

    void addValidationError( String message );

    void addValidationError( String message, Throwable t );

    List<ValidationMessage> getValidationWarnings();

    void setValidationWarnings( List<ValidationMessage> validationWarnings );

    void addValidationWarning( ValidationMessage message );

    void addValidationWarning( String message );

    void append( ValidationResponse response );
    
    void setContext( ValidationContext ctx );

    ValidationContext getContext();
}
