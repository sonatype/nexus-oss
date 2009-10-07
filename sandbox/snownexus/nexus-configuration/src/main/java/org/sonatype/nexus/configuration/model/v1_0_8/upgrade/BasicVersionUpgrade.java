/*
 * $Id$
 */

package org.sonatype.nexus.configuration.model.v1_0_8.upgrade;

/**
 * Converts between version 1.0.7 and version 1.0.8 of the model.
 * 
 * @version $Revision$ $Date$
 */
public class BasicVersionUpgrade implements org.sonatype.nexus.configuration.model.v1_0_8.upgrade.VersionUpgrade {


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method upgradeCGroupsSetting.
     * 
     * @param cGroupsSetting
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting upgradeCGroupsSetting(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSetting cGroupsSetting)
    {
        return upgradeCGroupsSetting( cGroupsSetting, new org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting upgradeCGroupsSetting(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSetting) 

    /**
     * Method upgradeCGroupsSetting.
     * 
     * @param cGroupsSetting
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting upgradeCGroupsSetting(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSetting cGroupsSetting, org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting value)
    {
        if ( cGroupsSetting == null )
        {
            return null;
        }
        // Convert field stopItemSearchOnFirstFoundFile
        value.setStopItemSearchOnFirstFoundFile( cGroupsSetting.isStopItemSearchOnFirstFoundFile() );
        // Convert field mergeMetadata
        value.setMergeMetadata( cGroupsSetting.isMergeMetadata() );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting upgradeCGroupsSetting(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSetting, org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting) 

    /**
     * Method upgradeCGroupsSettingPathMappingItem.
     * 
     * @param cGroupsSettingPathMappingItem
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem upgradeCGroupsSettingPathMappingItem(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem cGroupsSettingPathMappingItem)
    {
        return upgradeCGroupsSettingPathMappingItem( cGroupsSettingPathMappingItem, new org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem upgradeCGroupsSettingPathMappingItem(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem) 

    /**
     * Method upgradeCGroupsSettingPathMappingItem.
     * 
     * @param cGroupsSettingPathMappingItem
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem upgradeCGroupsSettingPathMappingItem(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem cGroupsSettingPathMappingItem, org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem value)
    {
        if ( cGroupsSettingPathMappingItem == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cGroupsSettingPathMappingItem.getId() );
        // Convert field groupId
        value.setGroupId( cGroupsSettingPathMappingItem.getGroupId() );
        // Convert field routePattern
        value.setRoutePattern( cGroupsSettingPathMappingItem.getRoutePattern() );
        // Convert field routeType
        value.setRouteType( cGroupsSettingPathMappingItem.getRouteType() );
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cGroupsSettingPathMappingItem.getRepositories().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setRepositories( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem upgradeCGroupsSettingPathMappingItem(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem, org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem) 

    /**
     * Method upgradeCHttpProxySettings.
     * 
     * @param cHttpProxySettings
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings upgradeCHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CHttpProxySettings cHttpProxySettings)
    {
        return upgradeCHttpProxySettings( cHttpProxySettings, new org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings upgradeCHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CHttpProxySettings) 

    /**
     * Method upgradeCHttpProxySettings.
     * 
     * @param cHttpProxySettings
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings upgradeCHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CHttpProxySettings cHttpProxySettings, org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings value)
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings upgradeCHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CHttpProxySettings, org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings) 

    /**
     * Method upgradeCLocalStorage.
     * 
     * @param cLocalStorage
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage upgradeCLocalStorage(org.sonatype.nexus.configuration.model.v1_0_7.CLocalStorage cLocalStorage)
    {
        return upgradeCLocalStorage( cLocalStorage, new org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage upgradeCLocalStorage(org.sonatype.nexus.configuration.model.v1_0_7.CLocalStorage) 

    /**
     * Method upgradeCLocalStorage.
     * 
     * @param cLocalStorage
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage upgradeCLocalStorage(org.sonatype.nexus.configuration.model.v1_0_7.CLocalStorage cLocalStorage, org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage value)
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage upgradeCLocalStorage(org.sonatype.nexus.configuration.model.v1_0_7.CLocalStorage, org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage) 

    /**
     * Method upgradeCProps.
     * 
     * @param cProps
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CProps
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CProps upgradeCProps(org.sonatype.nexus.configuration.model.v1_0_7.CProps cProps)
    {
        return upgradeCProps( cProps, new org.sonatype.nexus.configuration.model.v1_0_8.CProps() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CProps upgradeCProps(org.sonatype.nexus.configuration.model.v1_0_7.CProps) 

    /**
     * Method upgradeCProps.
     * 
     * @param cProps
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CProps
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CProps upgradeCProps(org.sonatype.nexus.configuration.model.v1_0_7.CProps cProps, org.sonatype.nexus.configuration.model.v1_0_8.CProps value)
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CProps upgradeCProps(org.sonatype.nexus.configuration.model.v1_0_7.CProps, org.sonatype.nexus.configuration.model.v1_0_8.CProps) 

    /**
     * Method upgradeCRemoteAuthentication.
     * 
     * @param cRemoteAuthentication
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication upgradeCRemoteAuthentication(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteAuthentication cRemoteAuthentication)
    {
        return upgradeCRemoteAuthentication( cRemoteAuthentication, new org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication upgradeCRemoteAuthentication(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteAuthentication) 

    /**
     * Method upgradeCRemoteAuthentication.
     * 
     * @param cRemoteAuthentication
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication upgradeCRemoteAuthentication(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteAuthentication cRemoteAuthentication, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication value)
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
        // Convert field privateKey
        value.setPrivateKey( cRemoteAuthentication.getPrivateKey() );
        // Convert field passphrase
        value.setPassphrase( cRemoteAuthentication.getPassphrase() );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication upgradeCRemoteAuthentication(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteAuthentication, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication) 

    /**
     * Method upgradeCRemoteConnectionSettings.
     * 
     * @param cRemoteConnectionSettings
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings upgradeCRemoteConnectionSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteConnectionSettings cRemoteConnectionSettings)
    {
        return upgradeCRemoteConnectionSettings( cRemoteConnectionSettings, new org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings upgradeCRemoteConnectionSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteConnectionSettings) 

    /**
     * Method upgradeCRemoteConnectionSettings.
     * 
     * @param cRemoteConnectionSettings
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings upgradeCRemoteConnectionSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteConnectionSettings cRemoteConnectionSettings, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings value)
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings upgradeCRemoteConnectionSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteConnectionSettings, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings) 

    /**
     * Method upgradeCRemoteHttpProxySettings.
     * 
     * @param cRemoteHttpProxySettings
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteHttpProxySettings cRemoteHttpProxySettings)
    {
        return upgradeCRemoteHttpProxySettings( cRemoteHttpProxySettings, new org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteHttpProxySettings) 

    /**
     * Method upgradeCRemoteHttpProxySettings.
     * 
     * @param cRemoteHttpProxySettings
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteHttpProxySettings cRemoteHttpProxySettings, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings value)
    {
        if ( cRemoteHttpProxySettings == null )
        {
            return null;
        }
        // Convert field proxyHostname
        value.setProxyHostname( cRemoteHttpProxySettings.getProxyHostname() );
        // Convert field proxyPort
        value.setProxyPort( cRemoteHttpProxySettings.getProxyPort() );
        value.setAuthentication( upgradeCRemoteAuthentication( cRemoteHttpProxySettings.getAuthentication() ) );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteHttpProxySettings, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings) 

    /**
     * Method upgradeCRemoteNexusInstance.
     * 
     * @param cRemoteNexusInstance
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance upgradeCRemoteNexusInstance(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance cRemoteNexusInstance)
    {
        return upgradeCRemoteNexusInstance( cRemoteNexusInstance, new org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance upgradeCRemoteNexusInstance(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance) 

    /**
     * Method upgradeCRemoteNexusInstance.
     * 
     * @param cRemoteNexusInstance
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance upgradeCRemoteNexusInstance(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance cRemoteNexusInstance, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance value)
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance upgradeCRemoteNexusInstance(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance) 

    /**
     * Method upgradeCRemoteStorage.
     * 
     * @param cRemoteStorage
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage upgradeCRemoteStorage(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteStorage cRemoteStorage)
    {
        return upgradeCRemoteStorage( cRemoteStorage, new org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage upgradeCRemoteStorage(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteStorage) 

    /**
     * Method upgradeCRemoteStorage.
     * 
     * @param cRemoteStorage
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage upgradeCRemoteStorage(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteStorage cRemoteStorage, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage value)
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
        value.setHttpProxySettings( upgradeCRemoteHttpProxySettings( cRemoteStorage.getHttpProxySettings() ) );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage upgradeCRemoteStorage(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteStorage, org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage) 

    /**
     * Method upgradeCRepository.
     * 
     * @param cRepository
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CRepository
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepository upgradeCRepository(org.sonatype.nexus.configuration.model.v1_0_7.CRepository cRepository)
    {
        return upgradeCRepository( cRepository, new org.sonatype.nexus.configuration.model.v1_0_8.CRepository() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepository upgradeCRepository(org.sonatype.nexus.configuration.model.v1_0_7.CRepository) 

    /**
     * Method upgradeCRepository.
     * 
     * @param cRepository
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CRepository
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepository upgradeCRepository(org.sonatype.nexus.configuration.model.v1_0_7.CRepository cRepository, org.sonatype.nexus.configuration.model.v1_0_8.CRepository value)
    {
        if ( cRepository == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cRepository.getId() );
        // Convert field name
        value.setName( cRepository.getName() );
        // Convert field localStatus
        value.setLocalStatus( cRepository.getLocalStatus() );
        // Convert field proxyMode
        value.setProxyMode( cRepository.getProxyMode() );
        // Convert field type
        value.setType( cRepository.getType() );
        // Convert field allowWrite
        value.setAllowWrite( cRepository.isAllowWrite() );
        // Convert field userManaged
        value.setUserManaged( cRepository.isUserManaged() );
        // Convert field exposed
        value.setExposed( cRepository.isExposed() );
        // Convert field notFoundCacheActive
        value.setNotFoundCacheActive( cRepository.isNotFoundCacheActive() );
        // Convert field browseable
        value.setBrowseable( cRepository.isBrowseable() );
        // Convert field indexable
        value.setIndexable( cRepository.isIndexable() );
        // Convert field notFoundCacheTTL
        value.setNotFoundCacheTTL( cRepository.getNotFoundCacheTTL() );
        // Convert field artifactMaxAge
        value.setArtifactMaxAge( cRepository.getArtifactMaxAge() );
        // Convert field metadataMaxAge
        value.setMetadataMaxAge( cRepository.getMetadataMaxAge() );
        // Convert field maintainProxiedRepositoryMetadata
        value.setMaintainProxiedRepositoryMetadata( cRepository.isMaintainProxiedRepositoryMetadata() );
        // Convert field repositoryPolicy
        value.setRepositoryPolicy( cRepository.getRepositoryPolicy() );
        // Convert field checksumPolicy
        value.setChecksumPolicy( cRepository.getChecksumPolicy() );
        // Convert field downloadRemoteIndexes
        value.setDownloadRemoteIndexes( cRepository.isDownloadRemoteIndexes() );
        value.setLocalStorage( upgradeCLocalStorage( cRepository.getLocalStorage() ) );
        value.setRemoteStorage( upgradeCRemoteStorage( cRepository.getRemoteStorage() ) );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepository upgradeCRepository(org.sonatype.nexus.configuration.model.v1_0_7.CRepository, org.sonatype.nexus.configuration.model.v1_0_8.CRepository) 

    /**
     * Method upgradeCRepositoryGroup.
     * 
     * @param cRepositoryGroup
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup cRepositoryGroup)
    {
        return upgradeCRepositoryGroup( cRepositoryGroup, new org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup) 

    /**
     * Method upgradeCRepositoryGroup.
     * 
     * @param cRepositoryGroup
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup cRepositoryGroup, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup value)
    {
        if ( cRepositoryGroup == null )
        {
            return null;
        }
        // Convert field groupId
        value.setGroupId( cRepositoryGroup.getGroupId() );
        // Convert field name
        value.setName( cRepositoryGroup.getName() );
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cRepositoryGroup.getRepositories().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setRepositories( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup) 

    /**
     * Method upgradeCRepositoryGrouping.
     * 
     * @param cRepositoryGrouping
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping upgradeCRepositoryGrouping(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGrouping cRepositoryGrouping)
    {
        return upgradeCRepositoryGrouping( cRepositoryGrouping, new org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping upgradeCRepositoryGrouping(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGrouping) 

    /**
     * Method upgradeCRepositoryGrouping.
     * 
     * @param cRepositoryGrouping
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping upgradeCRepositoryGrouping(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGrouping cRepositoryGrouping, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping value)
    {
        if ( cRepositoryGrouping == null )
        {
            return null;
        }
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cRepositoryGrouping.getPathMappings().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem v = (org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem) i.next();
                list.add( upgradeCGroupsSettingPathMappingItem( v ) );
            }
            value.setPathMappings( list );
        }
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cRepositoryGrouping.getRepositoryGroups().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup v = (org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup) i.next();
                list.add( upgradeCRepositoryGroup( v ) );
            }
            value.setRepositoryGroups( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping upgradeCRepositoryGrouping(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGrouping, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping) 

    /**
     * Method upgradeCRepositoryShadow.
     * 
     * @param cRepositoryShadow
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow upgradeCRepositoryShadow(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow cRepositoryShadow)
    {
        return upgradeCRepositoryShadow( cRepositoryShadow, new org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow upgradeCRepositoryShadow(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow) 

    /**
     * Method upgradeCRepositoryShadow.
     * 
     * @param cRepositoryShadow
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow upgradeCRepositoryShadow(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow cRepositoryShadow, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow value)
    {
        if ( cRepositoryShadow == null )
        {
            return null;
        }
        // Convert field id
        value.setId( cRepositoryShadow.getId() );
        // Convert field name
        value.setName( cRepositoryShadow.getName() );
        // Convert field localStatus
        value.setLocalStatus( cRepositoryShadow.getLocalStatus() );
        // Convert field shadowOf
        value.setShadowOf( cRepositoryShadow.getShadowOf() );
        // Convert field type
        value.setType( cRepositoryShadow.getType() );
        // Convert field syncAtStartup
        value.setSyncAtStartup( cRepositoryShadow.isSyncAtStartup() );
        // Convert field userManaged
        value.setUserManaged( cRepositoryShadow.isUserManaged() );
        // Convert field exposed
        value.setExposed( cRepositoryShadow.isExposed() );
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cRepositoryShadow.getArtifactVersionConstraints().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint v = (org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint) i.next();
                list.add( upgradeCRepositoryShadowArtifactVersionConstraint( v ) );
            }
            value.setArtifactVersionConstraints( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow upgradeCRepositoryShadow(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow) 

    /**
     * Method upgradeCRepositoryShadowArtifactVersionConstraint.
     * 
     * @param cRepositoryShadowArtifactVersionConstraint
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint upgradeCRepositoryShadowArtifactVersionConstraint(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint cRepositoryShadowArtifactVersionConstraint)
    {
        return upgradeCRepositoryShadowArtifactVersionConstraint( cRepositoryShadowArtifactVersionConstraint, new org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint upgradeCRepositoryShadowArtifactVersionConstraint(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint) 

    /**
     * Method upgradeCRepositoryShadowArtifactVersionConstraint.
     * 
     * @param cRepositoryShadowArtifactVersionConstraint
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint upgradeCRepositoryShadowArtifactVersionConstraint(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint cRepositoryShadowArtifactVersionConstraint, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint value)
    {
        if ( cRepositoryShadowArtifactVersionConstraint == null )
        {
            return null;
        }
        // Convert field groupId
        value.setGroupId( cRepositoryShadowArtifactVersionConstraint.getGroupId() );
        // Convert field artifactId
        value.setArtifactId( cRepositoryShadowArtifactVersionConstraint.getArtifactId() );
        // Convert field version
        value.setVersion( cRepositoryShadowArtifactVersionConstraint.getVersion() );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint upgradeCRepositoryShadowArtifactVersionConstraint(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint) 

    /**
     * Method upgradeCRepositoryTarget.
     * 
     * @param cRepositoryTarget
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget upgradeCRepositoryTarget(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget cRepositoryTarget)
    {
        return upgradeCRepositoryTarget( cRepositoryTarget, new org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget upgradeCRepositoryTarget(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget) 

    /**
     * Method upgradeCRepositoryTarget.
     * 
     * @param cRepositoryTarget
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget upgradeCRepositoryTarget(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget cRepositoryTarget, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget value)
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
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cRepositoryTarget.getPatterns().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setPatterns( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget upgradeCRepositoryTarget(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget, org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget) 

    /**
     * Method upgradeCRestApiSettings.
     * 
     * @param cRestApiSettings
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings upgradeCRestApiSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRestApiSettings cRestApiSettings)
    {
        return upgradeCRestApiSettings( cRestApiSettings, new org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings upgradeCRestApiSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRestApiSettings) 

    /**
     * Method upgradeCRestApiSettings.
     * 
     * @param cRestApiSettings
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings upgradeCRestApiSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRestApiSettings cRestApiSettings, org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings value)
    {
        if ( cRestApiSettings == null )
        {
            return null;
        }
        // Convert field baseUrl
        value.setBaseUrl( cRestApiSettings.getBaseUrl() );
        // Convert field accessAllowedFrom
        value.setAccessAllowedFrom( cRestApiSettings.getAccessAllowedFrom() );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings upgradeCRestApiSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRestApiSettings, org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings) 

    /**
     * Method upgradeCRouting.
     * 
     * @param cRouting
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CRouting
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRouting upgradeCRouting(org.sonatype.nexus.configuration.model.v1_0_7.CRouting cRouting)
    {
        return upgradeCRouting( cRouting, new org.sonatype.nexus.configuration.model.v1_0_8.CRouting() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRouting upgradeCRouting(org.sonatype.nexus.configuration.model.v1_0_7.CRouting) 

    /**
     * Method upgradeCRouting.
     * 
     * @param cRouting
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CRouting
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRouting upgradeCRouting(org.sonatype.nexus.configuration.model.v1_0_7.CRouting cRouting, org.sonatype.nexus.configuration.model.v1_0_8.CRouting value)
    {
        if ( cRouting == null )
        {
            return null;
        }
        // Convert field followLinks
        value.setFollowLinks( cRouting.isFollowLinks() );
        // Convert field notFoundCacheTTL
        value.setNotFoundCacheTTL( cRouting.getNotFoundCacheTTL() );
        value.setGroups( upgradeCGroupsSetting( cRouting.getGroups() ) );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CRouting upgradeCRouting(org.sonatype.nexus.configuration.model.v1_0_7.CRouting, org.sonatype.nexus.configuration.model.v1_0_8.CRouting) 

    /**
     * Method upgradeCScheduleConfig.
     * 
     * @param cScheduleConfig
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfi
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig upgradeCScheduleConfig(org.sonatype.nexus.configuration.model.v1_0_7.CScheduleConfig cScheduleConfig)
    {
        return upgradeCScheduleConfig( cScheduleConfig, new org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig upgradeCScheduleConfig(org.sonatype.nexus.configuration.model.v1_0_7.CScheduleConfig) 

    /**
     * Method upgradeCScheduleConfig.
     * 
     * @param cScheduleConfig
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfi
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig upgradeCScheduleConfig(org.sonatype.nexus.configuration.model.v1_0_7.CScheduleConfig cScheduleConfig, org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig value)
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
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cScheduleConfig.getDaysOfWeek().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setDaysOfWeek( list );
        }
        {
            java.util.List list = new java.util.ArrayList();
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig upgradeCScheduleConfig(org.sonatype.nexus.configuration.model.v1_0_7.CScheduleConfig, org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig) 

    /**
     * Method upgradeCScheduledTask.
     * 
     * @param cScheduledTask
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask upgradeCScheduledTask(org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask cScheduledTask)
    {
        return upgradeCScheduledTask( cScheduledTask, new org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask upgradeCScheduledTask(org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask) 

    /**
     * Method upgradeCScheduledTask.
     * 
     * @param cScheduledTask
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask upgradeCScheduledTask(org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask cScheduledTask, org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask value)
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
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cScheduledTask.getProperties().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CProps v = (org.sonatype.nexus.configuration.model.v1_0_7.CProps) i.next();
                list.add( upgradeCProps( v ) );
            }
            value.setProperties( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask upgradeCScheduledTask(org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask, org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask) 

    /**
     * Method upgradeCSecurity.
     * 
     * @param cSecurity
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CSecurity
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CSecurity upgradeCSecurity(org.sonatype.nexus.configuration.model.v1_0_7.CSecurity cSecurity)
    {
        return upgradeCSecurity( cSecurity, new org.sonatype.nexus.configuration.model.v1_0_8.CSecurity() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CSecurity upgradeCSecurity(org.sonatype.nexus.configuration.model.v1_0_7.CSecurity) 

    /**
     * Method upgradeCSecurity.
     * 
     * @param cSecurity
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.CSecurity
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CSecurity upgradeCSecurity(org.sonatype.nexus.configuration.model.v1_0_7.CSecurity cSecurity, org.sonatype.nexus.configuration.model.v1_0_8.CSecurity value)
    {
        if ( cSecurity == null )
        {
            return null;
        }
        // Convert field enabled
        value.setEnabled( cSecurity.isEnabled() );
        // Convert field anonymousAccessEnabled
        value.setAnonymousAccessEnabled( cSecurity.isAnonymousAccessEnabled() );
        // Convert field anonymousUsername
        value.setAnonymousUsername( cSecurity.getAnonymousUsername() );
        // Convert field anonymousPassword
        value.setAnonymousPassword( cSecurity.getAnonymousPassword() );
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = cSecurity.getRealms().iterator(); i.hasNext(); )
            {
                String v = (String) i.next();
                list.add( v );
            }
            value.setRealms( list );
        }
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CSecurity upgradeCSecurity(org.sonatype.nexus.configuration.model.v1_0_7.CSecurity, org.sonatype.nexus.configuration.model.v1_0_8.CSecurity) 

    /**
     * Method upgradeCSmtpConfiguration.
     * 
     * @param cSmtpConfiguration
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration upgradeCSmtpConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.CSmtpConfiguration cSmtpConfiguration)
    {
        return upgradeCSmtpConfiguration( cSmtpConfiguration, new org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration upgradeCSmtpConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.CSmtpConfiguration) 

    /**
     * Method upgradeCSmtpConfiguration.
     * 
     * @param cSmtpConfiguration
     * @param value
     * @return
     * org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration upgradeCSmtpConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.CSmtpConfiguration cSmtpConfiguration, org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration value)
    {
        if ( cSmtpConfiguration == null )
        {
            return null;
        }
        // Convert field host
        value.setHost( cSmtpConfiguration.getHost() );
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
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration upgradeCSmtpConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.CSmtpConfiguration, org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration) 

    /**
     * Method upgradeConfiguration.
     * 
     * @param configuration
     * @return org.sonatype.nexus.configuration.model.v1_0_8.Configuration
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.Configuration upgradeConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.Configuration configuration)
    {
        return upgradeConfiguration( configuration, new org.sonatype.nexus.configuration.model.v1_0_8.Configuration() );
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.Configuration upgradeConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.Configuration) 

    /**
     * Method upgradeConfiguration.
     * 
     * @param configuration
     * @param value
     * @return org.sonatype.nexus.configuration.model.v1_0_8.Configuration
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.Configuration upgradeConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.Configuration configuration, org.sonatype.nexus.configuration.model.v1_0_8.Configuration value)
    {
        if ( configuration == null )
        {
            return null;
        }
        // Convert field version
        value.setVersion( configuration.getVersion() );
        value.setSecurity( upgradeCSecurity( configuration.getSecurity() ) );
        value.setGlobalConnectionSettings( upgradeCRemoteConnectionSettings( configuration.getGlobalConnectionSettings() ) );
        value.setGlobalHttpProxySettings( upgradeCRemoteHttpProxySettings( configuration.getGlobalHttpProxySettings() ) );
        value.setRestApi( upgradeCRestApiSettings( configuration.getRestApi() ) );
        value.setHttpProxy( upgradeCHttpProxySettings( configuration.getHttpProxy() ) );
        value.setRouting( upgradeCRouting( configuration.getRouting() ) );
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = configuration.getRepositories().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CRepository v = (org.sonatype.nexus.configuration.model.v1_0_7.CRepository) i.next();
                list.add( upgradeCRepository( v ) );
            }
            value.setRepositories( list );
        }
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = configuration.getRepositoryShadows().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow v = (org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow) i.next();
                list.add( upgradeCRepositoryShadow( v ) );
            }
            value.setRepositoryShadows( list );
        }
        value.setRepositoryGrouping( upgradeCRepositoryGrouping( configuration.getRepositoryGrouping() ) );
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = configuration.getRemoteNexusInstances().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance v = (org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance) i.next();
                list.add( upgradeCRemoteNexusInstance( v ) );
            }
            value.setRemoteNexusInstances( list );
        }
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = configuration.getRepositoryTargets().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget v = (org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget) i.next();
                list.add( upgradeCRepositoryTarget( v ) );
            }
            value.setRepositoryTargets( list );
        }
        {
            java.util.List list = new java.util.ArrayList();
            for ( java.util.Iterator i = configuration.getTasks().iterator(); i.hasNext(); )
            {
                org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask v = (org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask) i.next();
                list.add( upgradeCScheduledTask( v ) );
            }
            value.setTasks( list );
        }
        value.setSmtpConfiguration( upgradeCSmtpConfiguration( configuration.getSmtpConfiguration() ) );
        
        return value;
    } //-- org.sonatype.nexus.configuration.model.v1_0_8.Configuration upgradeConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.Configuration, org.sonatype.nexus.configuration.model.v1_0_8.Configuration) 


}
