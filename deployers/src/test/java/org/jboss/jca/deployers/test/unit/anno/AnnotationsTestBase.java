/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.deployers.test.unit.anno;

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.annotations.repository.jandex.AnnotationScannerImpl;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * A AnnotationsTestBase - base class for annotation tests
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public abstract class AnnotationsTestBase
{
   /**
    * logger
    */
   protected static Logger log = Logger.getLogger(AnnotationsTestBase.class);


   /**
    * 
    * Annotations
    */
   protected Annotations annotations;

   /**
    * be run before the Test method.
    * @throws Throwable throwable exception
    */
   @Before
   public void setup() throws Throwable
   {
      annotations = new Annotations();
   }

   /**
    * causes that method to be run after the Test method.
    * @throws Throwable throwable exception
    */
   @After
   public void tearDown() throws Throwable
   {
      annotations = null;
   }

   /**
    * 
    * Proceeds annotation parsing on archive
    * 
    * @throws Throwable in case of error
    */
   @Test
   public void proceed() throws Throwable
   {
      try
      {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

         JavaArchive jar = createArchive();

         String fileName = System.getProperty("archives.dir") + File.separator + "anno.jar";
         File f = new File(fileName);
         jar.as(ZipExporter.class).exportTo(f, true);
         URL url = f.toURI().toURL();

         AnnotationScanner asf = new AnnotationScannerImpl();
         AnnotationRepository ar = asf.scan(new URL[]
         {url}, classLoader);
         Connector c = annotations.process(ar, null, classLoader);
         log.info("///Connector:" + c);
         checkConnector(c);
         //checkMerging with null connector
         checkConnector(annotations.merge(null, ar, classLoader));

         //getting empty connector
         url = new File(System.getProperty("archives.dir")).toURI().toURL();
         AnnotationRepository ar1 = asf.scan(new URL[]
         {url}, classLoader);
         Connector c1 = annotations.process(ar1, null, classLoader);

         //check merging
         checkConnector(annotations.merge(c, ar1, classLoader));
         checkConnector(annotations.merge(c1, ar, classLoader));
      }
      catch (AssertionError e)
      {
         e.printStackTrace();
         fail(e.getMessage());
      }
   }

   /**
    * Creates archive with annotated class to proceed 
    * @return java archive
    */
   protected abstract JavaArchive createArchive();

   /**
    * 
    * Checks resulting connector
    * 
    * @param connector after annotations proceeding
    */
   protected void checkConnector(Connector connector)
   {
   }

   /**
    * 
    * Checks, if all ConfigProperties objects are correct
    * 
    * @param cps - list of ConfigProperties to check
    * @param firstType - type of first property
    * @param firstValue - value of first property
    * @param secondType - type of second property
    * @param secondValue - value of second property
    */
   protected void checkProperties(List<? extends ConfigProperty> cps, String firstType, String firstValue,
      String secondType, String secondValue)
   {
      assertEquals(2, cps.size());

      @SuppressWarnings("unchecked")
      List<ConfigProperty16> cp = (List<ConfigProperty16>) cps;
      assertTrue(isPropertyCorrect(cp, "first", firstType, firstValue, "1st", "first", true, false, true));
      assertTrue(isPropertyCorrect(cp, "second", secondType, secondValue, "2nd", "second", false, true, false));
   }

   /**
    * 
    * Looks up in ConfigProperties list for a property with some parameters set
    * 
    * @param cps - list of ConfigProperties
    * @param name - name of property
    * @param type - type of property
    * @param value - value of property
    * @param desc1 - description1
    * @param desc2 - description 2
    * @param ignore - is property ignored?
    * @param dynamic - is property supported dynamic updates?
    * @param confident - is property confidential?
    * @return true, if list contains ConfigProperty with all these parameters set
    */
   protected boolean isPropertyCorrect(List<ConfigProperty16> cps, String name, String type, String value, String desc1,
      String desc2, boolean ignore, boolean dynamic, boolean confident)
   {
      for (ConfigProperty16 p : cps)
      {
         if (valuesAreEqual(p.getConfigPropertyName(), name) &&
             valuesAreEqual(p.getConfigPropertyType(), "java.lang." + type) &&
             valuesAreEqual(p.getConfigPropertyValue(), value))
         {
            checkValues(p.getDescriptions(), desc2, desc1);
            assertEquals(p.getConfigPropertyIgnore(), ignore);
            assertEquals(p.getConfigPropertySupportsDynamicUpdates(), dynamic);
            assertEquals(p.getConfigPropertyConfidential(), confident);
            return true;
         }

      }
      return false;
   }

   /**
    * 
    * Checks, if List of icons contains icon with small and large icons values set
    * 
    * @param icons - List of Icons
    * @param small icon value
    * @param large icon value
    */
   protected void checkIcons(List<Icon> icons, String small, String large)
   {
      for (Icon icon : icons)
      {
         log.info("Icon:" + icon.getSmallIcon() + "//" + icon.getLargeIcon());
         if (valuesAreEqual(icon.getSmallIcon(), small) && valuesAreEqual(icon.getLargeIcon(), large))
            return;
      }
      fail(icons + "There's no icon with files: " + small + " and " + large);
   }

   /**
    * 
    * Checks, if one of LocalizedXsdStrings contains some String value
    * 
    * @param descs List of LocalizedXsdStrings to proceed
    * @param texts - String objects, that should be values of LocalizedXsdStrings
    */
   protected void checkValues(List<LocalizedXsdString> descs, String... texts)
   {
      assertEquals(descs.size(), texts.length);
      List<String> txt = Arrays.asList(texts);
      for (LocalizedXsdString str : descs)
      {
         assertTrue(txt.contains(str.getValue()));
      }
   }

   /**
    * 
    * Checks, if value of XsdString equals to some String
    * 
    * @param str XsdString
    * @param text to compare
    * @return true, if values are equal
    */
   protected boolean valuesAreEqual(XsdString str, String text)
   {
      if (str == null)
         return text == null;
      else
         return str.getValue().equals(text);
   }

}
