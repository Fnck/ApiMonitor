package com.stubhub.demo.jersey.own;

import com.sun.jersey.core.spi.scanning.Scanner;
import com.sun.jersey.spi.container.ReloadListener;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A resource configuration that performs scanning to find root resource
 * and provider classes.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ScanningResourceConfig extends DefaultResourceConfig implements ReloadListener {
    private static final Logger LOGGER = 
            Logger.getLogger(ScanningResourceConfig.class.getName());

    private Scanner scanner;

    private final Set<Class<?>> cachedClasses = new HashSet<Class<?>>();

    /**
     * Initialize and scan for root resource and provider classes
     * using a scanner.
     *
     * @param scanner the scanner.
     */
    public void init(final Scanner scanner) {
        this.scanner = scanner;

        final AnnotationScannerListener asl = new PathProviderScannerListener();
        scanner.scan(asl);

        getClasses().addAll(asl.getAnnotatedClasses());
        
        if (LOGGER.isLoggable(Level.INFO) && !getClasses().isEmpty()) {
            final Set<Class> rootResourceClasses = get(Path.class, Produces.class);
            if (rootResourceClasses.isEmpty()) {
                LOGGER.log(Level.INFO, "No root resource classes found.");
            } else {
                logClasses("Root resource classes found:", rootResourceClasses);
            }

            final Set<Class> providerClasses = get(Provider.class, Produces.class);
            if (providerClasses.isEmpty()) {
                LOGGER.log(Level.INFO, "No provider classes found.");
            } else {
                logClasses("Provider classes found:", providerClasses);
            }

        }

        cachedClasses.clear();
        cachedClasses.addAll(getClasses());
    }

    /**
     * Perform a new search for resource classes and provider classes.
     * <p/>
     * Deprecated, use onReload instead.
     */
    @Deprecated
    public void reload() {
        onReload();
    }

    /**
     * Perform a new search for resource classes and provider classes.
     */
    @Override
    public void onReload() {
        Set<Class<?>> classesToRemove = new HashSet<Class<?>>();
        Set<Class<?>> classesToAdd = new HashSet<Class<?>>();

        for(Class c : getClasses())
            if(!cachedClasses.contains(c))
                classesToAdd.add(c);

        for(Class c : cachedClasses)
            if(!getClasses().contains(c))
                classesToRemove.add(c);

        getClasses().clear();

        init(scanner);

        getClasses().addAll(classesToAdd);
        getClasses().removeAll(classesToRemove);
    }

    private Set<Class> get(Class<? extends Annotation> ac1, Class<? extends Annotation> ac2) {
        Set<Class> s = new HashSet<Class>();
        for (Class c : getClasses())
            if ((c.isAnnotationPresent(ac1))||(c.isAnnotationPresent(ac2)))
                s.add(c);
        return s;
    }

    private void logClasses(String s, Set<Class> classes) {
        final StringBuilder b = new StringBuilder();
        b.append(s);
        for (Class c : classes)
            b.append('\n').append("  ").append(c);

        LOGGER.log(Level.INFO, b.toString());
    }
}
