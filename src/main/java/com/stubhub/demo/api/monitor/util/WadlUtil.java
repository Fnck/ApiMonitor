package com.stubhub.demo.api.monitor.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stubhub.demo.api.monitor.entity.WadlGeneratorDescription;
import com.stubhub.demo.jersey.own.PackagesResourceConfig;
import com.stubhub.demo.jersey.own.ResourceConfig;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.generators.WadlGeneratorApplicationDoc;
import com.sun.jersey.server.wadl.generators.WadlGeneratorJAXBGrammarGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Include;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;

public class WadlUtil {
	private static Logger logger = LoggerFactory.getLogger(WadlUtil.class);
	
	public File _wadlFile;
	public boolean _formatWadlFile;
	public String _baseUri;
	public String[] _packagesResourceConfig;
	public List<WadlGeneratorDescription> _wadlGenerators;
	public static void main(String[] args) throws Exception{
		WadlUtil util = new WadlUtil();
		util._baseUri = "http://example.com:8080/rest";
		util._wadlFile = new File("c:\\myapplication.wadl");
		util._packagesResourceConfig = new String[]{"com.stubhub.demo.cxf.intf"};
		util._wadlGenerators = new ArrayList<WadlGeneratorDescription>();
		WadlGeneratorDescription docDesc = new WadlGeneratorDescription();
		docDesc.setClassName(WadlGeneratorApplicationDoc.class.getName());
		Properties docProp = new Properties();
		docProp.put("applicationDocsFile", "C:\\Work\\MySpace\\CXFDemo\\src\\main\\doc\\application-doc.xml");
		docDesc.setProperties(docProp);
		/*
		WadlGeneratorDescription grammarDesc = new WadlGeneratorDescription();
		grammarDesc.setClassName(WadlGeneratorGrammarsSupport.class.getName());
		Properties grammarProp = new Properties();
		grammarProp.put("grammarsFile", "C:\\Work\\MySpace\\CXFDemo\\src\\main\\doc\\application-grammars.xml");
		grammarDesc.setProperties(grammarProp);
		*/
		util._wadlGenerators.add(docDesc);
		//util._wadlGenerators.add(grammarDesc);
		
		util.generateWadl();
	}
	
	public String generateWadl() throws Exception{
		this._formatWadlFile = true;
		WadlGenerator wadlGenerator = new WadlGeneratorJAXBGrammarGenerator();
		if (this._wadlGenerators != null) {
			for (WadlGeneratorDescription wadlGeneratorDescription : this._wadlGenerators) {
				wadlGenerator = loadWadlGenerator(wadlGeneratorDescription, wadlGenerator);
			}
		}
		wadlGenerator.init();
		
		ApplicationDescription ad = createApplicationDescription(this._packagesResourceConfig, wadlGenerator);
		Application a = ad.getApplication();
		for (Resources resources : a.getResources()) {
			resources.setBase(this._baseUri);
		}
		//writeExternalGrammars(ad)
		
		/***
		 * start to sort the application elements
		 */
		for(Resources r : a.getResources()){
			sortResourceList(r.getResource());
			for(Resource rs : r.getResource()){
				sortMethodOrResource(rs.getMethodOrResource());				
			}
		}
		
		JAXBContext c = JAXBContext.newInstance(wadlGenerator.getRequiredJaxbContextPath(), Thread.currentThread().getContextClassLoader());

		Marshaller m = c.createMarshaller();
		m.setProperty("jaxb.formatted.output", Boolean.valueOf(this._formatWadlFile));

		OutputStream out = new ByteArrayOutputStream();//new BufferedOutputStream(new FileOutputStream(this._wadlFile));

		XMLSerializer serializer = getXMLSerializer(out);

		m.marshal(a, serializer);
		out.close();
		XmlFormatter xf = new XmlFormatter();
		String formattedXml = xf.format(out.toString());
		//logger.info(formattedXml);
		return formattedXml;
	}
	
