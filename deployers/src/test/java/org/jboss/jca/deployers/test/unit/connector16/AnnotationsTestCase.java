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
package org.jboss.jca.deployers.test.unit.connector16;

import org.jboss.jca.common.annotations.Annotations;
import org.jboss.jca.common.annotations.repository.jandex.AnnotationScannerImpl;
import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.ra.AdminObject;
import org.jboss.jca.common.api.metadata.ra.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.ra.ConfigProperty;
import org.jboss.jca.common.api.metadata.ra.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.ra.Connector;
import org.jboss.jca.common.api.metadata.ra.Connector.Version;
import org.jboss.jca.common.api.metadata.ra.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.ra.Icon;
import org.jboss.jca.common.api.metadata.ra.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.LicenseType;
import org.jboss.jca.common.api.metadata.ra.LocalizedXsdString;
import org.jboss.jca.common.api.metadata.ra.MessageListener;
import org.jboss.jca.common.api.metadata.ra.Messageadapter;
import org.jboss.jca.common.api.metadata.ra.ResourceAdapter;
import org.jboss.jca.common.api.metadata.ra.SecurityPermission;
import org.jboss.jca.common.api.metadata.ra.XsdString;
import org.jboss.jca.common.api.metadata.ra.ra16.ConfigProperty16;
import org.jboss.jca.common.metadata.ra.common.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.ra.common.ResourceAdapter1516Impl;
import org.jboss.jca.common.metadata.ra.ra16.Activationspec16Impl;
import org.jboss.jca.common.metadata.ra.ra16.Connector16Impl;
import org.jboss.jca.common.spi.annotations.repository.AnnotationRepository;
import org.jboss.jca.common.spi.annotations.repository.AnnotationScanner;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 
 * A AnnotationsTestCase.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class AnnotationsTestCase
{
   private static Logger log = Logger.getLogger(AnnotationsTestCase.class);

   private String pack = "org.jboss.jca.deployers.test.rars.anno";

   /*
    * Annotations
    */
   private Annotations annotations;

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

         JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
         jar.addPackage(pack);

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
         log.info("///Connector1:" + c1);

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
    * 
    * Checks resulting connector
    * 
    * @param connector after annotations proceeding
    */
   private void checkConnector(Connector connector)
   {
      assertTrue(connector instanceof Connector16Impl);
      assertEquals(connector.getVersion(), Version.V_16);

      Connector16Impl con = (Connector16Impl) connector;
      assertEquals(null, con.getId());
      assertFalse(con.isMetadataComplete());
      assertEquals("", con.getModuleName());
      checkValues(con.getDescriptions(), "first", "second");
      checkValues(con.getDisplayNames(), "disp1", "disp2");

      List<Icon> icons = con.getIcons();
      /* @Ignored  - JBJCA-973
       * 
      assertEquals(4, icons.size()); 
      checkIcons(icons, "s1", "l1");
      checkIcons(icons, "", "l2");
      checkIcons(icons, "s3", "");
      checkIcons(icons,  "", "");
       * 
       */

      assertEquals(con.getVendorName().getValue(), "vendor");
      /* @Ignored - JBJCA-974
       * 
       * assertEquals(con.getResourceadapterVersion().getValue(), "1.a");
       */
      assertEquals(con.getEisType().getValue(), "type");

      LicenseType license = con.getLicense();
      checkValues(license.getDescriptions(), "lic1", "lic2");
      assertTrue(license.isLicenseRequired());

      ResourceAdapter rai = con.getResourceadapter();
      assertTrue(rai instanceof ResourceAdapter1516Impl);
      ResourceAdapter1516Impl ra = (ResourceAdapter1516Impl) rai;
      
      /*@Ignored - JBJCA-976
       * 
       * assertEquals(ra.getResourceadapterClass(), pack + ".AnnoResourceAdapter");
       */

      checkProperties(ra.getConfigProperties(), "String", "A", "Integer", "5");

      OutboundResourceAdapterImpl out = (OutboundResourceAdapterImpl) ra.getOutboundResourceadapter();
      List<ConnectionDefinition> cds = out.getConnectionDefinitions();
      assertEquals(2, cds.size());
      String bc = pack + ".AnnoConnection";
      for (ConnectionDefinition cd : cds)
      {
         if (valuesAreEqual(cd.getConnectionInterface(), bc))
         {
            assertTrue(valuesAreEqual(cd.getConnectionImplClass(), bc + "Impl"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryImplClass(), bc + "FactoryImpl"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryInterface(), bc + "Factory"));
            checkProperties(cd.getConfigProperties(), "Byte", "4", "Short", "0");
         }
         else
         {
            assertTrue(valuesAreEqual(cd.getConnectionInterface(), bc + "1"));
            assertTrue(valuesAreEqual(cd.getConnectionImplClass(), bc + "Impl1"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryImplClass(), bc + "FactoryImpl1"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryInterface(), bc + "Factory1"));
            checkProperties(cd.getConfigProperties(), "Byte", "2", "Short", "1");

         }
      }

      /* @Ignored - JBJCA-975
       * 
       * assertEquals(out.getTransactionSupport(), TransactionSupportEnum.LocalTransaction);
       */

      List<AuthenticationMechanism> ams = out.getAuthenticationMechanisms();
      assertEquals(2, ams.size());
      for (AuthenticationMechanism am : ams)
      {
         if (am.getCredentialInterface().equals(CredentialInterfaceEnum.PasswordCredential))
         {
            assertTrue(valuesAreEqual(am.getAuthenticationMechanismType(), "BasicPassword"));
            assertEquals(am.getDescriptions().size(), 0);
         }
         else
         {
            assertEquals(am.getCredentialInterface(), CredentialInterfaceEnum.GenericCredential);
            assertTrue(valuesAreEqual(am.getAuthenticationMechanismType(), "AuthMechanism"));
            checkValues(am.getDescriptions(), "desc1", "desc2");
         }
      }

      assertEquals(true, out.getReauthenticationSupport());

      InboundResourceAdapter in = ra.getInboundResourceadapter();

      Messageadapter ma = in.getMessageadapter();
      List<MessageListener> mls = ma.getMessagelisteners();
      assertEquals(2, mls.size());

      for (MessageListener ml : mls)
      {
         Activationspec16Impl as = (Activationspec16Impl) ml.getActivationspec();
         assertTrue(valuesAreEqual(as.getActivationspecClass(), pack + ".AnnoActivationSpec"));
         checkProperties(as.getConfigProperties(), "Character", "C", "Double", "0.5");
         /*@Ignored - JBJCA-977
          * 
          *assertEquals(as.getRequiredConfigProperties().size(), 1);
          *assertTrue(checkValue(as.getRequiredConfigProperties().get(0).getConfigPropertyName(), "first"));
         */
         if (!valuesAreEqual(ml.getMessagelistenerType(), pack + ".AnnoMessageListener"))
            assertTrue(valuesAreEqual(ml.getMessagelistenerType(), pack + ".AnnoMessageListener1"));
      }

      List<AdminObject> aos = ra.getAdminObjects();
      assertEquals(2, aos.size());
      bc = pack + ".AnnoAdminObject";
      for (AdminObject ao : aos)
      {
         if (valuesAreEqual(ao.getAdminobjectInterface(), bc))
         {
            assertTrue(valuesAreEqual(ao.getAdminobjectClass(), bc + "Impl"));
            checkProperties(ao.getConfigProperties(), "Long", "12345", "Boolean", "false");
         }
         else
         {
            assertTrue(valuesAreEqual(ao.getAdminobjectInterface(), bc + "1"));
            assertTrue(valuesAreEqual(ao.getAdminobjectClass(), bc + "Impl1"));
            checkProperties(ao.getConfigProperties(), "Float", "3.14", "String", "B");

         }
      }

      List<SecurityPermission> sps = ra.getSecurityPermissions();
      assertEquals(2, sps.size());
      for (SecurityPermission perm : sps)
      {
         if (valuesAreEqual(perm.getSecurityPermissionSpec(), "spec1"))
            assertEquals(perm.getDescriptions().size(), 0);
         else
         {
            assertTrue(valuesAreEqual(perm.getSecurityPermissionSpec(), "spec2"));
            /*@Ignored - JBJCA-978
             * 
             * checkValues(perm.getDescriptions(), "d1", "d2");
             */
         }

      }

      List<String> wcs = con.getRequiredWorkContexts();
      assertEquals(2, wcs.size());
      assertTrue(wcs.contains(pack + ".AnnoWorkContext"));
      assertTrue(wcs.contains(pack + ".AnnoWorkContext1"));

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
   private void checkProperties(List<? extends ConfigProperty> cps, String firstType, String firstValue,
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
   private boolean isPropertyCorrect(List<ConfigProperty16> cps, String name, String type, String value, String desc1,
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
   private void checkIcons(List<Icon> icons, String small, String large)
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
   private void checkValues(List<LocalizedXsdString> descs, String... texts)
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
   private boolean valuesAreEqual(XsdString str, String text)
   {
      if (str == null)
         return text == null;
      else
         return str.getValue().equals(text);
   }

}
