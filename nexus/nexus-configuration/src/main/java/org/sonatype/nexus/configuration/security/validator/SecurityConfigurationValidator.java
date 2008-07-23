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
package org.sonatype.nexus.configuration.security.validator;

import org.sonatype.nexus.configuration.security.model.CApplicationPrivilege;
import org.sonatype.nexus.configuration.security.model.CRepoTargetPrivilege;
import org.sonatype.nexus.configuration.security.model.CRole;
import org.sonatype.nexus.configuration.security.model.CUser;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * The validator used to validate current configuration in boot-up sequence.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationValidator extends ConfigurationValidator
{
    String ROLE = SecurityConfigurationValidator.class.getName();
    
    /**
     * Validate a user configuration
     * 
     * @param ctx
     * @param user
     * @return
     */
    ValidationResponse validateUser( SecurityValidationContext ctx, CUser user );
    
    /**
     * Validate a role configuration
     * 
     * @param ctx
     * @param role
     * @return
     */
    ValidationResponse validateRole( SecurityValidationContext ctx, CRole role );
    
    /**
     * Validate a repository target privilege configuration
     * 
     * @param ctx
     * @param privilege
     * @return
     */
    ValidationResponse validateRepoTargetPrivilege( SecurityValidationContext ctx, CRepoTargetPrivilege privilege );
    
    /**
     * Validate an application privilege configuration
     * 
     * @param ctx
     * @param privilege
     * @return
     */
    ValidationResponse validateApplicationPrivilege( SecurityValidationContext ctx, CApplicationPrivilege privilege );    
}
