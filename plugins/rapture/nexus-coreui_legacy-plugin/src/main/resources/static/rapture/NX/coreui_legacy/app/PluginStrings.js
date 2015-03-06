/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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
 * CoreUi plugin strings.
 *
 * @since 3.0
 */
Ext.define('NX.coreui_legacy.app.PluginStrings', {
  '@aggregate_priority': 90,

  singleton: true,
  requires: [
    'NX.I18n'
  ],

  /*
   * Note: Symbols follow the following naming convention:
   * <MODE>_<FEATURE>_<VIEW>_<NAME>_<COMPONENT>
   */

  keys: {
    // Admin -> Repository -> Repositories
    LEGACY_ADMIN_REPOSITORIES_TITLE: 'Repositories (legacy)',
    LEGACY_ADMIN_REPOSITORIES_SUBTITLE: 'Manage repositories',
    LEGACY_ADMIN_REPOSITORIES_UPDATE_ERROR: 'You do not have permission to update repositories',
    LEGACY_ADMIN_REPOSITORIES_UPDATE_SUCCESS: 'Repository updated: ',
    LEGACY_ADMIN_REPOSITORIES_SELECT_TITLE: 'Select Provider',
    LEGACY_ADMIN_REPOSITORIES_SELECT_PROVIDER_COLUMN: 'Provider',
    LEGACY_ADMIN_REPOSITORIES_SELECT_TYPE_COLUMN: 'Type',
    LEGACY_ADMIN_REPOSITORIES_CREATE_ERROR: 'You do not have permission to create repositories',
    LEGACY_ADMIN_REPOSITORIES_CREATE_TITLE: 'Create {0} Repository',
    LEGACY_ADMIN_REPOSITORIES_CREATE_GROUP_SUCCESS: 'Group repository created: ',
    LEGACY_ADMIN_REPOSITORIES_CREATE_HOSTED_SUCCESS: 'Hosted repository created: ',
    LEGACY_ADMIN_REPOSITORIES_CREATE_MAVEN_SUCCESS: 'Maven Hosted repository created: ',
    LEGACY_ADMIN_REPOSITORIES_CREATE_PROXY_SUCCESS: 'Proxy repository created: ',
    LEGACY_ADMIN_REPOSITORIES_CREATE_MAVEN_PROXY_SUCCESS: 'Maven Proxy repository created: ',
    LEGACY_ADMIN_REPOSITORIES_CREATE_VIRTUAL_SUCCESS: 'Shadow repository created: ',
    LEGACY_ADMIN_REPOSITORIES_LIST_NEW_BUTTON: 'Create repository',
    LEGACY_ADMIN_REPOSITORIES_LIST_NAME_COLUMN: 'Name',
    LEGACY_ADMIN_REPOSITORIES_LIST_TYPE_COLUMN: 'Type',
    LEGACY_ADMIN_REPOSITORIES_LIST_FORMAT_COLUMN: 'Format',
    LEGACY_ADMIN_REPOSITORIES_LIST_STATUS_COLUMN: 'Status',
    LEGACY_ADMIN_REPOSITORIES_LIST_URL_COLUMN: 'URL',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_COLUMN: 'Health check',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZING: 'Analyzing…',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZING_TOOLTIP: '<span><h2>The Analysis is Under Way</h2>The contents of your repository are being analyzed.  ' +
    'This process should only take a few minutes.<br><br>When complete, the ANALYZING button will be ' +
    'replaced by a set of icons that indicate how many security and licensing issues were discovered.' +
    '<br><br>Hover your mouse over these icons to see additional information about the issues that were found.</span>',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_VIEW_PERMISSION_ERROR: '<span><h2>Insufficient Permissions to View Summary Report</h2>' +
    'To view healthcheck summary report for a repository your user account must have the necessary permissions.</span>',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE: 'Analyze',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE_TOOLTIP: '<span><h2>Repository Health Check Analysis</h2>Click this button to request a Repository Health Check (RHC) ' +
    'by the Sonatype CLM service.  The process is non-invasive and non-disruptive.  Sonatype CLM ' +
    'will return actionable quality, security, and licensing information about the open source components in the repository.' +
    '<br><br><a href="http://links.sonatype.com/products/clm/rhc/home" ' +
    'target="_blank">How the Sonatype CLM Repository Health Check can help you make better software faster</a></span>',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE_DIALOG: 'Analyze Repository',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE_DIALOG_HELP: 'Do you want to analyze the repository {0} and others for security vulnerabilities and license issues?',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE_DIALOG_OK: 'Yes, all repositories',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE_DIALOG_YES: 'Yes, only this repository',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_ANALYZE_PERMISSION_ERROR: '<span><h2>Insufficient Permissions to Analyze a Repository</h2>' +
    'To analyze a repository your user account must have permissions to start analysis.</span>',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_LOADING: 'Loading…',
    LEGACY_ADMIN_REPOSITORIES_LIST_HEALTH_CHECK_UNAVAILABLE_TOOLTIP: '<span><h2>Repository Health Check Unavailable</h2>A Repository Health Check (RHC) ' +
    'cannot be performed by the Sonatype CLM service on this repository, because it is an unsupported type or out of service.<br><br>' +
    '<a href="http://links.sonatype.com/products/clm/rhc/home" ' +
    'target="_blank">How the Sonatype CLM Repository Health Check can help you make better software faster</a></span>',
    LEGACY_ADMIN_REPOSITORIES_LIST_IN_SERVICE: 'In service',
    LEGACY_ADMIN_REPOSITORIES_LIST_OUT_SERVICE: 'Out of service',
    LEGACY_ADMIN_REPOSITORIES_LIST_AUTO_BLOCK: ' (remote automatically blocked)',
    LEGACY_ADMIN_REPOSITORIES_LIST_MANUAL_BLOCK: ' (remote manually blocked)',
    LEGACY_ADMIN_REPOSITORIES_LIST_CHECK_REMOTE: ' (checking remote…)',
    LEGACY_ADMIN_REPOSITORIES_LIST_PROXY: ' (attempting to proxy and remote unavailable)',
    LEGACY_ADMIN_REPOSITORIES_LIST_REMOTE_AVAILABLE: ' (remote available)',
    LEGACY_ADMIN_REPOSITORIES_LIST_REMOTE_UNAVAILABLE: ' (remote unavailable)',
    LEGACY_ADMIN_REPOSITORIES_LIST_FILTER_ERROR: 'No repositories matched "$filter"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_DELETE_BUTTON: 'Delete repository',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_DELETE_SUCCESS: 'Repository deleted: {0}',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_MORE_BUTTON: 'More',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_EXPIRE_ITEM: 'Expire cache',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_EXPIRE_SUCCESS: 'Started expiring caches of "{0}"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_REBUILD_ITEM: 'Rebuild metadata',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_REBUILD_SUCCESS: 'Started rebuilding metadata of "{0}"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_BLOCK_ITEM: 'Block proxy',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_BLOCK_SUCCESS: 'Blocked proxy on "{0}"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_ALLOW_ITEM: 'Allow proxy',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_ALLOW_SUCCESS: 'Allowed proxy on "{0}"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_POOS_ITEM: 'Put out of service',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_POOS_SUCCESS: 'Repository "{0}" was put out of service',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_PRIS_ITEM: 'Put in service',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_PRIS_SUCCESS: 'Repository "{0}" was put in service',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_REPAIR_ITEM: 'Repair index',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_REPAIR_SUCCESS: 'Started repairing index of "{0}"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_UPDATE_ITEM: 'Update index',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_UPDATE_SUCCESS: 'Started updating index of "{0}"',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_BROWSE_BUTTON: 'Browse',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_SETTINGS_TAB: 'Settings',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_HEALTH_CHECK_TAB: 'Health Check',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_NUGET_TAB: 'NuGet',
    LEGACY_ADMIN_REPOSITORIES_DETAILS_ROUTING_TAB: 'Routing',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE: 'Artifact max age',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ARTIFACT_AGE_HELP: 'How long to cache artifacts before rechecking the remote repository. Release repositories should use -1.',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_AUTHENTICATION: 'Authentication',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_BLOCKING: 'Auto blocking enabled',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_BLOCKING_HELP: 'Auto-block outbound connections on the repository if remote peer is detected as unreachable/unresponsive',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_BROWSING: 'Allow file browsing',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CERTIFICATE_BUTTON: 'View certificate',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CHECKSUM: 'Checksum policy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_PLACEHOLDER: 'Select a policy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_IGNORE_ITEM: 'Ignore',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_WARN_ITEM: 'Warn',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_EXISTS_ITEM: 'Strict if exists',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_CHECKSUM_STRICT_ITEM: 'Strict',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT: 'Deployment policy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_HELP: 'Controls if deployments and/or updates to artifacts are allowed',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_PLACEHOLDER: 'Select a policy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_ALLOW_ITEM: 'Allow redeploy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_DISABLE_ITEM: 'Disable redeploy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DEPLOYMENT_RO_ITEM: 'Read-only',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_DOWNLOAD: 'Download remote indexes',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_FILE: 'Remote content validation',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_FORMAT: 'Content format',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_HTTP: 'HTTP request settings',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ID: 'ID',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ID_HELP: 'Unique identifier for the repository',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ID_ERROR: 'Only letters, digits, underscores (_), hyphens (-), and dots (.) are allowed in the identifier',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ITEM_AGE: 'Item max age',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_ITEM_AGE_HELP: 'How long to cache non-artifact and metadata items before rechecking the remote repository',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_LOCAL: 'Local storage',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_LOCAL_HELP: 'Default location to store repository contents',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_METADATA_AGE: 'Metadata max age',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_METADATA_AGE_HELP: 'How long to cache metadata before rechecking the remote repository',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_MEMBERS: 'Member repositories',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_MEMBERS_HELP: 'Select and order the repositories that are part of this group',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_MEMBERS_FROM: 'Available',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_MEMBERS_TO: 'Members',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_NAME: 'Name',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_NAME_HELP: 'Repository display name',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_OVERRIDE: 'Override local storage',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_OVERRIDE_HELP: 'Custom location to store repository contents. Leave blank to use the default.',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_OVERRIDE_PLACEHOLDER: 'Enter a URL',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_POLICY: 'Repository policy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_POLICY_HELP: 'What type of artifacts does this repository store?',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_POLICY_PLACEHOLDER: 'Select a policy',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_POLICY_RELEASE_ITEM: 'Release',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_POLICY_SNAPSHOTS_ITEM: 'Snapshots',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_PROVIDER: 'Content provider',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_PUBLISH: 'Publish URL',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_REMOTE: 'Remote storage',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_REMOTE_HELP: 'Location of the remote repository being proxied',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_REMOTE_PLACEHOLDER: 'Enter a URL',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_SEARCH: 'Include in search results',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_SOURCE: 'Source repository',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_SOURCE_HELP: 'Physical repository presented as a logical view by the repository',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_SOURCE_PLACEHOLDER: 'Select a repository',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_SYNCHRONIZE: 'Synchronize on startup',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_SYNCHRONIZE_HELP: 'Rebuild virtual links when the server starts',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_TRUST_STORE: 'Use the Nexus truststore',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_TTL: 'Not found cache TTL',
    LEGACY_ADMIN_REPOSITORIES_SETTINGS_TTL_HELP: 'How long to cache the fact that a file was not found in the repository',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_PUBLISH_SECTION: 'Publish status',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_DISCOVERY_SECTION: 'Discovery status',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_DISCOVERY: 'Enable routing discovery',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_DISCOVERY_SUCCESS: 'Discovery started for: {0}',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_PUBLISHED: 'Data published on',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_RUN: 'Last discovery run',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_UPDATE: 'Discovery run interval',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_UPDATE_BUTTON: 'Update now',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_UPDATE_PLACEHOLDER: 'Select an interval',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_1_HOUR_ITEM: 'Hourly',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_2_HOUR_ITEM: 'Every 2 hours',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_3_HOUR_ITEM: 'Every 3 hours',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_6_HOUR_ITEM: 'Every 6 hours',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_9_HOUR_ITEM: 'Every 9 hours',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_12_HOUR_ITEM: 'Every 12 hours',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_DAILY_ITEM: 'Daily',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_WEEKLY_ITEM: 'Weekly',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_UPDATE_ERROR: 'You do not have permission to update routing repository settings',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_UPDATE_SUCCESS: 'Routing repository settings $action',
    LEGACY_ADMIN_REPOSITORIES_ROUTING_PREFIX_LINK: 'View prefix file',
    LEGACY_ADMIN_REPOSITORIES_NUGET_ACCESS: 'Accessing NuGet API Key requires validation of your credentials.',
    LEGACY_ADMIN_REPOSITORIES_NUGET_RESET: 'Resetting NuGet API Key requires validation of your credentials.'
  }
}, function(obj) {
  NX.I18n.register(obj.keys);
});

