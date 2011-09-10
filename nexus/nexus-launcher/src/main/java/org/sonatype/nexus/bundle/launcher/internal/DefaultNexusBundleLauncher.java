/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bundle.launcher.internal;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.bundle.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.ManagedNexusBundle;
import org.sonatype.nexus.bundle.launcher.NexusBundleLauncher;
import org.sonatype.nexus.bundle.launcher.NexusBundleLauncherException;
import org.sonatype.nexus.bundle.launcher.NexusBundleService;
import org.sonatype.nexus.bundle.launcher.NexusPort;
import org.sonatype.nexus.bundle.launcher.util.ArtifactResolver;
import org.sonatype.nexus.bundle.launcher.jsw.JSWExecSupport;
import org.sonatype.nexus.bundle.launcher.util.NexusBundleUtils;
import org.sonatype.nexus.bundle.launcher.util.PortReservationService;
import org.sonatype.nexus.bundle.launcher.util.ResolvedArtifact;
import org.sonatype.sisu.overlay.Overlay;

/**
 *
 * @author plynch
 */
@Named("default")
@Singleton
public class DefaultNexusBundleLauncher implements NexusBundleLauncher, NexusBundleService {

    private static final String NEXUS_CONTEXT = "/nexus";


    private Logger logger = LoggerFactory.getLogger(DefaultNexusBundleLauncher.class);
    /**
     * Used to resolveArtifactFile bundles and bundle plugins
     */
    private final ArtifactResolver artifactResolver;
    /**
     * Helper for common tasks
     */
    private final AntHelper ant;

    /**
     * Helper for common tasks
     */
    private final NexusBundleUtils bundleUtils;

    /**
     * service for reserving free ports used by bundles
     */
    private final PortReservationService portReservationService;
    /**
     * Directory where the service performs it's work
     */
    private final File serviceWorkDirectory;

    private final Map<String,ManagedNexusBundle> managedBundles = new ConcurrentHashMap<String, ManagedNexusBundle>();

    @Inject
    public DefaultNexusBundleLauncher(final ArtifactResolver artifactResolver, final PortReservationService portReservationService, final AntHelper ant, final NexusBundleUtils bundleUtils, @Named("${NexusBundleService.serviceWorkDirectory:-target/nbs}") final File serviceWorkDirectory) {
        Preconditions.checkNotNull(artifactResolver);
        Preconditions.checkNotNull(portReservationService);
        Preconditions.checkNotNull(serviceWorkDirectory);
        Preconditions.checkNotNull(ant);
        Preconditions.checkNotNull(bundleUtils);

        // required
        this.artifactResolver = artifactResolver;
        this.portReservationService = portReservationService;
        this.serviceWorkDirectory = serviceWorkDirectory;
        this.ant = ant;
        this.bundleUtils = bundleUtils;

        logger.debug(serviceWorkDirectory.getAbsolutePath());

        makeServiceDirectories();

    }

    /**
     * Make the required directories for the service to operate
     */
    protected final void makeServiceDirectories() {
        this.serviceWorkDirectory.mkdirs();
    }

    // ================== NexusBundleLauncher =========================
    public synchronized ManagedNexusBundle start(final NexusBundleConfiguration bundleConfiguration) {
        try {
            final ResolvedArtifact artifact = resolveArtifact(bundleConfiguration.getBundleArtifactCoordinates());
            final File bundleFile = resolveArtifactFile(artifact);
            final File extractionDir = computeExtractionDir(bundleConfiguration.getBundleId());
            final File appDir = computeNexusAppDir(extractionDir, artifact);

            this.bundleUtils.extractNexusBundle(bundleFile, extractionDir, bundleConfiguration.getNexusBundleExcludes());

            // if ( bundleConfiguration.isConfigureOptionalPlugins())
            //
            //     configureOptionalPlugins(extractionDir)
            // }

            final List<ResolvedArtifact> pluginArtifacts = artifactResolver.resolveArtifacts(bundleConfiguration.getPluginCoordinates());;
            installPlugins(appDir, pluginArtifacts);

            configureExtractedBundlePermissions(extractionDir);

            final File binDir = computeNexusBinDir(extractionDir, artifact);

            EnumMap<NexusPort,Integer> portMap = new EnumMap<NexusPort,Integer>(NexusPort.class);
            portMap.put(NexusPort.HTTP, this.portReservationService.reservePort());
            try {
                modifyJettyConfiguration(appDir, portMap.get(NexusPort.HTTP));
            } catch (IOException ex) {
                throw new NexusBundleLauncherException("Problem modifying jetty config", ex);
            }

            applyOverlays(bundleConfiguration, extractionDir);

            startBundle(binDir, "http://127.0.0.1:" + portMap.get(NexusPort.HTTP) + NEXUS_CONTEXT);

            // register
            File workDir = buildFilePath(extractionDir, "sonatype-work", "nexus");
            DefaultManagedNexusBundle managedBundle = new DefaultManagedNexusBundle(bundleConfiguration.getBundleId(),artifact, "127.0.0.1", portMap, NEXUS_CONTEXT, workDir, appDir);
            managedBundles.put(bundleConfiguration.getBundleId(), managedBundle);
            return managedBundle;
        } catch (IOException ex) {
            throw new NexusBundleLauncherException("Problem starting bundle", ex);
        }
    }

