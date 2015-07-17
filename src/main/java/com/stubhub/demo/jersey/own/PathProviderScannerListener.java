package com.stubhub.demo.jersey.own;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;

/**
 * An annotation-based scanning listener for classes annotated with
 * {@link Path} or {@link Provider}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class PathProviderScannerListener extends AnnotationScannerListener {

    /**
     * Create a scanning listener to check for Java classes in Java
     * class files annotated with {@link Path} or {@link Provider}.
     *
     */
    public PathProviderScannerListener() {
        super(Path.class, Provider.class, Produces.class);
    }

    /**
     * Create a scanning listener to check for Java classes in Java
     * class files annotated with {@link Path} or {@link Provider}.
     *
     * @param classloader the class loader to use to load Java classes that
     *        are annotated with any one of the annotations.
     */
    public PathProviderScannerListener(ClassLoader classloader) {
        super(classloader, Path.class, Provider.class, Produces.class);
    }
}
