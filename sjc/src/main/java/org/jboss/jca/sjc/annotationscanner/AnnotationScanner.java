/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jca.sjc.annotationscanner;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * An annotation scanner for JCA/SJC
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class AnnotationScanner
{
   private static Logger log = Logger.getLogger(AnnotationScanner.class);
   private static boolean trace = log.isTraceEnabled();

   /**
    * Constructor
    */
   private AnnotationScanner()
   {
   }

   /**
    * Scan
    * @param urls The URLs with class files
    * @return The map of annotations
    */
   public static Map<Class, List<Annotation>> scan(URL[] urls)
   {
      Map<Class, List<Annotation>> result = new HashMap<Class, List<Annotation>>();

      long start = System.currentTimeMillis();

      URLClassLoader cl = new URLClassLoader(urls, null);

      ClassPool cp = new ClassPool();
      cp.appendClassPath(new LoaderClassPath(cl));

      List<String> classes = getClassNames(urls);
      if (classes != null)
      {
         for (String className : classes)
         {
            try
            {
               CtClass ctClass = cp.get(className);
               Class clz = ctClass.toClass();

               // Class level annotations
               Object[] classAnnotations = ctClass.getAvailableAnnotations();

               processAnnotations(classAnnotations, clz, result);

               // Method level annotations
               CtMethod[] methods = ctClass.getMethods();
               if (methods != null)
               {
                  for (CtMethod method : methods)
                  {
                     Object[] methodAnnotations = method.getAvailableAnnotations();
                     processAnnotations(methodAnnotations, clz, result);
                  }
               }

               // Field level annotations
               CtField[] fields = ctClass.getFields();
               if (fields != null)
               {
                  for (CtField field : fields)
                  {
                     Object[] fieldAnnotations = field.getAvailableAnnotations();
                     processAnnotations(fieldAnnotations, clz, result);
                  }
               }
            }
            catch (CannotCompileException cce)
            {
               // Shouldn't happen - nothing we can do
            }
            catch (NotFoundException nfe)
            {
               // Shouldn't happen - nothing we can do
            }
         }
      }

      if (cl != null && cl instanceof Closeable)
      {
         try
         {
            ((Closeable)cl).close();
         }
         catch (IOException ioe)
         {
            // Swallow
         }
      }

      log.debug("Scanning took: " + (System.currentTimeMillis() - start) + " ms");

      return result;
   }

   /**
    * Get all the class names
    * @param urls The urls to be scanned
    * @return The names
    */
   private static List<String> getClassNames(URL[] urls)
   {
      List<String> result = new ArrayList<String>();

      for (URL url : urls)
      {
         try
         {
            log.debug("Checking " + url);

            if ("file".equals(url.getProtocol()))
            {
               long start = System.currentTimeMillis();

               File f = new File(url.getFile());

               List<String> classFiles = null;

               if (url.getPath().endsWith("/"))
               {
                  classFiles = ClassScanner.scan(f);
               }
               else if (url.getPath().endsWith(".jar"))
               {
                  classFiles = JarScanner.scan(f);
               }

               if (classFiles != null)
               {
                  result.addAll(classFiles);
               }

               long end = System.currentTimeMillis();
               log.debug("Retrieving classes took " + (end - start) + " ms for " + url.getFile());
            }
            else
            {
               log.warn("Protocol not supported: " + url);
            }
         }
         catch (Exception e)
         {
            // Nothing we can do
         }
      }

      return result;
   }

   /**
    * Add annotations to the map
    * @param annotations The annotations
    * @param clz The location class
    * @param map The result map
    */
   private static void processAnnotations(Object[] annotations, Class clz, Map<Class, List<Annotation>> map)
   {
      if (annotations != null)
      {
         // These are proxy objects
         for (Object annotation : annotations)
         {
            Class annotationClass = annotation.getClass().getInterfaces()[0];
                        
            Annotation a = new Annotation(annotationClass.cast(annotation),
                                          clz);

            if (trace)
            {
               log.trace("AnnotationClass=" + annotationClass.getName());
               log.trace("Annotation=" + annotation);
               log.trace("Located=" + clz.getName());
            }
                        
            List<Annotation> l = map.get(annotationClass);
            if (l == null)
               l = new ArrayList<Annotation>();
                        
            l.add(a);
            map.put(annotationClass, l);
         }
      }
   }
}
