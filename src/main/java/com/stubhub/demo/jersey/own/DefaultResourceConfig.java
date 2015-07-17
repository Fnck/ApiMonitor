package com.stubhub.demo.jersey.own;


import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mutable implementation of {@link ResourceConfig} that declares
 * default values for features.
 * <p>
 * The set of features and properties may be modified by modifying the instances 
 * returned from the methods {@link ResourceConfig#getFeatures} and 
 * {@link ResourceConfig#getProperties} respectively.
 */
public class DefaultResourceConfig extends ResourceConfig {
    
    private final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
    
    private final Set<Object> singletons = new LinkedHashSet<Object>(1);
    
    private final Map<String, MediaType> mediaExtentions = new HashMap<String, MediaType>(1);
    
    private final Map<String, String> languageExtentions = new HashMap<String, String>(1);

    private final Map<String, Object> explicitRootResources = new HashMap<String, Object>(1);

    private final Map<String, Boolean> features = new HashMap<String, Boolean>();
    
    private final Map<String, Object> properties = new HashMap<String, Object>();
    
    /**
     */
    public DefaultResourceConfig() {
        this((Set<Class<?>>)null);
    }
    
    /**
     * @param classes the initial set of root resource classes 
     *        and provider classes
     */
    public DefaultResourceConfig(Class<?>... classes) {
        this(new LinkedHashSet<Class<?>>(Arrays.asList(classes)));
    }
    
    /**
     * @param classes the initial set of root resource classes 
     *        and provider classes
     */
    public DefaultResourceConfig(Set<Class<?>> classes) {
        if (null != classes) {
            this.classes.addAll(classes);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
    
    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
    
    @Override
    public Map<String, MediaType> getMediaTypeMappings() {
        return mediaExtentions;
    }

    @Override
    public Map<String, String> getLanguageMappings() {
        return languageExtentions;
    }
    
    @Override
    public Map<String, Object> getExplicitRootResources() {
        return explicitRootResources;
    }

    @Override
    public Map<String, Boolean> getFeatures() {
        return features;
    }
    
    @Override
    public boolean getFeature(String featureName) {
        final Boolean v = features.get(featureName);
        return (v != null) ? v : false;
    }
    
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }
}