    @Override
    public ManagedNexusBundle start(NexusBundleConfiguration config, String groupName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized void stop(ManagedNexusBundle managedNexusBundle) {
        ManagedNexusBundle managedBundle = managedBundles.remove(managedNexusBundle.getId());
        if(managedBundle == null){
            // is it a good thing?
            throw new NexusBundleLauncherException("Managed bundle is not managed by this service.");
        }

        final File extractionDir = computeExtractionDir(managedBundle.getId());
        final File binDir = computeNexusBinDir(extractionDir, managedBundle.getArtifact());
        stopBundle(binDir);
        this.portReservationService.cancelPort(managedBundle.getHttpPort());

    }

    @Override
    public void stopAll(String groupName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopAll() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // =========================================

    /**
     * Resolve a file by its given artifact Coordinates.
     * @param artifactCoordinates the coordinates to locate the artifact with
     * @return the file of the resolved artifact
     */
    protected ResolvedArtifact resolveArtifact(final String artifactCoordinates) {
        return artifactResolver.resolveArtifact(artifactCoordinates);
    }

    /**
     * Get an artifact file from a Resolved Artifact, performing some sanity checks in the process.
     * @param artifact the artifact to get the file for
     * @return the File, never null
     * @throws NexusBundleLauncherException if a valid file could not be located.
     */
    protected File resolveArtifactFile(final ResolvedArtifact artifact) {
        Preconditions.checkNotNull(artifact);
        File file  = artifact.getFile();
        if (file == null) {
            throw new NexusBundleLauncherException("Artifact " + artifact + " is not resolved to a file.");
        }
        if (!file.isFile()) {
            throw new NexusBundleLauncherException("Artifact " + artifact + " is not a file. (" + file.getAbsolutePath() + ")");
        }
        return file;
    }

    /**
     * Compute the extraction directory for a given bundleConfiguration
     * @param bundleConfiguration the configuration for the bundle for which we are computing extraction dir
     * @return the computed extraction dir
     */
    protected File computeExtractionDir(final String bundleId){
        return new File(this.serviceWorkDirectory, bundleId);
    }

    protected File computeNexusAppDir(final File extractionDir, ResolvedArtifact artifact){
        return new File(extractionDir, artifact.getArtifactId() + "-" + artifact.getBaseVersion());
    }

    protected File computeNexusBinDir(final File extractionDir, ResolvedArtifact artifact){
        return new File(computeNexusAppDir(extractionDir, artifact), "bin");
    }



    /**
     * Set any required permissions on the extracted bundle dir.
     * <p>
     * This is in case the java extraction of the bundle loses permissions somehow.
     */
    protected void configureExtractedBundlePermissions(final File extractedBundleDir){
        Preconditions.checkNotNull(extractedBundleDir);
        try {
            ant.chmod(extractedBundleDir, "nexus-*/bin/**", "u+x");
        } catch (BuildException e) {
            throw new NexusBundleLauncherException("Problem setting permissions on bundle executables.", e);
        }
    }

    /**
     * Configure Jetty in the bundle via its properties, prior to managing the bundle
     * @param nexusAppDir
     * @param httpPort the http port jetty will listen on
     * @throws IOException
     */
    protected void modifyJettyConfiguration( final File nexusAppDir, final int httpPort )
        throws IOException
    {

        final File jettyProperties = new File( nexusAppDir, "conf/nexus.properties" );

        if ( !jettyProperties.isFile() )
        {
            throw new FileNotFoundException( "Jetty properties not found at " + jettyProperties.getAbsolutePath() );
        }

        Properties p = new Properties();
        InputStream in = new FileInputStream( jettyProperties );
        p.load( in );
        IOUtil.close( in );

        p.setProperty( "application-port", String.valueOf( httpPort ) );

        OutputStream out = new FileOutputStream( jettyProperties );
        p.store( out, "NexusStatusUtil" );
        IOUtil.close( out );

    }


    protected void startBundle(final File binDir, final String nexusBaseURL) {
        final JSWExecSupport jswExec = new JSWExecSupport(binDir, "nexus", ant);
        if(!jswExec.startAndWaitUntilReady(nexusBaseURL)){
            throw new NexusBundleLauncherException("Bundle start detection failed, see logs.");
        }
    }

    protected void stopBundle(final File binDir){
        final JSWExecSupport jswExec = new JSWExecSupport(binDir, "nexus", ant);
        jswExec.stop();
    }


    /**
     * Build a file from the specified path components.
     *
     * @param parent the parent directory to start building the path from
     * @param pathComponents path parts to append to the parent
     * @return the final file path with all the parts
     * FIXME move to utils
     */
    public static File buildFilePath(final File parent, final String... pathComponents) {
        StringBuilder path = new StringBuilder();
        for (String pathComponent : pathComponents) {
            path.append(pathComponent).append(File.separatorChar);
        }
        return new File(parent, path.toString());
    }

    protected List<ResolvedArtifact> resolveArtifacts(List<String> artifactCoordinates) {
        List<ResolvedArtifact> pluginFiles = new ArrayList<ResolvedArtifact>();
        for (String coord : artifactCoordinates) {
            pluginFiles.add(resolveArtifact(coord));
        }
        return pluginFiles;
    }

    protected void installPlugins(final File nexusAppDir, final List<ResolvedArtifact> pluginArtifacts) throws IOException{
        File pluginRepo = new File( nexusAppDir, "nexus/WEB-INF/plugin-repository" );
        for (ResolvedArtifact pluginArtifact : pluginArtifacts) {
            final File pluginFile = resolveArtifactFile(pluginArtifact);
            // standard nexus plugin bundle is a bundle.zip
            this.bundleUtils.extractNexusPlugin( pluginFile, pluginRepo );
        }
    }

    private void applyOverlays(NexusBundleConfiguration bundleConfiguration, File extractionDir) {
        List<Overlay> overlays = bundleConfiguration.getOverlays();
        if(overlays!=null&&overlays.size()>0)
        {
            for (Overlay overlay : overlays) {
                overlay.applyTo(extractionDir);
            }
        }
    }

}
