package com.stubhub.demo.api.monitor.aether.util;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
import org.sonatype.aether.impl.internal.DefaultServiceLocator;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;



public class AetherUtil
{

    public static RepositorySystem newRepositorySystem()
    {
    	DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class );
        locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );
        locator.setServices( WagonProvider.class, new ManualWagonProvider() );
        locator.setService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
        locator.setService( VersionResolver.class, DefaultVersionResolver.class );
        locator.setService( VersionRangeResolver.class, DefaultVersionRangeResolver.class );
        locator.setService( MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class );
        locator.setService( MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class );
        
		RepositorySystem system = locator.getService( RepositorySystem.class );
        return system;
    }

    public static RepositorySystemSession newRepositorySystemSession( RepositorySystem system )
    {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );

        //session.setTransferListener( new ConsoleTransferListener() );
        //session.setRepositoryListener( new ConsoleRepositoryListener() );

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static RemoteRepository newCentralRepository()
    {
        return new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" );
    }
    
    public static RemoteRepository newStubhubPublicRepository()
    {
        return new RemoteRepository( "stubhub", "default", "https://mvnrepository.stubcorp.dev/nexus/content/groups/stubhub-public/" );
    }
    
    public static RemoteRepository newStubhubSnapshotRepository()
    {
        return new RemoteRepository( "stubhub-snapshot", "default", "https://mvnrepository.stubcorp.dev/nexus/content/groups/stubhub-public-snapshots/" );
    }

}