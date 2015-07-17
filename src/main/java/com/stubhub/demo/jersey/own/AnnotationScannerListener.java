package com.stubhub.demo.jersey.own;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sun.jersey.core.osgi.OsgiRegistry;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.scanning.ScannerListener;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A scanner listener that processes Java class files (resource names
 * ending in ".class") annotated with one or more of a set of declared
 * annotations.
 * <p>
 * Java classes of a Java class file are processed, using ASM, to ascertain
 * if those classes are annotated with one or more of the set of declared
 * annotations.
 * <p>
 * Such an annotated Java class of a Java class file is loaded if the class
 * is public or is an inner class that is static and public.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AnnotationScannerListener implements ScannerListener {
    private final ClassLoader classloader;

    private final Set<Class<?>> classes;

    private final Set<String> annotations;

    private final AnnotatedClassVisitor classVisitor;

    /**
     * Create a scanner listener to check for annotated Java classes in Java
     * class files.
     *
     * @param annotations the set of annotation classes to check on Java class
     *        files.
     */
    public AnnotationScannerListener(Class<? extends Annotation>... annotations) {
        this(AccessController.doPrivileged(ReflectionHelper.getContextClassLoaderPA()), annotations);
    }

    /**
     * Create a scanner listener to check for annotated Java classes in Java
     * class files.
     *
     * @param classloader the class loader to use to load Java classes that
     *        are annotated with any one of the annotations.
     * @param annotations the set of annotation classes to check on Java class
     *        files.
     */
    public AnnotationScannerListener(ClassLoader classloader,
                                     Class<? extends Annotation>... annotations) {
        this.classloader = classloader;
        this.classes = new LinkedHashSet<Class<?>>();
        this.annotations = getAnnotationSet(annotations);
        this.classVisitor = new AnnotatedClassVisitor();
    }

    /**
     * Get the set of annotated classes.
     *
     * @return the set of annotated classes.
     */
    public Set<Class<?>> getAnnotatedClasses() {
        return classes;
    }

    private Set<String> getAnnotationSet(Class<? extends Annotation>... annotations) {
        Set<String> a = new HashSet<String>();
        for (Class c : annotations) {
            a.add("L" + c.getName().replaceAll("\\.", "/") + ";");
        }
        return a;
    }

    // ScannerListener

    public boolean onAccept(String name) {
        return name.endsWith(".class");
    }

    public void onProcess(String name, InputStream in) throws IOException {
        new ClassReader(in).accept(classVisitor, 0);
    }

    //

    private final class AnnotatedClassVisitor implements ClassVisitor {

        /**
         * The name of the visited class.
         */
        private String className;
        /**
         * True if the class has the correct scope
         */
        private boolean isScoped;
        /**
         * True if the class has the correct declared annotations
         */
        private boolean isAnnotated;

        public void visit(int version, int access, String name,
                          String signature, String superName, String[] interfaces) {
            className = name;
            isScoped = (access & Opcodes.ACC_PUBLIC) != 0;
            isAnnotated = false;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            isAnnotated |= annotations.contains(desc);
            return null;
        }

        public void visitInnerClass(String name, String outerName,
                                    String innerName, int access) {
            // If the name of the class that was visited is equal
            // to the name of this visited inner class then
            // this access field needs to be used for checking the scope
            // of the inner class
            if (className.equals(name)) {
                isScoped = (access & Opcodes.ACC_PUBLIC) != 0;

                // Inner classes need to be statically scoped
                isScoped &= (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            }
        }

        public void visitEnd() {
            if (isScoped && isAnnotated) {
                // Correctly scoped and annotated
                // add to the set of matching classes.
            	String clazz = className.replaceAll("/", ".");
            	System.out.println(clazz);
                classes.add(getClassForName(clazz));
            }
        }

        public void visitOuterClass(String string, String string0,
                                    String string1) {
            // Do nothing
        }

        public FieldVisitor visitField(int i, String string,
                                       String string0, String string1, Object object) {
            // Do nothing
            return null;
        }

        public void visitSource(String string, String string0) {
            // Do nothing
        }

        public void visitAttribute(Attribute attribute) {
            // Do nothing
        }

        public MethodVisitor visitMethod(int i, String string,
                                         String string0, String string1, String[] string2) {
            // Do nothing
            return null;
        }

        private Class getClassForName(String className) {
            try {
                final OsgiRegistry osgiRegistry = ReflectionHelper.getOsgiRegistryInstance();

                if (osgiRegistry != null) {
                    return osgiRegistry.classForNameWithException(className);
                } else {
                    return AccessController.doPrivileged(ReflectionHelper.classForNameWithExceptionPEA(className, classloader));
                }
            } catch (ClassNotFoundException ex) {
                String s = "A class file of the class name, " +
                        className +
                        "is identified but the class could not be found";
                throw new RuntimeException(s, ex);
            } catch (PrivilegedActionException ex) {
                String s = "A class file of the class name, " +
                        className +
                        "is identified but the class could not be found";
                throw new RuntimeException(s, ex);
            }
        }

    }
}

