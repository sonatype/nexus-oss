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
import org.sonatype.nexus.bundle.launcher.util.PortReservationService;
import org.sonatype.nexus.bundle.launcher.util.ResolvedArtifact;

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
     * service for reserving free ports used by bundles
     */
    private final PortReservationService portReservationService;
    /**
     * Directory where the service performs it's work
     */
    private final File serviceWorkDirectory;
    /**
     * Root Directory where bundle overlays are looked up
     */
    private final File overlaysSourceDirectory;

    private final Map<String,ManagedNexusBundle> managedBundles = new ConcurrentHashMap<String, ManagedNexusBundle>();

    @Inject
    public DefaultNexusBundleLauncher(final ArtifactResolver artifactResolver, final PortReservationService portReservationService, final AntHelper ant, @Named("${NexusBundleService.serviceWorkDirectory:-target/nbs}") final File serviceWorkDirectory, @Named("${NexusBundleService.overlaySourceDirectory:-target/overlays}") final File overlaysSourceDirectory) {
        Preconditions.checkNotNull(artifactResolver);
        Preconditions.checkNotNull(portReservationService);
        Preconditions.checkNotNull(serviceWorkDirectory);
        Preconditions.checkNotNull(ant);
        Preconditions.checkNotNull(overlaysSourceDirectory);
        // required
        this.artifactResolver = artifactResolver;
        this.portReservationService = portReservationService;
        this.serviceWorkDirectory = serviceWorkDirectory;
        this.ant = ant;
        this.overlaysSourceDirectory = overlaysSourceDirectory;

        logger.debug(serviceWorkDirectory.getAbsolutePath());
        logger.debug(overlaysSourceDirectory.getAbsolutePath());

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

        final ResolvedArtifact artifact = resolveArtifact(bundleConfiguration.getBundleArtifactCoordinates());
        final File bundleFile = resolveArtifactFile(artifact);
        final File extractionDir = computeExtractionDir(bundleConfiguration.getBundleId());
        final File appDir = computeNexusAppDir(extractionDir, artifact);

        extractBundle(bundleFile, extractionDir, bundleConfiguration.getNexusBundleExcludes());
        configureExtractedBundlePermissions(extractionDir);

        // if ( bundleConfiguration.isConfigureOptionalPlugins())
        //
        //     configureOptionalPlugins(extractionDir)
        // }

        // final List<File> pluginFiles = resolveAdditionalPlugins(bundleConfiguration.getAdditionalPluginCoordinates())
        // configureAdditionalPlugins(pluginFiles);

        // final File bundleOverlaysSourceDirectory = computeBundleOverlaysSourceDirectory(bundleConfiguration);
        // installOverlays(extractionDir, bundleOverlaysSourceDirectory )

        final File binDir = computeNexusBinDir(extractionDir, artifact);

        EnumMap<NexusPort,Integer> portMap = new EnumMap<NexusPort,Integer>(NexusPort.class);
        portMap.put(NexusPort.HTTP, this.portReservationService.reservePort());
        try {
            modifyJettyConfiguration(appDir, portMap.get(NexusPort.HTTP));
        } catch (IOException ex) {
            throw new NexusBundleLauncherException("Problem modifying jetty config", ex);
        }

        startBundle(binDir, "http://127.0.0.1:" + portMap.get(NexusPort.HTTP) + NEXUS_CONTEXT);

        // register
        File workDir = buildFilePath(extractionDir, "sonatype-work", "nexus");
        DefaultManagedNexusBundle managedBundle = new DefaultManagedNexusBundle(bundleConfiguration.getBundleId(),artifact, "127.0.0.1", portMap, NEXUS_CONTEXT, workDir, appDir);
        managedBundles.put(bundleConfiguration.getBundleId(), managedBundle);
        return managedBundle;
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

    protected File resolveArtifactFile(final ResolvedArtifact artifact) {
        File file  = artifact.getFile();
        if (file == null) {
            throw new IllegalStateException("Artifact " + artifact + " is not resolved to a file.");
        }
        if (!file.isFile()) {
            throw new IllegalStateException("Artifact " + artifact + " is not a file. (" + file.getAbsolutePath() + ")");
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
        try {
            ant.chmod(extractedBundleDir, "nexus-*/bin/**", "u+x");
        } catch (BuildException e) {
            throw new NexusBundleLauncherException("Problem setting permissions on bundle executables.", e);
        }
    }

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

    /**
     * Extract the specified bundle file to a directory, excluding any provided patterns.
     * @param bundleFile the bundle file to extractUsingPlexus
     * @param extractionDir the directory to extractUsingPlexus to
     * @param nexusBundleExcludes the exclusion patterns to be applied during extraction
     */
    protected void extractBundle(final File sourceFile, final File toDir, final List<String> excludes) {
        Preconditions.checkNotNull(sourceFile);
        Preconditions.checkNotNull(toDir);
        Preconditions.checkNotNull(excludes);

        String fileName = sourceFile.getName();
        ant.mkdir(toDir);

        if (fileName.endsWith(".zip")) {
            //unArchiver = zipUnArchiverProvider.get();
            try {

                final Expand unzip = ant.createTask(Expand.class);
                unzip.setDest(toDir);
                unzip.setSrc(sourceFile);
                unzip.execute();

            } catch (BuildException e) {
                throw new NexusBundleLauncherException("Unable to unarchive " + sourceFile + " to " + toDir, e);
            }

        } else if (fileName.endsWith(".tar.gz")) {
            //throw new NexusBundleLauncherException("Archive type not supported yet " + fileName);
            try {
                final Untar untar = ant.createTask(Untar.class);
                untar.setDest(toDir);
                untar.setSrc(sourceFile);
                untar.setCompression(((Untar.UntarCompressionMethod)EnumeratedAttribute.getInstance(Untar.UntarCompressionMethod.class, "gzip")));
                untar.execute();

            } catch (BuildException e) {
                throw new NexusBundleLauncherException("Unable to unarchive " + sourceFile + " to " + toDir, e);
            }

        } else {
            throw new NexusBundleLauncherException("Archive type could not be determined from name: " + fileName);
        }

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


}
