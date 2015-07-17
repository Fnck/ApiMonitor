package com.stubhub.demo.jersey.own;

import com.sun.jersey.api.core.ResourceConfig;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A mutable implementation of {@link DefaultResourceConfig} that dynamically 
 * searches for root resource and provider classes in a given a set of
 * declared package and in all (if any) sub-packages of those declared packages.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class PackagesResourceConfig extends ScanningResourceConfig {
    /**
     * The property value MUST be an instance String or String[]. Each String
     * instance represents one or more package names that MUST be separated by
     * ';', ',' or ' ' (space).
     */
    public static final String PROPERTY_PACKAGES
            = "com.sun.jersey.config.property.packages";
    
    private static final Logger LOGGER = 
            Logger.getLogger(PackagesResourceConfig.class.getName());

    /**
     * Search for root resource classes declaring the packages as an 
     * array of package names.
     * 
     * @param packages the array package names.
     */
    public PackagesResourceConfig(String... packages) {
        if (packages == null || packages.length == 0)
            throw new IllegalArgumentException("Array of packages must not be null or empty");
        
        init(packages.clone());
    }

    /**
     * Search for root resource classes declaring the packages as a
     * property of {@link ResourceConfig}.
     * 
     * @param props the property bag that contains the property 
     *        {@link PackagesResourceConfig#PROPERTY_PACKAGES}. 
     */
    public PackagesResourceConfig(Map<String, Object> props) {
        this(getPackages(props));
        
        setPropertiesAndFeatures(props);
    }
    
    private void init(String[] packages) {
        if (LOGGER.isLoggable(Level.INFO)) {
            StringBuilder b = new StringBuilder();
            b.append("Scanning for root resource and provider classes in the packages:");
            for (String p : packages)
                b.append('\n').append("  ").append(p);
            
            LOGGER.log(Level.INFO, b.toString());
        }

        init(new PackageNamesScanner(packages));
    }
    
    private static String[] getPackages(Map<String, Object> props) {
        Object v = props.get(PROPERTY_PACKAGES);
        if (v == null)
            throw new IllegalArgumentException(PROPERTY_PACKAGES + 
                    " property is missing");
        
        String[] packages = getPackages(v);
        if (packages.length == 0)
            throw new IllegalArgumentException(PROPERTY_PACKAGES + 
                    " contains no packages");
        
        return packages;
    }
    
    private static String[] getPackages(Object param) {
        if (param instanceof String) {
            return getElements(new String[] { (String)param }, ResourceConfig.COMMON_DELIMITERS);
        } else if (param instanceof String[]) {
            return getElements((String[])param, ResourceConfig.COMMON_DELIMITERS);
        } else {
            throw new IllegalArgumentException(PROPERTY_PACKAGES + " must " +
                    "have a property value of type String or String[]");
        }
    }
}