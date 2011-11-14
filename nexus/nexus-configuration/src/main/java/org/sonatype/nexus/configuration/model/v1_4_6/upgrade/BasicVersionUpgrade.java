/*
 * $Id$
 */

package org.sonatype.nexus.configuration.model.v1_4_6.upgrade;

/**
 * Converts between version 1.4.5 and version 1.4.6 of the model.
 * 
 * @version $Revision$ $Date$
 */
public class BasicVersionUpgrade
    implements org.sonatype.nexus.configuration.model.v1_4_6.upgrade.VersionUpgrade
{

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method upgradeCErrorReporting.
     * 
     * @param cErrorReporting
     * @return CErrorReporting
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting upgradeCErrorReporting( org.sonatype.nexus.configuration.model.v1_4_5.CErrorReporting cErrorReporting )
    {
        return upgradeCErrorReporting( cErrorReporting, new org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting upgradeCErrorReporting( org.sonatype.nexus.configuration.model.v1_4_5.CErrorReporting )

    /**
     * Method upgradeCErrorReporting.
     * 
     * @param cErrorReporting
     * @param value
     * @return CErrorReporting
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting upgradeCErrorReporting( org.sonatype.nexus.configuration.model.v1_4_5.CErrorReporting cErrorReporting, org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting value )
    {
        if ( cErrorReporting == null )
        {
            return null;
        }
        // Convert field enabled
        value.setEnabled( cErrorReporting.isEnabled() );
        // Convert field jiraUrl
        value.setJiraUrl( cErrorReporting.getJiraUrl() );
        // Convert field jiraProject
        value.setJiraProject( cErrorReporting.getJiraProject() );
        // Convert field jiraUsername
        value.setJiraUsername( cErrorReporting.getJiraUsername() );
        // Convert field jiraPassword
        value.setJiraPassword( cErrorReporting.getJiraPassword() );
        // Convert field useGlobalProxy
        value.setUseGlobalProxy( cErrorReporting.isUseGlobalProxy() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting upgradeCErrorReporting( org.sonatype.nexus.configuration.model.v1_4_5.CErrorReporting, org.sonatype.nexus.configuration.model.v1_4_6.CErrorReporting )

    /**
     * Method upgradeCHttpProxySettings.
     * 
     * @param cHttpProxySettings
     * @return CHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings upgradeCHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CHttpProxySettings cHttpProxySettings )
    {
        return upgradeCHttpProxySettings( cHttpProxySettings, new org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings upgradeCHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CHttpProxySettings )

    /**
     * Method upgradeCHttpProxySettings.
     * 
     * @param cHttpProxySettings
     * @param value
     * @return CHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings upgradeCHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CHttpProxySettings cHttpProxySettings, org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings value )
    {
        if ( cHttpProxySettings == null )
        {
            return null;
        }
        // Convert field enabled
        value.setEnabled( cHttpProxySettings.isEnabled() );
        // Convert field port
        value.setPort( cHttpProxySettings.getPort() );
        // Convert field proxyPolicy
        value.setProxyPolicy( cHttpProxySettings.getProxyPolicy() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings upgradeCHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CHttpProxySettings, org.sonatype.nexus.configuration.model.v1_4_6.CHttpProxySettings )

    /**
     * Method upgradeCLocalStorage.
     * 
     * @param cLocalStorage
     * @return CLocalStorage
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage upgradeCLocalStorage( org.sonatype.nexus.configuration.model.v1_4_5.CLocalStorage cLocalStorage )
    {
        return upgradeCLocalStorage( cLocalStorage, new org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage upgradeCLocalStorage( org.sonatype.nexus.configuration.model.v1_4_5.CLocalStorage )

    /**
     * Method upgradeCLocalStorage.
     * 
     * @param cLocalStorage
     * @param value
     * @return CLocalStorage
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage upgradeCLocalStorage( org.sonatype.nexus.configuration.model.v1_4_5.CLocalStorage cLocalStorage, org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage value )
    {
        if ( cLocalStorage == null )
        {
            return null;
        }
        // Convert field provider
        value.setProvider( cLocalStorage.getProvider() );
        // Convert field url
        value.setUrl( cLocalStorage.getUrl() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage upgradeCLocalStorage( org.sonatype.nexus.configuration.model.v1_4_5.CLocalStorage, org.sonatype.nexus.configuration.model.v1_4_6.CLocalStorage )

    /**
     * Method upgradeCMirror.
     * 
     * @param cMirror
     * @return CMirror
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CMirror upgradeCMirror( org.sonatype.nexus.configuration.model.v1_4_5.CMirror cMirror )
    {
        return upgradeCMirror( cMirror, new org.sonatype.nexus.configuration.model.v1_4_6.CMirror() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CMirror upgradeCMirror( org.sonatype.nexus.configuration.model.v1_4_5.CMirror )

    /**
     * Method upgradeCMirror.
     * 
     * @param cMirror
     * @param value
     * @return CMirror
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CMirror upgradeCMirror( org.sonatype.nexus.configuration.model.v1_4_5.CMirror cMirror, org.sonatype.nexus.configuration.model.v1_4_6.CMirror value )
    {
        if ( cMirror == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cMirror.getId() );
        // Convert field url
        value.setUrl( cMirror.getUrl() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CMirror upgradeCMirror( org.sonatype.nexus.configuration.model.v1_4_5.CMirror, org.sonatype.nexus.configuration.model.v1_4_6.CMirror )

    /**
     * Method upgradeCNotification.
     * 
     * @param cNotification
     * @return CNotification
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CNotification upgradeCNotification( org.sonatype.nexus.configuration.model.v1_4_5.CNotification cNotification )
    {
        return upgradeCNotification( cNotification, new org.sonatype.nexus.configuration.model.v1_4_6.CNotification() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CNotification upgradeCNotification( org.sonatype.nexus.configuration.model.v1_4_5.CNotification )

    /**
     * Method upgradeCNotification.
     * 
     * @param cNotification
     * @param value
     * @return CNotification
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CNotification upgradeCNotification( org.sonatype.nexus.configuration.model.v1_4_5.CNotification cNotification, org.sonatype.nexus.configuration.model.v1_4_6.CNotification value )
    {
        if ( cNotification == null )
        {
            return null;
        }
        // Convert field enabled
        value.setEnabled( cNotification.isEnabled() );
        {
            java.util.List list = new java.util.ArrayList/*<CNotificationTarget>*/();
            for ( java.util.Iterator i = cNotification.getNotificationTargets().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget v = (org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget) i.next();
                list.add( upgradeCNotificationTarget( v ) );
            }
            value.setNotificationTargets( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CNotification upgradeCNotification( org.sonatype.nexus.configuration.model.v1_4_5.CNotification, org.sonatype.nexus.configuration.model.v1_4_6.CNotification )

    /**
     * Method upgradeCNotificationTarget.
     * 
     * @param cNotificationTarget
     * @return CNotificationTarget
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget upgradeCNotificationTarget( org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget cNotificationTarget )
    {
        return upgradeCNotificationTarget( cNotificationTarget, new org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget upgradeCNotificationTarget( org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget )

    /**
     * Method upgradeCNotificationTarget.
     * 
     * @param cNotificationTarget
     * @param value
     * @return CNotificationTarget
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget upgradeCNotificationTarget( org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget cNotificationTarget, org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget value )
    {
        if ( cNotificationTarget == null )
        {
            return null;
        }
        // Convert field targetId
        value.setTargetId( cNotificationTarget.getTargetId() );
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cNotificationTarget.getTargetRoles().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setTargetRoles( list );
        }
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cNotificationTarget.getTargetUsers().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setTargetUsers( list );
        }
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cNotificationTarget.getTargetExternals().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setTargetExternals( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget upgradeCNotificationTarget( org.sonatype.nexus.configuration.model.v1_4_5.CNotificationTarget, org.sonatype.nexus.configuration.model.v1_4_6.CNotificationTarget )

    /**
     * Method upgradeCPathMappingItem.
     * 
     * @param cPathMappingItem
     * @return CPathMappingItem
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem upgradeCPathMappingItem( org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem cPathMappingItem )
    {
        return upgradeCPathMappingItem( cPathMappingItem, new org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem upgradeCPathMappingItem( org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem )

    /**
     * Method upgradeCPathMappingItem.
     * 
     * @param cPathMappingItem
     * @param value
     * @return CPathMappingItem
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem upgradeCPathMappingItem( org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem cPathMappingItem, org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem value )
    {
        if ( cPathMappingItem == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cPathMappingItem.getId() );
        // Convert field groupId
        value.setGroupId( cPathMappingItem.getGroupId() );
        // Convert field routeType
        value.setRouteType( cPathMappingItem.getRouteType() );
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cPathMappingItem.getRoutePatterns().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setRoutePatterns( list );
        }
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cPathMappingItem.getRepositories().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setRepositories( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem upgradeCPathMappingItem( org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem, org.sonatype.nexus.configuration.model.v1_4_6.CPathMappingItem )

    /**
     * Method upgradeCPlugin.
     * 
     * @param cPlugin
     * @return CPlugin
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CPlugin upgradeCPlugin( org.sonatype.nexus.configuration.model.v1_4_5.CPlugin cPlugin )
    {
        return upgradeCPlugin( cPlugin, new org.sonatype.nexus.configuration.model.v1_4_6.CPlugin() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CPlugin upgradeCPlugin( org.sonatype.nexus.configuration.model.v1_4_5.CPlugin )

    /**
     * Method upgradeCPlugin.
     * 
     * @param cPlugin
     * @param value
     * @return CPlugin
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CPlugin upgradeCPlugin( org.sonatype.nexus.configuration.model.v1_4_5.CPlugin cPlugin, org.sonatype.nexus.configuration.model.v1_4_6.CPlugin value )
    {
        if ( cPlugin == null )
        {
            return null;
        }
        // Convert field type
        value.setType( cPlugin.getType() );
        // Convert field id
        value.setId( cPlugin.getId() );
        // Convert field name
        value.setName( cPlugin.getName() );
        // Convert field status
        value.setStatus( cPlugin.getStatus() );
        // Convert field externalConfiguration
        value.setExternalConfiguration( cPlugin.getExternalConfiguration() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CPlugin upgradeCPlugin( org.sonatype.nexus.configuration.model.v1_4_5.CPlugin, org.sonatype.nexus.configuration.model.v1_4_6.CPlugin )

    /**
     * Method upgradeCProps.
     * 
     * @param cProps
     * @return CProps
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CProps upgradeCProps( org.sonatype.nexus.configuration.model.v1_4_5.CProps cProps )
    {
        return upgradeCProps( cProps, new org.sonatype.nexus.configuration.model.v1_4_6.CProps() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CProps upgradeCProps( org.sonatype.nexus.configuration.model.v1_4_5.CProps )

    /**
     * Method upgradeCProps.
     * 
     * @param cProps
     * @param value
     * @return CProps
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CProps upgradeCProps( org.sonatype.nexus.configuration.model.v1_4_5.CProps cProps, org.sonatype.nexus.configuration.model.v1_4_6.CProps value )
    {
        if ( cProps == null )
        {
            return null;
        }
        // Convert field key
        value.setKey( cProps.getKey() );
        // Convert field value
        value.setValue( cProps.getValue() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CProps upgradeCProps( org.sonatype.nexus.configuration.model.v1_4_5.CProps, org.sonatype.nexus.configuration.model.v1_4_6.CProps )

    /**
     * Method upgradeCRemoteAuthentication.
     * 
     * @param cRemoteAuthentication
     * @return CRemoteAuthentication
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication upgradeCRemoteAuthentication( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteAuthentication cRemoteAuthentication )
    {
        return upgradeCRemoteAuthentication( cRemoteAuthentication, new org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication upgradeCRemoteAuthentication( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteAuthentication )

    /**
     * Method upgradeCRemoteAuthentication.
     * 
     * @param cRemoteAuthentication
     * @param value
     * @return CRemoteAuthentication
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication upgradeCRemoteAuthentication( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteAuthentication cRemoteAuthentication, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication value )
    {
        if ( cRemoteAuthentication == null )
        {
            return null;
        }
        // Convert field username
        value.setUsername( cRemoteAuthentication.getUsername() );
        // Convert field password
        value.setPassword( cRemoteAuthentication.getPassword() );
        // Convert field ntlmHost
        value.setNtlmHost( cRemoteAuthentication.getNtlmHost() );
        // Convert field ntlmDomain
        value.setNtlmDomain( cRemoteAuthentication.getNtlmDomain() );
        // Convert field trustStore
        value.setTrustStore( cRemoteAuthentication.getTrustStore() );
        // Convert field trustStorePassword
        value.setTrustStorePassword( cRemoteAuthentication.getTrustStorePassword() );
        // Convert field keyStore
        value.setKeyStore( cRemoteAuthentication.getKeyStore() );
        // Convert field keyStorePassword
        value.setKeyStorePassword( cRemoteAuthentication.getKeyStorePassword() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication upgradeCRemoteAuthentication( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteAuthentication, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteAuthentication )

    /**
     * Method upgradeCRemoteConnectionSettings.
     * 
     * @param cRemoteConnectionSettings
     * @return CRemoteConnectionSettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings upgradeCRemoteConnectionSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteConnectionSettings cRemoteConnectionSettings )
    {
        return upgradeCRemoteConnectionSettings( cRemoteConnectionSettings, new org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings upgradeCRemoteConnectionSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteConnectionSettings )

    /**
     * Method upgradeCRemoteConnectionSettings.
     * 
     * @param cRemoteConnectionSettings
     * @param value
     * @return CRemoteConnectionSettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings upgradeCRemoteConnectionSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteConnectionSettings cRemoteConnectionSettings, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings value )
    {
        if ( cRemoteConnectionSettings == null )
        {
            return null;
        }
        // Convert field connectionTimeout
        value.setConnectionTimeout( cRemoteConnectionSettings.getConnectionTimeout() );
        // Convert field retrievalRetryCount
        value.setRetrievalRetryCount( cRemoteConnectionSettings.getRetrievalRetryCount() );
        // Convert field queryString
        value.setQueryString( cRemoteConnectionSettings.getQueryString() );
        // Convert field userAgentCustomizationString
        value.setUserAgentCustomizationString( cRemoteConnectionSettings.getUserAgentCustomizationString() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings upgradeCRemoteConnectionSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteConnectionSettings, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteConnectionSettings )

    /**
     * Method upgradeCRemoteHttpProxySettings.
     * 
     * @param cRemoteHttpProxySettings
     * @return CRemoteHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteHttpProxySettings cRemoteHttpProxySettings )
    {
        return upgradeCRemoteHttpProxySettings( cRemoteHttpProxySettings, new org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteHttpProxySettings )

    /**
     * Method upgradeCRemoteHttpProxySettings.
     * 
     * @param cRemoteHttpProxySettings
     * @param value
     * @return CRemoteHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteHttpProxySettings cRemoteHttpProxySettings, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings value )
    {
        if ( cRemoteHttpProxySettings == null )
        {
            return null;
        }
        // Convert field blockInheritance
        value.setBlockInheritance( cRemoteHttpProxySettings.isBlockInheritance() );
        // Convert field proxyHostname
        value.setProxyHostname( cRemoteHttpProxySettings.getProxyHostname() );
        // Convert field proxyPort
        value.setProxyPort( cRemoteHttpProxySettings.getProxyPort() );
        value.setAuthentication( upgradeCRemoteAuthentication( cRemoteHttpProxySettings.getAuthentication() ) );
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cRemoteHttpProxySettings.getNonProxyHosts().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setNonProxyHosts( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteHttpProxySettings, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteHttpProxySettings )

    /**
     * Method upgradeCRemoteNexusInstance.
     * 
     * @param cRemoteNexusInstance
     * @return CRemoteNexusInstance
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance upgradeCRemoteNexusInstance( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance cRemoteNexusInstance )
    {
        return upgradeCRemoteNexusInstance( cRemoteNexusInstance, new org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance upgradeCRemoteNexusInstance( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance )

    /**
     * Method upgradeCRemoteNexusInstance.
     * 
     * @param cRemoteNexusInstance
     * @param value
     * @return CRemoteNexusInstance
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance upgradeCRemoteNexusInstance( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance cRemoteNexusInstance, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance value )
    {
        if ( cRemoteNexusInstance == null )
        {
            return null;
        }
        // Convert field alias
        value.setAlias( cRemoteNexusInstance.getAlias() );
        // Convert field instanceUrl
        value.setInstanceUrl( cRemoteNexusInstance.getInstanceUrl() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance upgradeCRemoteNexusInstance( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteNexusInstance )

    /**
     * Method upgradeCRemoteStorage.
     * 
     * @param cRemoteStorage
     * @return CRemoteStorage
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage upgradeCRemoteStorage( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteStorage cRemoteStorage )
    {
        return upgradeCRemoteStorage( cRemoteStorage, new org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage upgradeCRemoteStorage( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteStorage )

    /**
     * Method upgradeCRemoteStorage.
     * 
     * @param cRemoteStorage
     * @param value
     * @return CRemoteStorage
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage upgradeCRemoteStorage( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteStorage cRemoteStorage, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage value )
    {
        if ( cRemoteStorage == null )
        {
            return null;
        }
        // Convert field provider
        value.setProvider( cRemoteStorage.getProvider() );
        // Convert field url
        value.setUrl( cRemoteStorage.getUrl() );
        value.setAuthentication( upgradeCRemoteAuthentication( cRemoteStorage.getAuthentication() ) );
        value.setConnectionSettings( upgradeCRemoteConnectionSettings( cRemoteStorage.getConnectionSettings() ) );
        // Convert field inheritHttpProxySettings
        value.setInheritHttpProxySettings( cRemoteStorage.isInheritHttpProxySettings() );
        value.setHttpProxySettings( upgradeCRemoteHttpProxySettings( cRemoteStorage.getHttpProxySettings() ) );
        {
            java.util.List list = new java.util.ArrayList/*<CMirror>*/();
            for ( java.util.Iterator i = cRemoteStorage.getMirrors().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CMirror v = (org.sonatype.nexus.configuration.model.v1_4_5.CMirror) i.next();
                list.add( upgradeCMirror( v ) );
            }
            value.setMirrors( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage upgradeCRemoteStorage( org.sonatype.nexus.configuration.model.v1_4_5.CRemoteStorage, org.sonatype.nexus.configuration.model.v1_4_6.CRemoteStorage )

    /**
     * Method upgradeCRepository.
     * 
     * @param cRepository
     * @return CRepository
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepository upgradeCRepository( org.sonatype.nexus.configuration.model.v1_4_5.CRepository cRepository )
    {
        return upgradeCRepository( cRepository, new org.sonatype.nexus.configuration.model.v1_4_6.CRepository() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRepository upgradeCRepository( org.sonatype.nexus.configuration.model.v1_4_5.CRepository )

    /**
     * Method upgradeCRepository.
     * 
     * @param cRepository
     * @param value
     * @return CRepository
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepository upgradeCRepository( org.sonatype.nexus.configuration.model.v1_4_5.CRepository cRepository, org.sonatype.nexus.configuration.model.v1_4_6.CRepository value )
    {
        if ( cRepository == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cRepository.getId() );
        // Convert field name
        value.setName( cRepository.getName() );
        // Convert field providerRole
        value.setProviderRole( cRepository.getProviderRole() );
        // Convert field providerHint
        value.setProviderHint( cRepository.getProviderHint() );
        // Convert field pathPrefix
        value.setPathPrefix( cRepository.getPathPrefix() );
        // Convert field localStatus
        value.setLocalStatus( cRepository.getLocalStatus() );
        // Convert field notFoundCacheActive
        value.setNotFoundCacheActive( cRepository.isNotFoundCacheActive() );
        // Convert field notFoundCacheTTL
        value.setNotFoundCacheTTL( cRepository.getNotFoundCacheTTL() );
        // Convert field userManaged
        value.setUserManaged( cRepository.isUserManaged() );
        // Convert field exposed
        value.setExposed( cRepository.isExposed() );
        // Convert field browseable
        value.setBrowseable( cRepository.isBrowseable() );
        // Convert field writePolicy
        value.setWritePolicy( cRepository.getWritePolicy() );
        // Convert field indexable
        value.setIndexable( cRepository.isIndexable() );
        // Convert field searchable
        value.setSearchable( cRepository.isSearchable() );
        value.setLocalStorage( upgradeCLocalStorage( cRepository.getLocalStorage() ) );
        value.setRemoteStorage( upgradeCRemoteStorage( cRepository.getRemoteStorage() ) );
        {
            java.util.List list = new java.util.ArrayList/*<CMirror>*/();
            for ( java.util.Iterator i = cRepository.getMirrors().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CMirror v = (org.sonatype.nexus.configuration.model.v1_4_5.CMirror) i.next();
                list.add( upgradeCMirror( v ) );
            }
            value.setMirrors( list );
        }
        // Convert field externalConfiguration
        value.setExternalConfiguration( cRepository.getExternalConfiguration() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRepository upgradeCRepository( org.sonatype.nexus.configuration.model.v1_4_5.CRepository, org.sonatype.nexus.configuration.model.v1_4_6.CRepository )

    /**
     * Method upgradeCRepositoryGrouping.
     * 
     * @param cRepositoryGrouping
     * @return CRepositoryGrouping
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping upgradeCRepositoryGrouping( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryGrouping cRepositoryGrouping )
    {
        return upgradeCRepositoryGrouping( cRepositoryGrouping, new org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping upgradeCRepositoryGrouping( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryGrouping )

    /**
     * Method upgradeCRepositoryGrouping.
     * 
     * @param cRepositoryGrouping
     * @param value
     * @return CRepositoryGrouping
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping upgradeCRepositoryGrouping( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryGrouping cRepositoryGrouping, org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping value )
    {
        if ( cRepositoryGrouping == null )
        {
            return null;
        }
        {
            java.util.List list = new java.util.ArrayList/*<CPathMappingItem>*/();
            for ( java.util.Iterator i = cRepositoryGrouping.getPathMappings().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem v = (org.sonatype.nexus.configuration.model.v1_4_5.CPathMappingItem) i.next();
                list.add( upgradeCPathMappingItem( v ) );
            }
            value.setPathMappings( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping upgradeCRepositoryGrouping( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryGrouping, org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryGrouping )

    /**
     * Method upgradeCRepositoryTarget.
     * 
     * @param cRepositoryTarget
     * @return CRepositoryTarget
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget upgradeCRepositoryTarget( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget cRepositoryTarget )
    {
        return upgradeCRepositoryTarget( cRepositoryTarget, new org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget upgradeCRepositoryTarget( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget )

    /**
     * Method upgradeCRepositoryTarget.
     * 
     * @param cRepositoryTarget
     * @param value
     * @return CRepositoryTarget
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget upgradeCRepositoryTarget( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget cRepositoryTarget, org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget value )
    {
        if ( cRepositoryTarget == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cRepositoryTarget.getId() );
        // Convert field name
        value.setName( cRepositoryTarget.getName() );
        // Convert field contentClass
        value.setContentClass( cRepositoryTarget.getContentClass() );
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cRepositoryTarget.getPatterns().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setPatterns( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget upgradeCRepositoryTarget( org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget, org.sonatype.nexus.configuration.model.v1_4_6.CRepositoryTarget )

    /**
     * Method upgradeCRestApiSettings.
     * 
     * @param cRestApiSettings
     * @return CRestApiSettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings upgradeCRestApiSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRestApiSettings cRestApiSettings )
    {
        return upgradeCRestApiSettings( cRestApiSettings, new org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings upgradeCRestApiSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRestApiSettings )

    /**
     * Method upgradeCRestApiSettings.
     * 
     * @param cRestApiSettings
     * @param value
     * @return CRestApiSettings
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings upgradeCRestApiSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRestApiSettings cRestApiSettings, org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings value )
    {
        if ( cRestApiSettings == null )
        {
            return null;
        }
        // Convert field baseUrl
        value.setBaseUrl( cRestApiSettings.getBaseUrl() );
        // Convert field forceBaseUrl
        value.setForceBaseUrl( cRestApiSettings.isForceBaseUrl() );
        // Convert field uiTimeout
        value.setUiTimeout( cRestApiSettings.getUiTimeout() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings upgradeCRestApiSettings( org.sonatype.nexus.configuration.model.v1_4_5.CRestApiSettings, org.sonatype.nexus.configuration.model.v1_4_6.CRestApiSettings )

    /**
     * Method upgradeCRouting.
     * 
     * @param cRouting
     * @return CRouting
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRouting upgradeCRouting( org.sonatype.nexus.configuration.model.v1_4_5.CRouting cRouting )
    {
        return upgradeCRouting( cRouting, new org.sonatype.nexus.configuration.model.v1_4_6.CRouting() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRouting upgradeCRouting( org.sonatype.nexus.configuration.model.v1_4_5.CRouting )

    /**
     * Method upgradeCRouting.
     * 
     * @param cRouting
     * @param value
     * @return CRouting
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CRouting upgradeCRouting( org.sonatype.nexus.configuration.model.v1_4_5.CRouting cRouting, org.sonatype.nexus.configuration.model.v1_4_6.CRouting value )
    {
        if ( cRouting == null )
        {
            return null;
        }
        // Convert field resolveLinks
        value.setResolveLinks( cRouting.isResolveLinks() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CRouting upgradeCRouting( org.sonatype.nexus.configuration.model.v1_4_5.CRouting, org.sonatype.nexus.configuration.model.v1_4_6.CRouting )

    /**
     * Method upgradeCScheduleConfig.
     * 
     * @param cScheduleConfig
     * @return CScheduleConfig
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig upgradeCScheduleConfig( org.sonatype.nexus.configuration.model.v1_4_5.CScheduleConfig cScheduleConfig )
    {
        return upgradeCScheduleConfig( cScheduleConfig, new org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig upgradeCScheduleConfig( org.sonatype.nexus.configuration.model.v1_4_5.CScheduleConfig )

    /**
     * Method upgradeCScheduleConfig.
     * 
     * @param cScheduleConfig
     * @param value
     * @return CScheduleConfig
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig upgradeCScheduleConfig( org.sonatype.nexus.configuration.model.v1_4_5.CScheduleConfig cScheduleConfig, org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig value )
    {
        if ( cScheduleConfig == null )
        {
            return null;
        }
        // Convert field type
        value.setType( cScheduleConfig.getType() );
        // Convert field startDate
        value.setStartDate( cScheduleConfig.getStartDate() );
        // Convert field endDate
        value.setEndDate( cScheduleConfig.getEndDate() );
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cScheduleConfig.getDaysOfWeek().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setDaysOfWeek( list );
        }
        {
            java.util.List list = new java.util.ArrayList/*<String>*/();
            for ( java.util.Iterator i = cScheduleConfig.getDaysOfMonth().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setDaysOfMonth( list );
        }
        // Convert field cronCommand
        value.setCronCommand( cScheduleConfig.getCronCommand() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig upgradeCScheduleConfig( org.sonatype.nexus.configuration.model.v1_4_5.CScheduleConfig, org.sonatype.nexus.configuration.model.v1_4_6.CScheduleConfig )

    /**
     * Method upgradeCScheduledTask.
     * 
     * @param cScheduledTask
     * @return CScheduledTask
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask upgradeCScheduledTask( org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask cScheduledTask )
    {
        return upgradeCScheduledTask( cScheduledTask, new org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask upgradeCScheduledTask( org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask )

    /**
     * Method upgradeCScheduledTask.
     * 
     * @param cScheduledTask
     * @param value
     * @return CScheduledTask
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask upgradeCScheduledTask( org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask cScheduledTask, org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask value )
    {
        if ( cScheduledTask == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cScheduledTask.getId() );
        // Convert field name
        value.setName( cScheduledTask.getName() );
        // Convert field enabled
        value.setEnabled( cScheduledTask.isEnabled() );
        // Convert field type
        value.setType( cScheduledTask.getType() );
        // Convert field status
        value.setStatus( cScheduledTask.getStatus() );
        // Convert field lastRun
        value.setLastRun( cScheduledTask.getLastRun() );
        // Convert field nextRun
        value.setNextRun( cScheduledTask.getNextRun() );
        value.setSchedule( upgradeCScheduleConfig( cScheduledTask.getSchedule() ) );
        {
            java.util.List list = new java.util.ArrayList/*<CProps>*/();
            for ( java.util.Iterator i = cScheduledTask.getProperties().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CProps v = (org.sonatype.nexus.configuration.model.v1_4_5.CProps) i.next();
                list.add( upgradeCProps( v ) );
            }
            value.setProperties( list );
        }

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask upgradeCScheduledTask( org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask, org.sonatype.nexus.configuration.model.v1_4_6.CScheduledTask )

    /**
     * Method upgradeCSmtpConfiguration.
     * 
     * @param cSmtpConfiguration
     * @return CSmtpConfiguration
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration upgradeCSmtpConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.CSmtpConfiguration cSmtpConfiguration )
    {
        return upgradeCSmtpConfiguration( cSmtpConfiguration, new org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration upgradeCSmtpConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.CSmtpConfiguration )

    /**
     * Method upgradeCSmtpConfiguration.
     * 
     * @param cSmtpConfiguration
     * @param value
     * @return CSmtpConfiguration
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration upgradeCSmtpConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.CSmtpConfiguration cSmtpConfiguration, org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration value )
    {
        if ( cSmtpConfiguration == null )
        {
            return null;
        }
        // Convert field hostname
        value.setHostname( cSmtpConfiguration.getHostname() );
        // Convert field port
        value.setPort( cSmtpConfiguration.getPort() );
        // Convert field sslEnabled
        value.setSslEnabled( cSmtpConfiguration.isSslEnabled() );
        // Convert field tlsEnabled
        value.setTlsEnabled( cSmtpConfiguration.isTlsEnabled() );
        // Convert field username
        value.setUsername( cSmtpConfiguration.getUsername() );
        // Convert field password
        value.setPassword( cSmtpConfiguration.getPassword() );
        // Convert field debugMode
        value.setDebugMode( cSmtpConfiguration.isDebugMode() );
        // Convert field systemEmailAddress
        value.setSystemEmailAddress( cSmtpConfiguration.getSystemEmailAddress() );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration upgradeCSmtpConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.CSmtpConfiguration, org.sonatype.nexus.configuration.model.v1_4_6.CSmtpConfiguration )

    /**
     * Method upgradeConfiguration.
     * 
     * @param configuration
     * @return Configuration
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.Configuration upgradeConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.Configuration configuration )
    {
        return upgradeConfiguration( configuration, new org.sonatype.nexus.configuration.model.v1_4_6.Configuration() );
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.Configuration upgradeConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.Configuration )

    /**
     * Method upgradeConfiguration.
     * 
     * @param configuration
     * @param value
     * @return Configuration
     */
    public org.sonatype.nexus.configuration.model.v1_4_6.Configuration upgradeConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.Configuration configuration, org.sonatype.nexus.configuration.model.v1_4_6.Configuration value )
    {
        if ( configuration == null )
        {
            return null;
        }
        value.setVersion( "1.4.6" );
        value.setGlobalConnectionSettings( upgradeCRemoteConnectionSettings( configuration.getGlobalConnectionSettings() ) );
        value.setGlobalHttpProxySettings( upgradeCRemoteHttpProxySettings( configuration.getGlobalHttpProxySettings() ) );
        value.setRestApi( upgradeCRestApiSettings( configuration.getRestApi() ) );
        value.setHttpProxy( upgradeCHttpProxySettings( configuration.getHttpProxy() ) );
        value.setRouting( upgradeCRouting( configuration.getRouting() ) );
        {
            java.util.List list = new java.util.ArrayList/*<CRepository>*/();
            for ( java.util.Iterator i = configuration.getRepositories().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CRepository v = (org.sonatype.nexus.configuration.model.v1_4_5.CRepository) i.next();
                list.add( upgradeCRepository( v ) );
            }
            value.setRepositories( list );
        }
        value.setRepositoryGrouping( upgradeCRepositoryGrouping( configuration.getRepositoryGrouping() ) );
        {
            java.util.List list = new java.util.ArrayList/*<CRemoteNexusInstance>*/();
            for ( java.util.Iterator i = configuration.getRemoteNexusInstances().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance v = (org.sonatype.nexus.configuration.model.v1_4_5.CRemoteNexusInstance) i.next();
                list.add( upgradeCRemoteNexusInstance( v ) );
            }
            value.setRemoteNexusInstances( list );
        }
        {
            java.util.List list = new java.util.ArrayList/*<CRepositoryTarget>*/();
            for ( java.util.Iterator i = configuration.getRepositoryTargets().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget v = (org.sonatype.nexus.configuration.model.v1_4_5.CRepositoryTarget) i.next();
                list.add( upgradeCRepositoryTarget( v ) );
            }
            value.setRepositoryTargets( list );
        }
        {
            java.util.List list = new java.util.ArrayList/*<CScheduledTask>*/();
            for ( java.util.Iterator i = configuration.getTasks().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask v = (org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask) i.next();
                list.add( upgradeCScheduledTask( v ) );
            }
            value.setTasks( list );
        }
        value.setSmtpConfiguration( upgradeCSmtpConfiguration( configuration.getSmtpConfiguration() ) );
        value.setErrorReporting( upgradeCErrorReporting( configuration.getErrorReporting() ) );
        {
            java.util.List list = new java.util.ArrayList/*<CPlugin>*/();
            for ( java.util.Iterator i = configuration.getPlugins().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_4_5.CPlugin v = (org.sonatype.nexus.configuration.model.v1_4_5.CPlugin) i.next();
                list.add( upgradeCPlugin( v ) );
            }
            value.setPlugins( list );
        }
        value.setNotification( upgradeCNotification( configuration.getNotification() ) );

        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_4_6.Configuration upgradeConfiguration( org.sonatype.nexus.configuration.model.v1_4_5.Configuration, org.sonatype.nexus.configuration.model.v1_4_6.Configuration )


}