	public void generateWadlDoc() throws Exception{
		this._formatWadlFile = true;
		WadlGenerator wadlGenerator = new WadlGeneratorJAXBGrammarGenerator();
		if (this._wadlGenerators != null) {
			for (WadlGeneratorDescription wadlGeneratorDescription : this._wadlGenerators) {
				wadlGenerator = loadWadlGenerator(wadlGeneratorDescription, wadlGenerator);
			}
		}
		wadlGenerator.init();
		
		ApplicationDescription ad = createApplicationDescription(this._packagesResourceConfig, wadlGenerator);
		Application a = ad.getApplication();
		for (Resources resources : a.getResources()) {
			resources.setBase(this._baseUri);
		}
		//writeExternalGrammars(ad)
		
		/***
		 * start to sort the application elements
		 */
		for(Resources r : a.getResources()){
			sortResourceList(r.getResource());
			for(Resource rs : r.getResource()){
				sortMethodOrResource(rs.getMethodOrResource());				
			}
		}
		
		JAXBContext c = JAXBContext.newInstance(wadlGenerator.getRequiredJaxbContextPath(), Thread.currentThread().getContextClassLoader());

		Marshaller m = c.createMarshaller();
		m.setProperty("jaxb.formatted.output", Boolean.valueOf(this._formatWadlFile));

		OutputStream out = new ByteArrayOutputStream();//new BufferedOutputStream(new FileOutputStream(this._wadlFile));

		XMLSerializer serializer = getXMLSerializer(out);

		m.marshal(a, serializer);
		out.close();
		XmlFormatter xf = new XmlFormatter();
		String formattedXml = xf.format(out.toString());
		logger.info(formattedXml);
		
		FileOutputStream fop = new FileOutputStream(this._wadlFile);
		if (!this._wadlFile.exists()) {
			this._wadlFile.createNewFile();
		}
		byte [] content = formattedXml.getBytes();
		fop.write(content);
		fop.flush();
		fop.close();
		//getLog().info("Wrote " + this._wadlFile);
	}
	
	private void sortMethodOrResource(List<Object> methodOrResourceList){
		sortList(methodOrResourceList);
		for(Object o : methodOrResourceList){
			if(o instanceof Resource){
				sortList(((Resource)o).getMethodOrResource());
				sortMethodOrResource(((Resource)o).getMethodOrResource());
				if(((Resource)o).getParam() != null){
					sortParamList(((Resource)o).getParam());
				}
			}
			if(o instanceof com.sun.research.ws.wadl.Method){
				if(((com.sun.research.ws.wadl.Method)o).getRequest() != null){
					sortParamList(((com.sun.research.ws.wadl.Method)o).getRequest().getParam());
					for(Representation rp : ((com.sun.research.ws.wadl.Method)o).getRequest().getRepresentation()){
						if(rp.getElement() != null){
							logger.info("QName of request representation: {}",rp.getElement().toString());
						}						
					}
				}				
			}
		}		
	}
	
	private void writeExternalGrammars(ApplicationDescription ad) throws FileNotFoundException, IOException {
	    File wadlParent = this._wadlFile.getParentFile();
	    Set<String> externalMetadataKeys = ad.getExternalMetadataKeys();

	    List hrefs = new ArrayList();
	    for (String key : externalMetadataKeys) {
	      ApplicationDescription.ExternalGrammar externalGrammar = ad.getExternalGrammar(key);

	      File externalFile = new File(wadlParent, key);
	      OutputStream externalGrammarOutputStream = new BufferedOutputStream(new FileOutputStream(externalFile));
	      try {
	        externalGrammarOutputStream.write(externalGrammar.getContent());
	        //getLog().info("Wrote " + externalFile);
	      } finally {
	        externalGrammarOutputStream.close();
	      }
	      hrefs.add(key);
	    }

	    JAXBGrammars grammars = new JAXBGrammars(hrefs);
	    Application application = ad.getApplication();
	    application.setGrammars(grammars);
	  }

