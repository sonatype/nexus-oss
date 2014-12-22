/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext, NX*/

/**
 * Application strings
 *
 * @since 3.0
 */
Ext.define('NX.app.PluginStrings', {
  singleton: true,
  requires: [
    'NX.I18n'
  ],

  keys: {

    /*
     * Global strings
     */

    // Header
    GLOBAL_HEADER_TITLE: 'Sonatype Nexus>',
    GLOBAL_HEADER_BROWSE_TOOLTIP: 'Browse Server Contents>',
    GLOBAL_HEADER_ADMIN_TOOLTIP: 'Server administration and configuration>',
    GLOBAL_HEADER_SEARCH_PLACEHOLDER: 'Search…>',
    GLOBAL_HEADER_REFRESH_TOOLTIP: 'Refresh current view and data>',
    GLOBAL_HEADER_USER_TOOLTIP: 'User profile and options>',
    GLOBAL_HEADER_SIGN_IN: 'Sign In>',
    GLOBAL_HEADER_SIGN_IN_TOOLTIP: 'Have an account?>',
    GLOBAL_HEADER_SIGN_OUT_TOOLTIP: 'Sign out>',
    GLOBAL_HEADER_HELP_TOOLTIP: 'Help>',
    GLOBAL_HEADER_HELP_FEATURE: 'Help for: >',
    GLOBAL_HEADER_HELP_ABOUT: 'About>',
    GLOBAL_HEADER_HELP_DOCUMENTATION: 'Documentation>',
    GLOBAL_HEADER_HELP_KB: 'Knowledge Base>',
    GLOBAL_HEADER_HELP_COMMUNITY: 'Community>',
    GLOBAL_HEADER_HELP_ISSUES: 'Issue Tracker>',
    GLOBAL_HEADER_HELP_SUPPORT: 'Support>',

    // Footer
    GLOBAL_FOOTER_COPYRIGHT: 'Sonatype Nexus™ © Copyright Sonatype, Inc.>',

    // Sign in
    GLOBAL_SIGN_IN_TITLE: 'Sign In>',
    GLOBAL_SIGN_IN_USERNAME: 'Username>',
    GLOBAL_SIGN_IN_USERNAME_PLACEHOLDER: 'enter your username>',
    GLOBAL_SIGN_IN_PASSWORD: 'Password>',
    GLOBAL_SIGN_IN_PASSWORD_PLACEHOLDER: 'enter your password>',
    GLOBAL_SIGN_IN_REMEMBER_ME: 'Remember me>',
    GLOBAL_SIGN_IN_SUBMIT: 'Sign In>',
    GLOBAL_SIGN_IN_CANCEL: 'Cancel>',

    // Filter box
    GLOBAL_FILTER_PLACEHOLDER: 'filter>',

    // Pagination
    //GLOBAL_PAGINATION_CONTROL: 'Page [X] of [Y]',
    //GLOBAL_PAGINATION_DISPLAYED: 'Displaying [X] of [Y]',

    /*
     * Features
     */

    // Browse -> Welcome
    BROWSE_WELCOME_TITLE: 'Welcome>',
    BROWSE_WELCOME_SUBTITLE: 'Welcome to Sonatype Nexus!>',

    // Browse -> Search
    BROWSE_SEARCH_TITLE: 'Search>',
    BROWSE_SEARCH_SUBTITLE: 'Search for components>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_KEYWORD: 'Keyword>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_ARTIFACT_ID: 'Artifact ID>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_CLASSIFIER: 'Classifier>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_CLASS_NAME: 'Class name>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_FORMAT: 'Format>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_GROUP_ID: 'Group ID>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_PACKAGING: 'Packaging>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_SHA_1: 'SHA-1>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_VERSION: 'Version>',
    BROWSE_SEARCH_COMPONENTS_CRITERIA_FILTER_PLACEHOLDER: 'any>',
    BROWSE_SEARCH_COMPONENTS_MORE_BUTTON: 'More Criteria>',
    BROWSE_SEARCH_COMPONENTS_COMPONENT_COLUMN: 'Component>',
    BROWSE_SEARCH_COMPONENTS_EMPTY_STATE: 'No results>',
    BROWSE_SEARCH_VERSIONS_GROUP: 'Group>',
    BROWSE_SEARCH_VERSIONS_NAME: 'Name>',
    BROWSE_SEARCH_VERSIONS_FORMAT: 'Format>',
    BROWSE_SEARCH_VERSIONS_POPULAR: 'Most popular version>',
    BROWSE_SEARCH_VERSIONS_VERSION_COLUMN: 'Version>',
    BROWSE_SEARCH_VERSIONS_FILE_COLUMN: 'File>',
    BROWSE_SEARCH_VERSIONS_REPOSITORY_COLUMN: 'Repository>',
    BROWSE_SEARCH_VERSIONS_AGE_COLUMN: 'Age>',
    BROWSE_SEARCH_VERSIONS_POPULARITY_COLUMN: 'Popularity>',
    BROWSE_SEARCH_VERSIONS_SECURITY_COLUMN: 'Security>',
    BROWSE_SEARCH_VERSIONS_LICENSE_COLUMN: 'License>',
    BROWSE_SEARCH_DETAILS_INFO_TAB: 'Info>',
    BROWSE_SEARCH_DETAILS_MAVEN_TAB: 'Maven>',
    BROWSE_SEARCH_MAVEN_GROUP: 'Group>',
    BROWSE_SEARCH_MAVEN_ARTIFACT: 'Artifact>',
    BROWSE_SEARCH_MAVEN_VERSION: 'Version>',
    BROWSE_SEARCH_MAVEN_CLASSIFIER: 'Classifier>',
    BROWSE_SEARCH_MAVEN_EXTENSION: 'Extension>',
    BROWSE_SEARCH_MAVEN_XML: 'XML>',
    BROWSE_SEARCH_COMPONENT_INFO_LICENSE_SECTION: 'License Analysis>',
    BROWSE_SEARCH_COMPONENT_INFO_THREAT_LEVEL_COLUMN: 'Threat Level>',
    BROWSE_SEARCH_COMPONENT_INFO_DECLARED_LICENSES_COLUMN: 'Declared License(s)>',
    BROWSE_SEARCH_COMPONENT_INFO_OBSERVED_LICENSES_COLUMN: 'Observed License(s) in Source>',
    BROWSE_SEARCH_COMPONENT_INFO_SECURITY_SECTION: 'Security Issues>',
    BROWSE_SEARCH_COMPONENT_INFO_THREAT_LEVEL_COLUMN: 'Threat Level>',
    BROWSE_SEARCH_COMPONENT_INFO_PROBLEM_CODE_COLUMN: 'Problem Code>',
    BROWSE_SEARCH_COMPONENT_INFO_SUMMARY_COLUMN: 'Summary>',

    // Browse -> Search -> Class Name
    BROWSE_SEARCH_CLASS_TITLE: 'Class Name>',
    BROWSE_SEARCH_CLASS_SUBTITLE: 'Search for components by class-name>',

    // Browse -> Search -> Custom
    BROWSE_SEARCH_CUSTOM_TITLE: 'Custom>',
    BROWSE_SEARCH_CUSTOM_SUBTITLE: 'Search for components by custom criteria>',

    // Browse -> Search -> Maven
    BROWSE_SEARCH_MAVEN_TITLE: 'Maven>',
    BROWSE_SEARCH_MAVEN_SUBTITLE: 'Search for components by Maven coordinates>',

    // Browse -> Search -> SHA-1
    BROWSE_SEARCH_SHA_1_TITLE: 'SHA-1>',
    BROWSE_SEARCH_SHA_1_SUBTITLE: 'Search for components by SHA-1>',

    // Browse -> Feeds
    BROWSE_FEEDS_TITLE: 'Feeds>',
    BROWSE_FEEDS_SUBTITLE: 'System event feeds>',
    BROWSE_FEEDS_LIST_SUBSCRIBE_BUTTON: 'Subscribe>',
    BROWSE_FEEDS_LIST_FEED_COLUMN: 'Feed>',
    BROWSE_FEEDS_LIST_URL_COLUMN: 'URL>',
    BROWSE_FEEDS_DETAILS_EVENTS_TAB: 'Events>',
    BROWSE_FEEDS_DETAILS_TITLE_COLUMN: 'Title>',
    BROWSE_FEEDS_DETAILS_DATE_COLUMN: 'Date>',

    // Browse -> Repository
    BROWSE_REPOSITORY_TITLE: 'Repository>',
    BROWSE_REPOSITORY_SUBTITLE: 'Browse repositories>',
    BROWSE_REPOSITORY_MORE_BUTTON: 'More…>',

    // Browse -> Repository -> Standard
    BROWSE_STANDARD_TITLE: 'Standard>',
    BROWSE_STANDARD_SUBTITLE: 'Browse standard repositories>',
    BROWSE_STANDARD_LIST_FILE_COLUMN: 'Storage File>',
    BROWSE_STANDARD_LIST_STORAGE_COLUMN: 'Storage>',
    BROWSE_STANDARD_LIST_INDEX_COLUMN: 'Index>',
    BROWSE_STANDARD_INFO_PATH: 'Path>',
    BROWSE_STANDARD_INFO_SIZE: 'Size>',
    BROWSE_STANDARD_INFO_UPLOADED_BY: 'Uploaded by',
    BROWSE_STANDARD_INFO_UPLOADED_DATE: 'Uploaded Date>',
    BROWSE_STANDARD_INFO_MODIFIED: 'Last Modified>',
    BROWSE_STANDARD_INFO_SHA1: 'SHA1>',
    BROWSE_STANDARD_INFO_MD5: 'MD5>',
    BROWSE_STANDARD_INFO_CONTAINED: 'Contained in>',
    //BROWSE_STANDARD_COMPONENT_INFO_EMPTY_STATE: 'CLM data is not available for this artifact. Unable to properly identify the groupId, artifactId or version.>',

    // Browse -> Repository -> Managed
    BROWSE_MANAGED_TITLE: 'Managed>',
    BROWSE_MANAGED_SUBTITLE: 'Browse managed repositories>',

    // Browse -> Upload
    BROWSE_UPLOAD_TITLE: 'Upload>',
    BROWSE_UPLOAD_SUBTITLE: 'Upload content to Nexus>',

    // Browse -> Upload -> Maven
    BROWSE_MAVEN_TITLE: 'Maven>',
    BROWSE_MAVEN_SUBTITLE: 'Upload artifacts to Maven Hosted Repositories>',
    BROWSE_MAVEN_REPOSITORY: 'Repository>',
    BROWSE_MAVEN_REPOSITORY_HELP: 'Select the repository where artifacts will be uploaded.>',
    BROWSE_MAVEN_REPOSITORY_PLACEHOLDER: 'select repository>',
    BROWSE_MAVEN_ARTIFACT_HELP: 'Select the artifacts you wish to upload>',
    BROWSE_MAVEN_ARTIFACT_BUTTON: 'Add an artifact>',
    BROWSE_MAVEN_ARTIFACTS_BUTTON: 'Add another artifact>',
    BROWSE_MAVEN_ARTIFACT_UPLOAD_BUTTON: 'Upload>',
    BROWSE_MAVEN_ARTIFACT_DISCARD_BUTTON: 'Discard>',

    // Browse -> Upload -> Staging
    BROWSE_STAGING_TITLE: 'Staging>',
    BROWSE_STAGING_SUBTITLE: 'Upload artifacts via Staging>',
    BROWSE_STAGING_PROFILE: 'Staging profile>',
    BROWSE_STAGING_PROFILE_HELP: 'Select a staging profile to be used or leave it empty in order to be automatically selected.>',
    BROWSE_STAGING_PROFILE_PLACEHOLDER: 'select a profile>',
    BROWSE_STAGING_DESCRIPTION: 'Description>',
    BROWSE_STAGING_DESCRIPTION_HELP: 'A detailed description of the staged repository>',

    // Browse -> Upload -> Staging Bundle
    BROWSE_STAGING_BUNDLE_TITLE: 'Staging Bundle>',
    BROWSE_STAGING_BUNDLE_SUBTITLE: 'Upload bundle via Staging>',

    // Admin -> Repository
    ADMIN_REPOSITORY_TITLE: 'Repository>',
    ADMIN_REPOSITORY_SUBTITLE: 'Repository administration>',

    // Admin -> Repository -> Repositories
    ADMIN_REPOSITORIES_TITLE: 'Repositories>',
    ADMIN_REPOSITORIES_SUBTITLE: 'Manage repositories>',

    // Admin -> Repository -> Routing
    ADMIN_ROUTING_TITLE: 'Routing>',
    ADMIN_ROUTING_SUBTITLE: 'Manage repository routes>',

    // Admin -> Repository -> Targets
    ADMIN_TARGETS_TITLE: 'Targets>',
    ADMIN_TARGETS_SUBTITLE: 'Manage repository targets>',

    // Admin -> Staging
    ADMIN_STAGING_TITLE: 'Staging>',
    ADMIN_STAGING_SUBTITLE: 'Staging administration>',

    // Admin -> Staging -> Profiles
    ADMIN_PROFILES_TITLE: 'Profiles>',
    ADMIN_PROFILES_SUBTITLE: 'Manage staging profiles>',

    // Admin -> Staging -> Rules
    ADMIN_RULES_TITLE: 'Rules>',
    ADMIN_RULES_SUBTITLE: 'Manage staging rules>',

    // Admin -> Security
    ADMIN_SECURITY_TITLE: 'Security>',
    ADMIN_SECURITY_SUBTITLE: 'Security administration>',

    // Admin -> Security -> Privileges
    ADMIN_PRIVILEGES_TITLE: 'Privileges>',
    ADMIN_PRIVILEGES_SUBTITLE: 'Manage privileges>',

    // Admin -> Security -> Roles
    ADMIN_ROLES_TITLE: 'Roles>',
    ADMIN_ROLES_SUBTITLE: 'Manage roles>',

    // Admin -> Security -> Users
    ADMIN_USERS_TITLE: 'Users>',
    ADMIN_USERS_SUBTITLE: 'Manage users>',

    // Admin -> Security -> Anonymous
    ADMIN_ANONYMOUS_TITLE: 'Anonymous>',
    ADMIN_ANONYMOUS_SUBTITLE: 'Manage anonymous user configuration>',

    // Admin -> Security -> Atlassian Crowd
    ADMIN_CROWD_TITLE: 'Atlassian Crowd>',
    ADMIN_CROWD_SUBTITLE: 'Manage Atlassian Crowd configuration>',

    // Admin -> Security -> LDAP
    ADMIN_LDAP_SUBTITLE: 'LDAP>',
    ADMIN_LDAP_SUBTITLE: 'Manage LDAP servers configuration>',

    // Admin -> Security -> PGP
    ADMIN_PGP_TITLE: 'PGP>',
    ADMIN_PGP_SUBTITLE: 'Manage PGP key servers used to look up public PGP keys>',

    // Admin -> Security -> Realms
    ADMIN_REALMS_TITLE: 'Realms>',
    ADMIN_REALMS_SUBTITLE: 'Manage security realm configuration>',

    // Admin -> Security -> SSL Certificates
    ADMIN_SSL_TITLE: 'SSL Certificates>',
    ADMIN_SSL_SUBTITLE: 'Manage trusted SSL certificates for use with the Nexus Trust Store>',

    // Admin -> Security -> User Token
    ADMIN_TOKEN_TITLE: 'User Token>',
    ADMIN_TOKEN_SUBTITLE: 'Manage user-token configuration>',

    // Admin -> Support
    ADMIN_SUPPORT_TITLE: 'Support>',
    ADMIN_SUPPORT_SUBTITLE: 'Support tools>',

    // Admin -> Support -> Analytics
    ADMIN_ANALYTICS_TITLE: 'Analytics>',
    ADMIN_ANALYTICS_SUBTITLE: 'Manage analytics configuration>',

    // Admin -> Support -> Logging
    ADMIN_LOGGING_TITLE: 'Logging>',
    ADMIN_LOGGING_SUBTITLE: 'Control logging verbosity levels>',

    // Admin -> Support -> Logging -> Log Viewer
    ADMIN_LOG_VIEWER_TITLE: 'Log Viewer>',
    ADMIN_LOG_VIEWER_SUBTITLE: 'View the current log contents>',

    // Admin -> Support -> Metrics
    ADMIN_METRICS_TITLE: 'Metrics>',
    ADMIN_METRICS_SUBTITLE: 'Provides server metrics>',

    // Admin -> Support -> Support Request
    ADMIN_SUPPORT_REQUEST_TITLE: 'Support Request>',
    ADMIN_SUPPORT_REQUEST_SUBTITLE: 'Submit a support request>',

    // Admin -> Support -> Support ZIP
    ADMIN_SUPPORT_ZIP_TITLE: 'Support ZIP>',
    ADMIN_SUPPORT_ZIP_SUBTITLE: 'Creates a ZIP file containing useful support information about your server>',

    // Admin -> Support -> System Information
    ADMIN_SYSTEM_INFORMATION_TITLE: 'System Information>',
    ADMIN_SYSTEM_INFORMATION_SUBTITLE: 'Shows system information>',

    // Admin -> System
    ADMIN_SYSTEM_TITLE: 'System>',
    ADMIN_SYSTEM_SUBTITLE: 'System administration>',

    // Admin -> System -> Capabilities
    ADMIN_CAPABILITIES_TITLE: 'Capabilities>',
    ADMIN_CAPABILITIES_SUBTITLE: 'Manage capabilities>',

    // Admin -> System -> Email Server
    ADMIN_SMTP_TITLE: 'Email Server>',
    ADMIN_SMTP_SUBTITLE: 'Manage email server SMTP configuration>',

    // Admin -> System -> General
    ADMIN_GENERAL_TITLE: 'General>',
    ADMIN_GENERAL_SUBTITLE: 'Manage general configuration>',

    // Admin -> System -> HTTP
    ADMIN_HTTP_TITLE: 'HTTP>',
    ADMIN_HTTP_SUBTITLE: 'Manage outbound HTTP/HTTPS configuration>',

    // Admin -> System -> Licensing
    ADMIN_LICENSING_TITLE: 'Licensing>',
    ADMIN_LICENSING_SUBTITLE: 'Manage Nexus product license>',

    // Admin -> System -> Licensing -> License Users
    ADMIN_LICENSE_USERS_TITLE: 'License Users>',
    ADMIN_LICENSE_USERS_SUBTITLE: 'Reports active license users in the last 7 days>',

    // Admin -> System -> Plugins
    ADMIN_PLUGINS_TITLE: 'Plugins>',
    ADMIN_PLUGINS_SUBTITLE: 'View installed plugins>',

    // Admin -> System -> Smart Proxy
    ADMIN_SMART_PROXY_TITLE: 'Smart Proxy>',
    ADMIN_SMART_PROXY_SUBTITLE: 'Manage Smart Proxy server configuration>',

    // Admin -> System -> Smart Proxy -> Trusted Keys
    ADMIN_TRUSTED_KEYS_TITLE: 'Trusted Keys>',
    ADMIN_TRUSTED_KEYS_SUBTITLE: 'Manage Smart Proxy trusted server key certificates>',

    // Admin -> System -> Tasks
    ADMIN_TASKS_TITLE: 'Tasks>',
    ADMIN_TASKS_SUBTITLE: 'Manage scheduled tasks>',

    // Admin -> System -> Templates
    ADMIN_TEMPLATES_PROXY_TITLE: 'Templates>',
    ADMIN_TEMPLATES_PROXY_SUBTITLE: 'Manage templates (Maven settings, etc)>',

    // User -> Account
    USER_ACCOUNT_TITLE: 'Account>',
    USER_ACCOUNT_SUBTITLE: 'Manage your account>',

    // User -> User Token
    USER_TOKEN_TITLE: 'User Token>',
    USER_TOKEN_SUBTITLE: 'Access Nexus without the use of passwords>'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

