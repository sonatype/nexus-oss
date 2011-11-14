/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
/*
 * $Id$
 */

package org.sonatype.nexus.configuration.model.v1_4_6.upgrade;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting;
import org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage;
import org.sonatype.nexus.configuration.model.v1_4_6.CMirror;
import org.sonatype.nexus.configuration.model.v1_4_6.CNotification;
import org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget;
import org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem;
import org.sonatype.nexus.configuration.model.v1_4_6.CPlugin;
import org.sonatype.nexus.configuration.model.v1_4_6.CProps;
import org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance;
import org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage;
import org.sonatype.nexus.configuration.model.v1_4_6.CRepository;
import org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget;
import org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings;
import org.sonatype.nexus.configuration.model.v1_4_6.CRouting;
import org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig;
import org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask;
import org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration;
import org.sonatype.nexus.configuration.model.v1_4_6.Configuration;

/**
 * Converts between version 1.4.5 and version 1.4.6 of the model.
 * 
 * @version $Revision$ $Date$
 */
public interface VersionUpgrade
{

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param cErrorReporting
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting upgradeCErrorReporting( org.sonatype.nexus.configuration.model.v1_4_5.CErrorReporting cErrorReporting );
    /**
     * 
     * 
     * @param cHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings upgradeCHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CHttpProxySettings cHttpProxySettings );
    /**
     * 
     * 
     * @param cLocalStorage
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage upgradeCLocalStorage( org.sonatype.nexus.configuration.model.v1_4_5.CLocalStorage cLocalStorage );
    /**
     * 
     * 
     * @param cMirror
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CMirror upgradeCMirror( org.sonatype.nexus.configuration.model.v1_4_5.CMirror cMirror );
    /**
     * 
     * 
     * @param cNotification
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CNotification upgradeCNotification( org.sonatype.nexus.configuration.model.v1_4_5.CNotification cNotification );
    /**
     * 
     * 
     * @param cNotificationTarget
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget upgradeCNotificationTarget( org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget cNotificationTarget );
    /**
     * 
     * 
     * @param cPathMappingItem
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem upgradeCPathMappingItem( org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem cPathMappingItem );
    /**
     * 
     * 
     * @param cPlugin
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CPlugin upgradeCPlugin( org.sonatype.nexus.configuration.model.v1_4_5.CPlugin cPlugin );
    /**
     * 
     * 
     * @param cProps
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CProps upgradeCProps( org.sonatype.nexus.configuration.model.v1_4_5.CProps cProps );
    /**
     * 
     * 
     * @param cRemoteAuthentication
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication upgradeCRemoteAuthentication( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteAuthentication cRemoteAuthentication );
    /**
     * 
     * 
     * @param cRemoteConnectionSettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings upgradeCRemoteConnectionSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteConnectionSettings cRemoteConnectionSettings );
    /**
     * 
     * 
     * @param cRemoteHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteHttpProxySettings cRemoteHttpProxySettings );
    /**
     * 
     * 
     * @param cRemoteNexusInstance
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance upgradeCRemoteNexusInstance( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance cRemoteNexusInstance );
    /**
     * 
     * 
     * @param cRemoteStorage
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage upgradeCRemoteStorage( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteStorage cRemoteStorage );
    /**
     * 
     * 
     * @param cRepository
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepository upgradeCRepository( org.sonatype.nexus.configuration.model.v1_4_5.CRepository cRepository );
    /**
     * 
     * 
     * @param cRepositoryGrouping
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping upgradeCRepositoryGrouping( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryGrouping cRepositoryGrouping );
    /**
     * 
     * 
     * @param cRepositoryTarget
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget upgradeCRepositoryTarget( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget cRepositoryTarget );
    /**
     * 
     * 
     * @param cRestApiSettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings upgradeCRestApiSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRestApiSettings cRestApiSettings );
    /**
     * 
     * 
     * @param cRouting
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRouting upgradeCRouting( org.sonatype.nexus.configuration.model.v1_4_5.CRouting cRouting );
    /**
     * 
     * 
     * @param cScheduleConfig
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig upgradeCScheduleConfig( org.sonatype.nexus.configuration.model.v1_4_5.CScheduleConfig cScheduleConfig );
    /**
     * 
     * 
     * @param cScheduledTask
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask upgradeCScheduledTask( org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask cScheduledTask );
    /**
     * 
     * 
     * @param cSmtpConfiguration
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration upgradeCSmtpConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.CSmtpConfiguration cSmtpConfiguration );
    /**
     * 
     * 
     * @param configuration
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.Configuration upgradeConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.Configuration configuration );
}
