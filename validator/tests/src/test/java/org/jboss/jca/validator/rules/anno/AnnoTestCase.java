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
package org.jboss.jca.validator.rules.anno;

import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.ValidatorException;
import org.jboss.jca.validator.rules.TestCaseAbstract;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import com.github.fungal.spi.deployers.DeployException;

import org.junit.Ignore;
import org.junit.Test;

import jakarta.resource.spi.ResourceAdapter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

import static org.junit.Assert.assertThat;

import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * TestCase stressing validation rules against annotated resources
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * 
 */
public class AnnoTestCase extends TestCaseAbstract
{

   // --------------------------------------------------------------------------------||
   // Class Members ------------------------------------------------------------------||
   // --------------------------------------------------------------------------------||

   /**
    * stress the RA rule.
    * 
    * @throws Throwable and expect a DeployValidatorException
    * 
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongRA() throws Throwable
   {
      // given
      ResourceAdapterArchive archive = getAnnoArchive(AnnoResourceAdapter1.class);
      try
      {
         // when
         embedded.deploy(archive);
      }
      catch (DeployException de)
      {
         // then
         ValidatorException dve = null;
         if (de.getCause() != null && de.getCause() instanceof ValidatorException)
         {
            dve = (ValidatorException) de.getCause();
         }
         else
         {
            // fail
            de.printStackTrace();
            throw de;
         }

         final Failure failureRA = new Failure(Severity.ERROR, "5.3.1", "A ResourceAdapter must implement "
                                                                        + "the jakarta.resource.spi."
                                                                        + "ResourceAdapter interface.",
                                               AnnoResourceAdapter1.class.getName());
         assertThat(dve.getFailures(), notNullValue());
         //FIXME validator.properties transformation doesn't work
         if(!ResourceAdapter.class.getPackage().toString().contains("jakarta")) {
            assertThat(dve.getFailures(), hasItem(equalTo(failureRA)));
            assertThat(dve.getFailures().size(), is(1));
         }
         // success
         throw dve;
      }
      finally
      {
         embedded.undeploy(archive);
      }

   }

   /**
    * stress the RA rule.
    * 
    * @param classes to add to archive
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   public void testAnnoArchive(Class... classes) throws Throwable
   {
      // given
      ResourceAdapterArchive archive = getAnnoArchive(classes);
      try
      {
         // when
         embedded.deploy(archive);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
      finally
      {
         embedded.undeploy(archive);
      }

   }

   /**
    * Type of config-property mismatch in annotation
    * JCA 1.6 spec. page 18-11
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test(expected = Exception.class)
   public void deployerShouldThrowDeployExceptionWithTypeMismatch() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter2.class);
   }

   /**
    * Type of config-property mismatch in setter method annotation
    * JCA 1.6 spec. page 18-11
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test(expected = Exception.class)
   public void deployerShouldThrowDeployExceptionWithSetterTypeMismatch() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter3.class);
   }

   /**
    * AO implements 2 interfaces and there is't explicitly chosen interface by annotation
    * JCA 1.6 spec. page 18-16
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test(expected = Exception.class)
   @Ignore
   public void deployerShouldThrowDeployExceptionWithAOImplementing2Interfaces() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter.class, AnnoAdminObject.class, AnnoAdminObject1.class,
         AnnoAdminObjectImpl.class);

   }
   
   /**
    * AO implements 2 interfaces and there is no annotation, defining interface
    * JCA 1.6 spec. page 18-16
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test(expected = Exception.class)
   @Ignore("JBJCA-980")
   public void deployerShouldThrowDeployExceptionWithAOImplementing2InterfacesWoAnno() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter.class, AnnoAdminObject.class, AnnoAdminObject1.class,
         AnnoAdminObjectImpl1.class);

   }

   /**
    * @ConnectionDefinition annotated class, that doesn't implement ManagedConnectionFactory interface
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test(expected = Exception.class)
   public void deployerShouldThrowDeployExceptionWithConDefDoesntImplMCFInterface() throws Throwable
   {
      testAnnoArchive(AnnoConnection.class, AnnoManagedConnectionFactoryNoImpl.class, AnnoConnectionImpl.class,
         AnnoConnectionFactory.class, AnnoConnectionFactoryImpl.class);

   }

   /**
    * @ConnectionDefinitions annotated class with 2 @ConnectionDefinition
    * JCA 1.6 spec. page 18-12
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test
   public void deployerShouldDeployMutipleAnnotatedConnectionDefinitions() throws Throwable
   {
      testAnnoArchive(AnnoConnection.class, AnnoConnection1.class, AnnoManagedConnectionFactory.class,
         AnnoConnectionImpl.class, AnnoConnectionFactory.class, AnnoConnectionFactoryImpl.class,
         AnnoConnectionImpl1.class, AnnoConnectionFactory1.class, AnnoConnectionFactoryImpl1.class);

   }

   /**
    * @Activation annotated class doesn't implement ActivationSpec Interface
    * It's not required - JCA 1.6 spec. page 18-14
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test
   @Ignore("JBJCA-981")
   public void deployerShouldDeployActivationNotImplActivationSpecInterface() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter.class, AnnoActivationSpec.class, AnnoActivationSpec1.class,
         AnnoActivation.class, AnnoActivation1.class, AnnoMessageListener.class, AnnoMessageListener1.class);

   }

   /**
    * AO implements only one interfaces and there is no annotation, defining interface
    * JCA 1.6 spec. page 18-16
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test
   public void deployerShouldDeployAOWithoutInterfaceDefined() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter.class, AnnoAdminObject.class, 
         AnnoAdminObjectImpl2.class);

   }

   /**
    * AO implements 3 interfaces, including Serializable and Externalizable,
    * and there is no annotation, defining interface
    * JCA 1.6 spec. page 18-16
    * 
    * @throws Throwable and expect a Exception
    * 
    */
   @Test
   public void deployerShouldDeployAOWithDefaultInterfacesDefined() throws Throwable
   {
      testAnnoArchive(AnnoResourceAdapter.class, AnnoAdminObject.class, 
         AnnoAdminObjectImpl3.class);

   }

}
