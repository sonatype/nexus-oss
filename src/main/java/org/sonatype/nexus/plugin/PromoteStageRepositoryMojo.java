package org.sonatype.nexus.plugin;

/**
 * Promote a finished Nexus staging repository into a permanent Nexus repository for general consumption.
 * 
 * @goal staging-promote
 * @requiresProject false
 * @aggregator
 * @deprecated
 */
@Deprecated
public class PromoteStageRepositoryMojo extends ReleaseStageRepositoryMojo
{

}
