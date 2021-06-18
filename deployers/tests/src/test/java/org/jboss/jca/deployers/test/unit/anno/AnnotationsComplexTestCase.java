/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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

import org.jboss.jca.common.api.metadata.common.TransactionSupportEnum;
import org.jboss.jca.common.api.metadata.spec.AdminObject;
import org.jboss.jca.common.api.metadata.spec.AuthenticationMechanism;
import org.jboss.jca.common.api.metadata.spec.ConnectionDefinition;
import org.jboss.jca.common.api.metadata.spec.Connector;
import org.jboss.jca.common.api.metadata.spec.Connector.Version;
import org.jboss.jca.common.api.metadata.spec.CredentialInterfaceEnum;
import org.jboss.jca.common.api.metadata.spec.Icon;
import org.jboss.jca.common.api.metadata.spec.InboundResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.LicenseType;
import org.jboss.jca.common.api.metadata.spec.MessageListener;
import org.jboss.jca.common.api.metadata.spec.Messageadapter;
import org.jboss.jca.common.api.metadata.spec.ResourceAdapter;
import org.jboss.jca.common.api.metadata.spec.SecurityPermission;
import org.jboss.jca.common.metadata.spec.ActivationSpecImpl;
import org.jboss.jca.common.metadata.spec.ConnectorImpl;
import org.jboss.jca.common.metadata.spec.OutboundResourceAdapterImpl;
import org.jboss.jca.common.metadata.spec.ResourceAdapterImpl;

import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.junit.Assert.*;

/**
 * 
 * A complex example. Proceeds all possible annotations in one RA.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public class AnnotationsComplexTestCase extends AnnotationsTestBase
{

   private String pack = "org.jboss.jca.deployers.test.rars.anno";

   /**
    * 
    * Checks resulting connector
    * 
    * @param connector after annotations proceeding
    */
   @Override
   protected void checkConnector(Connector connector)
   {
      assertTrue(connector instanceof ConnectorImpl);
      assertEquals(connector.getVersion(), Version.V_17);

      ConnectorImpl con = (ConnectorImpl) connector;
      assertEquals(null, con.getId());
      assertFalse(con.isMetadataComplete());
      assertEquals("", con.getModuleName());
      checkValues(con.getDescriptions(), "first", "second");
      checkValues(con.getDisplayNames(), "disp1", "disp2");

      List<Icon> icons = con.getIcons();
      assertNotNull(icons);
      assertEquals(4, icons.size()); 
      checkIcons(icons, "s1", "");
      checkIcons(icons, "", "l1");
      checkIcons(icons, "", "l2");
      checkIcons(icons, "s3", "");

      assertEquals(con.getVendorName().getValue(), "vendor");
      assertEquals(con.getResourceadapterVersion().getValue(), "1.a");
      assertEquals(con.getEisType().getValue(), "type");

      LicenseType license = con.getLicense();
      checkValues(license.getDescriptions(), "lic1", "lic2");
      assertTrue(license.isLicenseRequired());

      ResourceAdapter rai = con.getResourceadapter();
      assertTrue(rai instanceof ResourceAdapterImpl);
      ResourceAdapterImpl ra = (ResourceAdapterImpl) rai;
      
      assertEquals(pack + ".AnnoResourceAdapter", ra.getResourceadapterClass());

      checkProperties(ra.getConfigProperties(), "String", "A", "Integer", "5");

      OutboundResourceAdapterImpl out = (OutboundResourceAdapterImpl) ra.getOutboundResourceadapter();
      List<ConnectionDefinition> cds = out.getConnectionDefinitions();
      assertEquals(2, cds.size());
      String bc = pack + ".AnnoConnection";
      for (ConnectionDefinition cd : cds)
      {
         if (valuesAreEqual(cd.getConnectionInterface(), bc))
         {
            assertTrue(valuesAreEqual(cd.getManagedConnectionFactoryClass(), pack + ".AnnoManagedConnectionFactory"));
            assertTrue(valuesAreEqual(cd.getConnectionImplClass(), bc + "Impl"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryImplClass(), bc + "FactoryImpl"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryInterface(), bc + "Factory"));
            checkProperties(cd.getConfigProperties(), "Byte", "4", "Short", "0");
         }
         else
         {
            assertTrue(valuesAreEqual(cd.getManagedConnectionFactoryClass(), pack + ".AnnoManagedConnectionFactory1"));
            assertTrue(valuesAreEqual(cd.getConnectionInterface(), bc + "1"));
            assertTrue(valuesAreEqual(cd.getConnectionImplClass(), bc + "Impl1"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryImplClass(), bc + "FactoryImpl1"));
            assertTrue(valuesAreEqual(cd.getConnectionFactoryInterface(), bc + "Factory1"));
            checkProperties(cd.getConfigProperties(), "Byte", "2", "Short", "1");

         }
      }

      assertEquals(TransactionSupportEnum.LocalTransaction, out.getTransactionSupport());

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
         ActivationSpecImpl as = (ActivationSpecImpl) ml.getActivationspec();
         assertTrue(valuesAreEqual(as.getActivationspecClass(), pack + ".AnnoActivationSpec"));
         checkProperties(as.getConfigProperties(), "Character", "C", "Double", "0.5");
         assertNotNull(as.getRequiredConfigProperties());
         assertEquals(as.getRequiredConfigProperties().size(), 1);
         assertEquals(as.getRequiredConfigProperties().get(0).getConfigPropertyName().getValue(), "first");
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
            checkValues(perm.getDescriptions(), "d1", "d2");
         }
      }

      List<String> wcs = con.getRequiredWorkContexts();
      assertEquals(2, wcs.size());
      assertTrue(wcs.contains("javax.resource.spi.work.TransactionContext"));
      assertTrue(wcs.contains("javax.resource.spi.work.HintsContext"));

   }

   @Override
   protected JavaArchive createArchive()
   {
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
      jar.addPackage(pack);
      return jar;
   }


}
