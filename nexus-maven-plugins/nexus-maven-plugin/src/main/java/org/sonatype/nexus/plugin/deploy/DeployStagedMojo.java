package org.sonatype.nexus.plugin.deploy;

import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Deploys the (previously) locally staged artifacts.
 * 
 * @author cstamas
 * @goal deploy-staged
 * @requiresProject false
 */
public class DeployStagedMojo
    extends AbstractDeployMojo
{
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( isThisLastProjectInExecution() )
        {
            failIfOffline();

            try
            {
                deployStagedArtifacts();
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }
}
