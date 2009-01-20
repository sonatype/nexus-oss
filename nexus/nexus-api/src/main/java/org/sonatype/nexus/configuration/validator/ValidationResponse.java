/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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
