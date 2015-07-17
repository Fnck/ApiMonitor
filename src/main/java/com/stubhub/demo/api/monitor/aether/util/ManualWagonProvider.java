package com.stubhub.demo.api.monitor.aether.util;

import org.apache.maven.wagon.Wagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

public class ManualWagonProvider
implements WagonProvider
{

public Wagon lookup( String roleHint )
    throws Exception
{
    if ( "http".equals( roleHint ) )
    {
        return new WagonHttpClient();
    }
    if	("https".equals(roleHint)){
    	System.setProperty("jsse.enableSNIExtension", "false");
    	return new WagonHttpsClient();
    }
    return null;
}

public void release( Wagon wagon )
{

}

}
