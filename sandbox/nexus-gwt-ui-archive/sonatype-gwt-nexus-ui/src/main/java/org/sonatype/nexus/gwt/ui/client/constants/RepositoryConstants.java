/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
