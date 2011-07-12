package org.sonatype.nexus.plugin;

/**
 * Promote a finished Nexus staging repository into a permanent Nexus repository for general consumption.
 * 
 * @goal staging-promote
 * @requiresProject false
 * @aggregator
 * @deprecated Replaced by: staging-release. 
 */
@Deprecated
public class PromoteStageRepositoryMojo extends ReleaseStageRepositoryMojo
{

}
