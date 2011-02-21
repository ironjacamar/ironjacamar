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
package org.jboss.jca.validator.rules.ao;

import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.ValidatorException;
import org.jboss.jca.validator.rules.TestCaseAbstract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import com.github.fungal.spi.deployers.DeployException;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * TestCase stressing AO related validation rules
 *
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 *
 */
public class AOTestCase extends TestCaseAbstract
{


   /**
    * stress the AORA rule for Serialization constraint
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongSerializableAdminObject() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ao_serializable_wrong.rar");

      try
      {
         //when
         embedded.deploy(archive);
      }
      catch (DeployException de)
      {
         //then
         ValidatorException dve = null;
         if (de.getCause() != null && de.getCause() instanceof ValidatorException)
         {
            dve = (ValidatorException) de.getCause();
         }
         else
         {
            //fail
            de.printStackTrace();
            throw de;
         }

         final Failure failureRA =
               new Failure(Severity.ERROR,
                     "13.4.2.3",
                     "An AdminObject must implement javax.resource.Referenceable and java.io.Serializable "
                           + "interfaces if javax.resource.spi.ResourceAdapterAssociation is implemented Code",
                     org.jboss.jca.validator.rules.ao.TestAdminObjectSerializableWrong.class.getCanonicalName());
         assertThat(dve.getFailures(), notNullValue());
         assertThat(dve.getFailures(), hasItem(equalTo(failureRA)));
         assertThat(dve.getFailures().size(), is(1));
         //success
         throw dve;
      }
      finally
      {
         embedded.undeploy(archive);
      }
      //no exception = fail!
      fail();
   }

   /**
    * stress the AORA rule for Referenceable constraint
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongReferenceableAdminObject() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ao_referenceable_wrong.rar");
      try
      {
         //when
         embedded.deploy(archive);
      }
      catch (DeployException de)
      {
         //then
         ValidatorException dve = null;
         if (de.getCause() != null && de.getCause() instanceof ValidatorException)
         {
            dve = (ValidatorException) de.getCause();
         }
         else
         {
            //fail
            de.printStackTrace();
            throw de;
         }
         final Failure failureRA =
               new Failure(Severity.ERROR,
                     "13.4.2.3",
                     "An AdminObject must implement javax.resource.Referenceable and java.io.Serializable "
                           + "interfaces if javax.resource.spi.ResourceAdapterAssociation is implemented Code",
                     org.jboss.jca.validator.rules.ao.TestAdminObjectReferenceableWrong.class.getCanonicalName());
         assertThat(dve.getFailures(), notNullValue());
         assertThat(dve.getFailures(), hasItem(equalTo(failureRA)));
         assertThat(dve.getFailures().size(), is(1));
         //success
         throw dve;
      }
      finally
      {
         embedded.undeploy(archive);
      }
      //no exception = fail!
      fail();
   }


   /**
    * stress the AOConstructor rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongConstructor() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ao_constructor_wrong.rar");

      try
      {
         //when
         embedded.deploy(archive);
      }
      catch (DeployException de)
      {
         //then
         ValidatorException dve = null;
         if (de.getCause() != null && de.getCause() instanceof ValidatorException)
         {
            dve = (ValidatorException) de.getCause();
         }
         else
         {
            //fail
            de.printStackTrace();
            throw de;
         }

         final Failure failureRA =
               new Failure(Severity.ERROR, "19.3", "An AdminObject must have a default constructor",
                     null);
         assertThat(dve.getFailures(), notNullValue());
         assertThat(dve.getFailures(), hasItem(equalTo(failureRA)));
         assertThat(dve.getFailures().size(), is(1));
         //success
         throw dve;
      }
      finally
      {
         embedded.undeploy(archive);
      }
      //no exception = fail!
      fail();
   }

   /**
    * stress the AOConfigProperties rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test
   public void deployerShouldPrintIntoLogFileWithWrongPropertyAS() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ao_property_wrong.rar");
      System.setProperty("iron.jacamar.home", ".");
      File directory = new File("./log");
      directory.mkdir();
      BufferedReader input = null;

      try
      {
         //when
         embedded.deploy(archive);
         assertThat(directory.listFiles().length, is(1));
         assertThat(directory.listFiles()[0].getName(), is("ao_property_wrong.rar.log"));
         input = new BufferedReader(new FileReader(directory.listFiles()[0]));
         assertThat(input.readLine(), is("Severity: WARNING"));
         assertThat(input.readLine(), is("Section: 20.7"));
         assertThat(input.readLine(), is("Description: Invalid config-property-type for AdminObject."));
         assertThat(input.readLine(), is("Code: Class: "
               + "org.jboss.jca.validator.rules.ao.TestAdminObjectPropertyWrong "
               + "Property: myStringProperty Type: int"));

         assertThat(input.readLine(), is((String) null));
      }
      finally
      {
         if (input != null)
         {
            input.close();
         }
         deleteDirectory(directory);
      }
      embedded.undeploy(archive);
   }

   /**
    * Deploy a correct rar with admin object without RAReference
    *
    * @throws Throwable and doesn't expect any Exception
    *
    */
   @Test
   public void deployerShouldNotThrowDeployExceptionWithAllRightWithoutRAReference() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("all_right_aonora.rar");

      try
      {
         //when
         embedded.deploy(archive);
         //then
         //No Exception
      }
      finally
      {
         embedded.undeploy(archive);
      }

   }

   /**
    * Deploy a correct rar with admin object without RAReference
    *
    * @throws Throwable and doesn't expect any Exception
    *
    */
   @Test
   public void deployerShouldNotThrowDeployExceptionWithAllRightWithRAReference() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("all_right_aora.rar");

      try
      {
         //when
         embedded.deploy(archive);
         //then
         //No Exception
      }
      finally
      {
         embedded.undeploy(archive);
      }

   }

}