	  private XMLSerializer getXMLSerializer(OutputStream out) throws FileNotFoundException
	  {
	    OutputFormat of = new OutputFormat();

	    of.setCDataElements(new String[] { "http://wadl.dev.java.net/2009/02^doc", "ns2^doc", "^doc" });

	    of.setPreserveSpace(true);
	    of.setIndenting(true);

	    XMLSerializer serializer = new XMLSerializer(of);

	    serializer.setOutputByteStream(out);

	    return serializer;
	  }

	  private WadlGenerator loadWadlGenerator(WadlGeneratorDescription wadlGeneratorDescription, WadlGenerator wadlGeneratorDelegate)
	    throws Exception
	  {
	    //getLog().info("Loading wadlGenerator " + wadlGeneratorDescription.getClassName());
	    Class clazz = Class.forName(wadlGeneratorDescription.getClassName(), true, Thread.currentThread().getContextClassLoader());
	    WadlGenerator generator = (WadlGenerator)clazz.asSubclass(WadlGenerator.class).newInstance();
	    generator.setWadlGeneratorDelegate(wadlGeneratorDelegate);
	    if ((wadlGeneratorDescription.getProperties() != null) && (!wadlGeneratorDescription.getProperties().isEmpty()))
	    {
	      for (Map.Entry entry : wadlGeneratorDescription.getProperties().entrySet()) {
	        setProperty(generator, entry.getKey().toString(), entry.getValue());
	      }
	    }
	    return generator;
	  }

	  private void setProperty(Object object, String propertyName, Object propertyValue) throws Exception
	  {
	    String methodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
	    Method method = getMethodByName(methodName, object.getClass());
	    if (method.getParameterTypes().length != 1) {
	      throw new RuntimeException("Method " + methodName + " is no setter, it does not expect exactly one parameter, but " + method.getParameterTypes().length);
	    }
	    Class paramClazz = method.getParameterTypes()[0];
	    if (paramClazz == propertyValue.getClass()) {
	      method.invoke(object, new Object[] { propertyValue });
	    }
	    else
	    {
	      Constructor paramTypeConstructor = getMatchingConstructor(paramClazz, propertyValue);
	      if (paramTypeConstructor != null) {
	        Object typedPropertyValue;
	        try { typedPropertyValue = paramTypeConstructor.newInstance(new Object[] { propertyValue });
	        } catch (Exception e) {
	          throw new Exception("Could not create instance of configured property " + propertyName + " from value " + propertyValue + ", using the constructor " + paramTypeConstructor, e);
	        }

	        method.invoke(object, new Object[] { typedPropertyValue });
	      }
	      else {
	        throw new RuntimeException("The property '" + propertyName + "' could not be set" + " because the expected parameter is neither of type " + propertyValue.getClass() + " nor of any type that provides a constructor expecting a " + propertyValue.getClass() + "." + " The expected parameter is of type " + paramClazz.getName());
	      }
	    }
	  }

	  private Constructor<?> getMatchingConstructor(Class<?> paramClazz, Object propertyValue)
	  {
	    Constructor[] constructors = paramClazz.getConstructors();
	    for (Constructor constructor : constructors) {
	      Class[] parameterTypes = constructor.getParameterTypes();
	      if ((parameterTypes.length == 1) && (constructor.getParameterTypes()[0] == propertyValue.getClass()))
	      {
	        return constructor;
	      }
	    }
	    return null;
	  }

	  private Method getMethodByName(String methodName, Class<?> clazz) {
	    for (Method method : clazz.getMethods()) {
	      if (method.getName().equals(methodName)) {
	        return method;
	      }
	    }
	    throw new RuntimeException("Method '" + methodName + "' not found for class " + clazz.getName());
	  }

