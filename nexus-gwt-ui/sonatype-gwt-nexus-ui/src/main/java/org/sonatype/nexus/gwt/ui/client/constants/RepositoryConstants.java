package org.sonatype.nexus.gwt.ui.client.constants;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface RepositoryConstants extends ConstantsWithLookup {

    String headerName();
    String headerRepoType();
    String headerRepoPolicy();
    String headerStatusProxyMode();
    String headerStatusLocalStatus();
    String headerStatusRemoteStatus();

    String id();
    String name();
    String remoteStorageRemoteStorageUrl();
    String allowWrite();
    String browseable();
    String indexable();
    String advancedSettings();
    String defaultLocalStorageUrl();
    String overrideLocalStorageUrl();
    String notFoundCacheTTLBefore();
    String notFoundCacheTTLAfter();
    String artifactMaxAgeBefore();
    String artifactMaxAgeAfter();
    String metadataMaxAgeBefore();
    String metadataMaxAgeAfter();
    String remoteStorageAuthenticationSettings();
    String remoteStorageAuthenticationUsername();
    String remoteStorageAuthenticationPassword();
    String remoteStorageAuthenticationPrivateKey();
    String remoteStorageAuthenticationPassphrase();
    String remoteStorageAuthenticationNtlmHost();
    String remoteStorageAuthenticationNtlmDomain();
    String remoteStorageConnectionSettings();
    String remoteStorageConnectionSettingsUserAgentString();
    String remoteStorageConnectionSettingsQueryString();
    String remoteStorageConnectionSettingsConnectionTimeout();
    String remoteStorageConnectionSettingsRetrievalRetryCount();
    String remoteStorageHttpProxySettings();
    String remoteStorageHttpProxySettingsProxyHostname();
    String remoteStorageHttpProxySettingsProxyPort();
    String shadowOf();
    String format();
    String formatMaven1();
    String formatMaven2();
    String syncAtStartup();

    String repoTypeHosted();
    String repoTypeProxy();
    String repoTypeVirtual();

    String repoPolicyRelease();
    String repoPolicySnapshot();

    String save();
    String cancel();
    
}
