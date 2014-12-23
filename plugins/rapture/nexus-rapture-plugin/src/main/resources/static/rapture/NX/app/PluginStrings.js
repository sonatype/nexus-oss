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

  /*
   * Note: Symbols follow the following naming convention:
   * <MODE>_<FEATURE>_<VIEW>_<NAME>_<COMPONENT>
   */

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

    // Admin -> Repository
    ADMIN_REPOSITORY_TITLE: 'Repository>',
    ADMIN_REPOSITORY_SUBTITLE: 'Repository administration>',

    // Admin -> Repository -> Repositories
    ADMIN_REPOSITORIES_TITLE: 'Repositories>',
    ADMIN_REPOSITORIES_SUBTITLE: 'Manage repositories>',
    ADMIN_REPOSITORIES_LIST_NEW_BUTTON: 'New>',
    ADMIN_REPOSITORIES_LIST_NAME_COLUMN: 'Name>',
    ADMIN_REPOSITORIES_LIST_TYPE_COLUMN: 'Type>',
    ADMIN_REPOSITORIES_LIST_FORMAT_COLUMN: 'Format>',
    ADMIN_REPOSITORIES_LIST_STATUS_COLUMN: 'Status>',
    ADMIN_REPOSITORIES_LIST_URL_COLUMN: 'URL>',
    ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_COLUMN: 'Health Check>',
    ADMIN_REPOSITORIES_DETAILS_DELETE_BUTTON: 'Delete>',
    ADMIN_REPOSITORIES_DETAILS_MORE_BUTTON: 'More…>',
    ADMIN_REPOSITORIES_DETAILS_EXPIRE_ITEM: 'Expire Cache>',
    ADMIN_REPOSITORIES_DETAILS_REBUILD_ITEM: 'Rebuild Metadata>',
    ADMIN_REPOSITORIES_DETAILS_BLOCK_ITEM: 'Block Proxy>',
    ADMIN_REPOSITORIES_DETAILS_ALLOW_ITEM: 'Allow Proxy>',
    ADMIN_REPOSITORIES_DETAILS_POOS_ITEM: 'Put Out of Service>',
    ADMIN_REPOSITORIES_DETAILS_PRIS_ITEM: 'Put in Service>',
    ADMIN_REPOSITORIES_DETAILS_REPAIR_ITEM: 'Repair Index>',
    ADMIN_REPOSITORIES_DETAILS_UPDATE_ITEM: 'Update Index>',
    ADMIN_REPOSITORIES_DETAILS_BROWSE_BUTTON: 'Browse>',
    ADMIN_REPOSITORIES_DETAILS_SETTINGS_TAB: 'Settings>',
    ADMIN_REPOSITORIES_DETAILS_ROUTING_TAB: 'Routing>',
    ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE: 'Artifact Max Age>',
    ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE_HELP: 'How long to cache the artifacts in the repository before rechecking the remote repository.  Release repositories should use -1.>',
    ADMIN_REPOSITORIES_SETTINGS_AUTHENTICATION: 'Authentication>',
    ADMIN_REPOSITORIES_SETTINGS_BLOCKING: 'Auto Blocking Enabled>',
    ADMIN_REPOSITORIES_SETTINGS_BLOCKING_HELP: 'Auto-block outbound connections on the repository if remote peer is detected as unreachable/unresponsive.>',
    ADMIN_REPOSITORIES_SETTINGS_BROWSING: 'Allow file browsing>',
    ADMIN_REPOSITORIES_SETTINGS_BROWSING_HELP: 'Allow users to browse the contents of the repository.>',
    ADMIN_REPOSITORIES_SETTINGS_CERTIFICATE_BUTTON: 'View Certificate>',
    ADMIN_REPOSITORIES_SETTINGS_CHECKSUM: 'Checksum Policy>',
    ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_HELP: 'Repository checksum policy.>',
    ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_PLACEHOLDER: 'select a policy>',
    ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT: 'Deployment Policy>',
    ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_HELP: 'Controls if deployments and/or updates to artifacts are allowed.>',
    ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_PLACEHOLDER: 'select a policy>',
    ADMIN_REPOSTIORIES_SETTINGS_DOWNLOAD: 'Download Remote Indexes>',
    ADMIN_REPOSTIORIES_SETTINGS_DOWNLOAD_HELP: 'Allow downloading of remote repository indexes.>',
    ADMIN_REPOSITORIES_SETTINGS_FILE: 'File Content Validation>',
    ADMIN_REPOSITORIES_SETTINGS_FILE_HELP: 'Enable content validation for remote contents.>',
    ADMIN_REPOSITORIES_SETTINGS_FORMAT: 'Format>',
    ADMIN_REPOSITORIES_SETTINGS_FORMAT_HELP: 'Repository content format.>',
    ADMIN_REPOSITORIES_SETTINGS_HTTP: 'HTTP Request Settings>',
    ADMIN_REPOSITORIES_SETTINGS_ID: 'ID>',
    ADMIN_REPOSITORIES_SETTINGS_ID_HELP: 'Unique identifier for the repository.>',
    ADMIN_REPOSITORIES_SETTINGS_ID_PLACEHOLDER: 'enter a repository id>',
    ADMIN_REPOSITORIES_SETTINGS_ID_ERROR: 'Only letters, digits, underscores(_), hyphens(-), and dots(.) are allowed in ID>',
    ADMIN_REPOSITORIES_SETTINGS_ITEM_AGE: 'Item Max Age>',
    ADMIN_REPOSITORIES_SETTINGS_ITEM_AGE_HELP: 'How long to cache non-artifact and metadata items in the repository before rechecking the remote repository.>',
    ADMIN_REPOSITORIES_SETTINGS_LOCAL: 'Default Local Storage Location>',
    ADMIN_REPOSITORIES_SETTINGS_LOCAL_HELP: 'Default location to store repository contents.>',
    ADMIN_REPOSITORIES_SETTINGS_METADATA_AGE: 'Metadata Max Age>',
    ADMIN_REPOSITORIES_SETTINGS_METADATA_AGE_HELP: 'How long to cache the metadata in the repository before rechecking the remote repository.>',
    ADMIN_REPOSITORIES_SETTINGS_MEMBERS: 'Member Repositories>',
    ADMIN_REPOSITORIES_SETTINGS_MEMBERS_HELP: 'Select the repositories that are member of the group.>',
    ADMIN_REPOSITORIES_SETTINGS_MEMBERS_FROM: 'Available Repositories>',
    ADMIN_REPOSITORIES_SETTINGS_MEMBERS_TO: 'Ordered Member Repositories>',
    ADMIN_REPOSITORIES_SETTINGS_NAME: 'Name>',
    ADMIN_REPOSITORIES_SETTINGS_NAME_HELP: 'Repository display name>',
    ADMIN_REPOSITORIES_SETTINGS_NAME_PLACEHOLDER: 'enter a repository name>',
    ADMIN_REPOSITORIES_SETTINGS_OVERRIDE: 'Override Local Storage Location>',
    ADMIN_REPOSITORIES_SETTINGS_OVERRIDE_HELP: 'Custom location to store repository contents. Leave blank to use the default.>',
    ADMIN_REPOSITORIES_SETTINGS_OVERRIDE_PLACEHOLDER: 'enter an override url>',
    ADMIN_REPOSITORIES_SETTINGS_POLICY: 'Repository Policy>',
    ADMIN_REPOSITORIES_SETTINGS_POLICY_HELP: 'Maven repositories can store either release or snapshot artifacts.>',
    ADMIN_REPOSITORIES_SETTINGS_POLICY_PLACEHOLDER: 'select a policy>',
    ADMIN_REPOSITORIES_SETTINGS_PROVIDER: 'Provider>',
    ADMIN_REPOSITORIES_SETTINGS_PROVIDER_HELP: 'Repository content provider.>',
    ADMIN_REPOSITORIES_SETTINGS_PUBLISH: 'Publish URL>',
    ADMIN_REPOSITORIES_SETTINGS_PUBLISH_HELP: 'Expose the URL of the repository to users.>',
    ADMIN_REPOSITORIES_SETTINGS_REMOTE: 'Remote Storage Location>',
    ADMIN_REPOSITORIES_SETTINGS_REMOTE_HELP: 'Location of the remote repository being proxied.>',
    ADMIN_REPOSITORIES_SETTINGS_REMOTE_PLACEHOLDER: 'enter an URL>',
    ADMIN_REPOSITORIES_SETTINGS_SEARCH: 'Include in Search>',
    ADMIN_REPOSITORIES_SETTINGS_SEARCH_HELP: 'Include repository in search results.>',
    ADMIN_REPOSITORIES_SETTINGS_SOURCE: 'Source repository>',
    ADMIN_REPOSITORIES_SETTINGS_SOURCE_HELP: 'Physical repository being presented as a logical view by the repository.>',
    ADMIN_REPOSITORIES_SETTINGS_SOURCE_PLACEHOLDER: 'select a repository>',
    ADMIN_REPOSITORIES_SETTINGS_SYNCHRONIZE: 'Synchronize on Startup>',
    ADMIN_REPOSITORIES_SETTINGS_SYNCHRONIZE_HELP: 'Rebuild virtual links when the server starts.>',
    ADMIN_REPOSITORIES_SETTINGS_TRUST_STORE: 'Use Nexus TrustStore>',
    ADMIN_REPOSITORIES_SETTINGS_TTL: 'Not Found Cache TTL>',
    ADMIN_REPOSITORIES_SETTINGS_TTL_HELP: 'How long to cache the fact that a file was not found in the repository.>',
    ADMIN_REPOSITORIES_ROUTING_PUBLISH_SECTION: 'Publish Status>',
    ADMIN_REPOSITORIES_ROUTING_DISCOVERY_SECTION: 'Discovery Status>',
    ADMIN_REPOSITORIES_ROUTING_DISCOVERY: 'Enable discovery>',
    ADMIN_REPOSITORIES_ROUTING_DISCOVERY_HELP: 'Enable routing discovery.>',
    ADMIN_REPOSITORIES_ROUTING_PUBLISHED: 'Published On>',
    ADMIN_REPOSITORIES_ROUTING_PUBLISHED_HELP: 'Time when routing data was published.>',
    ADMIN_REPOSITORIES_ROUTING_RUN: 'Last Run',
    ADMIN_REPOSITORIES_ROUTING_RUN_HELP: 'Time when discovery process was last run.',
    ADMIN_REPOSITORIES_ROUTING_UPDATE: 'Update interval>',
    ADMIN_REPOSITORIES_ROUTING_UPDATE_HELP: 'Interval between routing discovery runs.>',
    ADMIN_REPOSITORIES_ROUTING_UPDATE_BUTTON: 'Update now>',
    ADMIN_REPOSITORIES_ROUTING_UPDATE_PLACEHOLDER: 'Select…>',
    ADMIN_REPOSITORIES_ROUTING_1_HOUR_ITEM: '1 hr>',
    ADMIN_REPOSITORIES_ROUTING_2_HOUR_ITEM: '2 hr>',
    ADMIN_REPOSITORIES_ROUTING_3_HOUR_ITEM: '3 hr>',
    ADMIN_REPOSITORIES_ROUTING_6_HOUR_ITEM: '6 hr>',
    ADMIN_REPOSITORIES_ROUTING_9_HOUR_ITEM: '9 hr>',
    ADMIN_REPOSITORIES_ROUTING_12_HOUR_ITEM: '2 hr>',
    ADMIN_REPOSITORIES_ROUTING_DAILY_ITEM: 'Daily>',
    ADMIN_REPOSITORIES_ROUTING_WEEKLY_ITEM: 'Weekly>',
    ADMIN_REPOSITORIES_ROUTING_PREFIX_LINK: 'View prefix file>',

    // Admin -> Repository -> Routing
    ADMIN_ROUTING_TITLE: 'Routing>',
    ADMIN_ROUTING_SUBTITLE: 'Manage repository routes>',
    ADMIN_ROUTING_LIST_NEW_BUTTON: 'New>',
    ADMIN_ROUTING_LIST_ROUTE_COLUMN: 'Route>',
    ADMIN_ROUTING_LIST_RULE_COLUMN: 'Rule Type>',
    ADMIN_ROUTING_LIST_GROUP_COLUMN: 'Group>',
    ADMIN_ROUTING_LIST_REPOSITORIES_COLUMN: 'Repositories>',
    ADMIN_ROUTING_DETAILS_DELETE_BUTTON: 'Delete>',
    ADMIN_ROUTING_DETAILS_SETTINGS_TAB: 'Settings>',
    ADMIN_ROUTING_SETTINGS_URL: 'URL pattern>',
    ADMIN_ROUTING_SETTINGS_URL_HELP: 'A regular expression used to match the artifact path. The path is everything after /nexus/content/ so it will include the group or repository name. .* is used to specify all paths. \'.*/com/some/company/.*\' will match any artifact with \'com.some.company\' as the group id or artifact id.>',
    ADMIN_ROUTING_SETTINGS_URL_PLACEHOLDER: 'enter a pattern>',
    ADMIN_ROUTING_SETTINGS_RULE: 'Rule Type>',
    ADMIN_ROUTING_SETTINGS_RULE_HELP: 'There are three types of rules: Inclusive (if the pattern matches, only use the repositories listed below), Exclusive (exclude the repositories listed below) and Blocking (block URLs matching the pattern).>',
    ADMIN_ROUTING_SETTINGS_RULE_PLACEHOLDER: 'select a type>',
    ADMIN_ROUTING_SETTINGS_BLOCKING_ITEM: 'Blocking>',
    ADMIN_ROUTING_SETTINGS_INCLUSIVE_ITEM: 'Inclusive>',
    ADMIN_ROUTING_SETTINGS_EXCLUSIVE_ITEM: 'Exclusive>',
    ADMIN_ROUTING_SETTINGS_GROUP: 'Repository Group>',
    ADMIN_ROUTING_SETTINGS_GROUP_HELP: 'A repository group this route applies to.>',
    ADMIN_ROUTING_SETTINGS_GROUP_PLACEHOLDER: 'select a group>',
    ADMIN_ROUTING_SETTINGS_REPOSITORIES: 'Repositories>',
    ADMIN_ROUTING_SETTINGS_REPOSITORIES_HELP: 'Ordered list of repositories which will be searched to locate a particular artifact.>',
    ADMIN_ROUTING_SETTINGS_AVAILABLE: 'Available Repositories>',
    ADMIN_ROUTING_SETTINGS_ORDERED: 'Ordered Route Repositories>',

    // Admin -> Repository -> Targets
    ADMIN_TARGETS_TITLE: 'Targets>',
    ADMIN_TARGETS_SUBTITLE: 'Manage repository targets>',
    ADMIN_TARGETS_LIST_NEW_BUTTON: 'New>',
    ADMIN_TARGETS_LIST_NAME_COLUMN: 'Name>',
    ADMIN_TARGETS_LIST_REPOSITORY_COLUMN: 'Repository Type>',
    ADMIN_TARGETS_LIST_PATTERNS_COLUMN: 'Patterns>',
    ADMIN_TARGETS_LIST_EMPTY_STATE: 'No repository targets defined>',
    ADMIN_TARGETS_DETAILS_DELETE_BUTTON: 'Delete>',
    ADMIN_TARGETS_DETAILS_SETTINGS_TAB: 'Settings>',
    ADMIN_TARGETS_SETTINGS_NAME: 'Name>',
    ADMIN_TARGETS_SETTINGS_NAME_HELP: 'The name of the repository target.>',
    ADMIN_TARGETS_SETTINGS_NAME_PLACEHOLDER: 'enter a target name>',
    ADMIN_TARGETS_SETTINGS_TYPE: 'Repository Type>',
    ADMIN_TARGETS_SETTINGS_TYPE_HELP: 'The content class of the repository target. It will be matched only against repositories with the same content class.>',
    ADMIN_TARGETS_SETTINGS_TYPE_PLACEHOLDER: 'select a repository type>',
    ADMIN_TARGETS_SETTINGS_PATTERNS: 'Patterns>',
    ADMIN_TARGETS_SETTINGS_PATTERNS_HELP: 'Enter a pattern expression and click "Add" to add it to the list. Regular expressions are used to match the artifact path. ".*" is used to specify all paths. ".*/com/some/company/.*" will match any artifact with "com.some.company" as the group id or artifact id. "^/com/some/company/.*" will match any artifact starting with com/some/company.>',
    ADMIN_TARGETS_SETTINGS_PATTERNS_PLACEHOLDER: 'enter a pattern expression>',

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

    // Admin -> Security -> LDAP
    ADMIN_LDAP_SUBTITLE: 'LDAP>',
    ADMIN_LDAP_SUBTITLE: 'Manage LDAP servers configuration>',

    // Admin -> Security -> Realms
    ADMIN_REALMS_TITLE: 'Realms>',
    ADMIN_REALMS_SUBTITLE: 'Manage security realm configuration>',

    // Admin -> Security -> SSL Certificates
    ADMIN_SSL_TITLE: 'SSL Certificates>',
    ADMIN_SSL_SUBTITLE: 'Manage trusted SSL certificates for use with the Nexus Trust Store>',

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

    // Admin -> System -> Plugins
    ADMIN_PLUGINS_TITLE: 'Plugins>',
    ADMIN_PLUGINS_SUBTITLE: 'View installed plugins>',

    // Admin -> System -> Tasks
    ADMIN_TASKS_TITLE: 'Tasks>',
    ADMIN_TASKS_SUBTITLE: 'Manage scheduled tasks>',

    // User -> Account
    USER_ACCOUNT_TITLE: 'Account>',
    USER_ACCOUNT_SUBTITLE: 'Manage your account>'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