	  @SuppressWarnings({ "unchecked", "rawtypes" })
	private ApplicationDescription createApplicationDescription(String[] paths, WadlGenerator wadlGenerator) throws MojoExecutionException {
	    Map map = new HashMap();
	    map.put("com.sun.jersey.config.property.packages", paths);
	    ResourceConfig rc = new PackagesResourceConfig(map);
	    Set s = new HashSet();
	    for (Class c : rc.getRootResourceClasses()) {
	      //getLog().debug("Adding class " + c.getName());
	      s.add(IntrospectionModeller.createResource(c));
	    }
	    return new WadlBuilder(wadlGenerator).generate(null, s);
	  }

	  public void setWadlFile(File wadlFile)
	  {
	    this._wadlFile = wadlFile;
	  }

	  public void setBaseUri(String baseUri)
	  {
	    this._baseUri = baseUri;
	  }

	  public void setPackagesResourceConfig(String[] packagesResourceConfig)
	  {
	    this._packagesResourceConfig = packagesResourceConfig;
	  }

	  public void setFormatWadlFile(boolean formatWadlFile)
	  {
	    this._formatWadlFile = formatWadlFile;
	  }

	  public void setWadlGenerators(List<WadlGeneratorDescription> wadlGenerators)
	  {
	    this._wadlGenerators = wadlGenerators;
	  }

	  private static class JAXBGrammars extends Grammars
	  {
	    public JAXBGrammars(List<String> hrefs) {
	      List includes = new ArrayList();

	      for (String href : hrefs) {
	        Include include = new Include();
	        include.setHref(href);

	        includes.add(include);
	      }

	      this.include = includes;
	    }
	  }
	  
	  private void sortResourceList(List<Resource> resource) {
		  if(resource.size() < 2){
			  return;
		  }
		  Map<String, Resource> t = new HashMap<String, Resource>(); 
		  for(Resource f : resource){
			  t.put(f.getPath(), f);
		  }
		  resource.clear();
		  String[] sortl = t.keySet().toArray(new String[]{});
		  List<String> keyl = Arrays.asList(sortl);
		  Collections.sort(keyl, new Comparator<String>(){

			  @Override
			  public int compare(String o1, String o2) {
				  return o2.compareTo(o1);
			  }

		  });
		  for(String k : keyl){
			  logger.info("Resource : " + k);
			  resource.add(t.get(k));
		  }
	  }

	  
	  private void sortParamList(List<Param> methodParameters) {
		  if(methodParameters.size() < 2){
			  return;
		  }
		  Map<String, Param> t = new HashMap<String, Param>(); 
		  for(Param p : methodParameters){
			  t.put(p.getName(), p);
		  }
		  methodParameters.clear();
		  String[] sortl = t.keySet().toArray(new String[]{});
		  List<String> keyl = Arrays.asList(sortl);
		  Collections.sort(keyl, new Comparator<String>(){

			  @Override
			  public int compare(String o1, String o2) {
				  return o2.compareTo(o1);
			  }

		  });
		  for(String k : keyl){
			  logger.info("Param: " + k);
			  methodParameters.add(t.get(k));
		  }
	  }

		private void sortList(List<Object> input){
			if(input.size() < 2){
				return;
			}
			Map<String, Object> t = new HashMap<String, Object>(); 
			for(Object f : input){
				if(f instanceof Resources){
					t.put(((Resources) f).getBase(), f);
				}
				if(f instanceof Resource){
					t.put(((Resource) f).getPath(), f);
				}
				if(f instanceof com.sun.research.ws.wadl.Method){
					t.put(((com.sun.research.ws.wadl.Method) f).getId(), f);
				}
			}
			input.clear();
			String[] sortl = t.keySet().toArray(new String[]{});
			List<String> keyl = Arrays.asList(sortl);
			Collections.sort(keyl, new Comparator<String>(){

				@Override
				public int compare(String o1, String o2) {
					return o2.compareTo(o1);
				}

			});
			for(String k : keyl){
				logger.info("MethodOrResource: " + k);
				input.add(t.get(k));
			}
		}
}
