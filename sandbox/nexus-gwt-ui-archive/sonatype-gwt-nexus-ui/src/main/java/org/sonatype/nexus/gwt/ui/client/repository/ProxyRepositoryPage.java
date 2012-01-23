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
package org.sonatype.nexus.gwt.ui.client.repository;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * 
 *
 * @author barath
 */
public class ProxyRepositoryPage extends RepositoryPage {
    
    public ProxyRepositoryPage() {
        super("proxy");
    }
    
    protected void addTypeSpecificInputs() {
        addRow(i18n.remoteStorageRemoteStorageUrl(),
               createTextBox("remoteStorage.remoteStorageUrl"));

        addCommonInputs();

        FlexTable authContent = new FlexTable();
        addRow(authContent, i18n.remoteStorageAuthenticationUsername(),
               createTextBox("remoteStorage.authentication.username"));
        addRow(authContent, i18n.remoteStorageAuthenticationPassword(),
               createTextBox("remoteStorage.authentication.password"));
        addRow(authContent, i18n.remoteStorageAuthenticationPrivateKey(),
               createTextBox("remoteStorage.authentication.privateKey"));
        addRow(authContent, i18n.remoteStorageAuthenticationPassphrase(),
               createTextBox("remoteStorage.authentication.passphrase"));
        addRow(authContent, i18n.remoteStorageAuthenticationNtlmHost(),
               createTextBox("remoteStorage.authentication.ntlmHost"));
        addRow(authContent, i18n.remoteStorageAuthenticationNtlmDomain(),
               createTextBox("remoteStorage.authentication.ntlmDomain"));
        DisclosurePanel auth = new DisclosurePanel(i18n.remoteStorageAuthenticationSettings());
        auth.add(authContent);
        addRow(auth);

        FlexTable connectionContent = new FlexTable();
        addRow(connectionContent, i18n.remoteStorageConnectionSettingsUserAgentString(),
               createTextBox("remoteStorage.connectionSettings.userAgentString"));
        addRow(connectionContent, i18n.remoteStorageConnectionSettingsQueryString(),
               createTextBox("remoteStorage.connectionSettings.queryString"));
        addRow(connectionContent, i18n.remoteStorageConnectionSettingsConnectionTimeout(),
               createTextBox("remoteStorage.connectionSettings.connectionTimeout"));
        addRow(connectionContent, i18n.remoteStorageConnectionSettingsRetrievalRetryCount(),
               createTextBox("remoteStorage.connectionSettings.retrievalRetryCount"));
        DisclosurePanel connection = new DisclosurePanel(i18n.remoteStorageConnectionSettings());
        connection.add(connectionContent);
        addRow(connection);

        FlexTable proxyContent = new FlexTable();
        addRow(proxyContent, i18n.remoteStorageHttpProxySettingsProxyHostname(),
               createTextBox("remoteStorage.httpProxySettings.proxyHostname"));
        addRow(proxyContent, i18n.remoteStorageHttpProxySettingsProxyPort(),
               createTextBox("remoteStorage.httpProxySettings.proxyPort"));
        addRow(proxyContent, i18n.remoteStorageAuthenticationUsername(),
               createTextBox("remoteStorage.authentication.username"));
        addRow(proxyContent, i18n.remoteStorageAuthenticationPassword(),
               createTextBox("remoteStorage.authentication.password"));
        addRow(proxyContent, i18n.remoteStorageAuthenticationPrivateKey(),
               createTextBox("remoteStorage.authentication.privateKey"));
        addRow(proxyContent, i18n.remoteStorageAuthenticationPassphrase(),
               createTextBox("remoteStorage.authentication.passphrase"));
        addRow(proxyContent, i18n.remoteStorageAuthenticationNtlmHost(),
               createTextBox("remoteStorage.authentication.ntlmHost"));
        addRow(proxyContent, i18n.remoteStorageAuthenticationNtlmDomain(),
               createTextBox("remoteStorage.authentication.ntlmDomain"));
        DisclosurePanel proxy = new DisclosurePanel(i18n.remoteStorageHttpProxySettings());
        proxy.add(proxyContent);
        addRow(proxy);
    }

}
