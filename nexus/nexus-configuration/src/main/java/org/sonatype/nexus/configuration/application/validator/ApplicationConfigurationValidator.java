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
package org.sonatype.nexus.configuration.application.validator;

import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.configuration.validator.ConfigurationValidator;
import org.sonatype.nexus.configuration.validator.ValidationResponse;

/**
 * The validator used to validate current configuration in boot-up sequence.
 * 
 * @author cstamas
 */
public interface ApplicationConfigurationValidator
    extends ConfigurationValidator
{
    /**
     * Validates a repository configuration.
     * 
     * @param repository
     * @return
     */
    ValidationResponse validateRepository( ApplicationValidationContext ctx, CRepository repository );

    /**
     * Validates a repository configuration.
     * 
     * @param repository
     * @return
     */
    ValidationResponse validateRepository( ApplicationValidationContext ctx, CRepositoryShadow repository );

    /**
     * Validates remote connection settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteConnectionSettings( ApplicationValidationContext ctx,
        CRemoteConnectionSettings settings );

    /**
     * Validates security settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateSecurity( ApplicationValidationContext ctx, CSecurity settings );

    /**
     * Validates remote proxy settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteHttpProxySettings( ApplicationValidationContext ctx,
        CRemoteHttpProxySettings settings );

    /**
     * Validates remote authentication.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteAuthentication( ApplicationValidationContext ctx, CRemoteAuthentication settings );

    /**
     * Validates rest api settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRestApiSettings( ApplicationValidationContext ctx, CRestApiSettings settings );

    /**
     * Validates Nexus built-in HTTP proxy settings.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateHttpProxySettings( ApplicationValidationContext ctx, CHttpProxySettings settings );

    /**
     * Validates routing.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRouting( ApplicationValidationContext ctx, CRouting settings );

    /**
     * Validates remote nexus instance.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRemoteNexusInstance( ApplicationValidationContext ctx, CRemoteNexusInstance settings );

    /**
     * Validates repository grouping.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRepositoryGrouping( ApplicationValidationContext ctx, CRepositoryGrouping settings );

    /**
     * Validates mapping.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateGroupsSettingPathMappingItem( ApplicationValidationContext ctx,
        CGroupsSettingPathMappingItem settings );

    /**
     * Validates repository group item.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRepositoryGroup( ApplicationValidationContext ctx, CRepositoryGroup settings );

    /**
     * Validates repository target item.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateRepositoryTarget( ApplicationValidationContext ctx, CRepositoryTarget settings );

    /**
     * Validates scheduled task.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateScheduledTask( ApplicationValidationContext ctx, CScheduledTask settings );

    /**
     * Validates schedule.
     * 
     * @param settings
     * @return
     */
    ValidationResponse validateSchedule( ApplicationValidationContext ctx, CScheduleConfig settings );

    /**
     * Validates smtp
     * 
     * @param ctx
     * @param settings
     * @return
     */
    ValidationResponse validateSmtpConfiguration( ApplicationValidationContext ctx, CSmtpConfiguration settings );
}
