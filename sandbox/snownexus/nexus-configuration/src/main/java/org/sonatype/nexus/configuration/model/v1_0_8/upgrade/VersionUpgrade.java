/*
 * $Id$
 */

package org.sonatype.nexus.configuration.model.v1_0_8.upgrade;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/


/**
 * Converts between version 1.0.7 and version 1.0.8 of the model.
 * 
 * @version $Revision$ $Date$
 */
public interface VersionUpgrade {


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * 
     * 
     * @param cGroupsSetting
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSetting upgradeCGroupsSetting(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSetting cGroupsSetting);
    /**
     * 
     * 
     * @param cGroupsSettingPathMappingItem
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CGroupsSettingPathMappingItem upgradeCGroupsSettingPathMappingItem(org.sonatype.nexus.configuration.model.v1_0_7.CGroupsSettingPathMappingItem cGroupsSettingPathMappingItem);
    /**
     * 
     * 
     * @param cHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CHttpProxySettings upgradeCHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CHttpProxySettings cHttpProxySettings);
    /**
     * 
     * 
     * @param cLocalStorage
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CLocalStorage upgradeCLocalStorage(org.sonatype.nexus.configuration.model.v1_0_7.CLocalStorage cLocalStorage);
    /**
     * 
     * 
     * @param cProps
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CProps upgradeCProps(org.sonatype.nexus.configuration.model.v1_0_7.CProps cProps);
    /**
     * 
     * 
     * @param cRemoteAuthentication
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteAuthentication upgradeCRemoteAuthentication(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteAuthentication cRemoteAuthentication);
    /**
     * 
     * 
     * @param cRemoteConnectionSettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteConnectionSettings upgradeCRemoteConnectionSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteConnectionSettings cRemoteConnectionSettings);
    /**
     * 
     * 
     * @param cRemoteHttpProxySettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteHttpProxySettings upgradeCRemoteHttpProxySettings(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteHttpProxySettings cRemoteHttpProxySettings);
    /**
     * 
     * 
     * @param cRemoteNexusInstance
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteNexusInstance upgradeCRemoteNexusInstance(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteNexusInstance cRemoteNexusInstance);
    /**
     * 
     * 
     * @param cRemoteStorage
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRemoteStorage upgradeCRemoteStorage(org.sonatype.nexus.configuration.model.v1_0_7.CRemoteStorage cRemoteStorage);
    /**
     * 
     * 
     * @param cRepository
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepository upgradeCRepository(org.sonatype.nexus.configuration.model.v1_0_7.CRepository cRepository);
    /**
     * 
     * 
     * @param cRepositoryGroup
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup cRepositoryGroup);
    /**
     * 
     * 
     * @param cRepositoryGrouping
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGrouping upgradeCRepositoryGrouping(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGrouping cRepositoryGrouping);
    /**
     * 
     * 
     * @param cRepositoryShadow
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow upgradeCRepositoryShadow(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadow cRepositoryShadow);
    /**
     * 
     * 
     * @param cRepositoryShadowArtifactVersionConstraint
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadowArtifactVersionConstraint upgradeCRepositoryShadowArtifactVersionConstraint(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryShadowArtifactVersionConstraint cRepositoryShadowArtifactVersionConstraint);
    /**
     * 
     * 
     * @param cRepositoryTarget
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryTarget upgradeCRepositoryTarget(org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryTarget cRepositoryTarget);
    /**
     * 
     * 
     * @param cRestApiSettings
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRestApiSettings upgradeCRestApiSettings(org.sonatype.nexus.configuration.model.v1_0_7.CRestApiSettings cRestApiSettings);
    /**
     * 
     * 
     * @param cRouting
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CRouting upgradeCRouting(org.sonatype.nexus.configuration.model.v1_0_7.CRouting cRouting);
    /**
     * 
     * 
     * @param cScheduleConfig
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CScheduleConfig upgradeCScheduleConfig(org.sonatype.nexus.configuration.model.v1_0_7.CScheduleConfig cScheduleConfig);
    /**
     * 
     * 
     * @param cScheduledTask
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask upgradeCScheduledTask(org.sonatype.nexus.configuration.model.v1_0_7.CScheduledTask cScheduledTask);
    /**
     * 
     * 
     * @param cSecurity
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CSecurity upgradeCSecurity(org.sonatype.nexus.configuration.model.v1_0_7.CSecurity cSecurity);
    /**
     * 
     * 
     * @param cSmtpConfiguration
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.CSmtpConfiguration upgradeCSmtpConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.CSmtpConfiguration cSmtpConfiguration);
    /**
     * 
     * 
     * @param configuration
     */
    public org.sonatype.nexus.configuration.model.v1_0_8.Configuration upgradeConfiguration(org.sonatype.nexus.configuration.model.v1_0_7.Configuration configuration);
}
