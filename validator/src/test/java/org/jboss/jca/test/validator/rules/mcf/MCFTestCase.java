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
package org.jboss.jca.test.validator.rules.mcf;

import org.jboss.jca.fungal.deployers.DeployException;
import org.jboss.jca.test.validator.rules.TestCaseAbstract;
import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.ValidatorException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * TestCase stressing RA related validation rules
 *
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 *
 */
public class MCFTestCase extends TestCaseAbstract
{

   /**
    * stress the MCF rule.
    *
    * @throws Throwable and expect a ValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongMCF() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("mcf_wrong.rar");
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
                     "6.5.3.2",
                     "The class must implement the javax.resource.spi.ManagedConnectionFactory interface.",
                     TestManagedConnectionFactoryWrong.class
                     .getName());
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

   }


   /**
    * stress the MCFEquals rule.
    *
    * @throws Throwable and expect a ValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongEqualsMCF() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("mcf_equals_wrong.rar");

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
                     "6.5.3.2",
                     "A ManagedConnectionFactory must implement a \"public boolean equals(Object)\" method.",
                     TestManagedConnectionFactoryEqualsWrong.class.getName());
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
    * stress the MCFEquals rule.
    *
    * @throws Throwable and expect a ValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongHashCodeMCF() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("mcf_hashcode_wrong.rar");

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
                     "6.5.3.2",
                     "A ManagedConnectionFactory must implement a \"public int hashCode()\" method.",
                     TestManagedConnectionFactoryHashCodeWrong.class.getName());
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
    * stress the MCFEquals rule and MCFHashCode rule, demonstrating also how DeployValidatorException can chains
    * more than one failure
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithFailuresChaininCaseOfWrongEqualsAndHashCodeMCF() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("mcf_equals_and_hashcode_wrong.rar");

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

         final Failure failureRAOne =
               new Failure(Severity.ERROR,
                     "6.5.3.2",
                     "A ManagedConnectionFactory must implement a \"public boolean equals(Object)\" method.",
                     TestManagedConnectionFactoryEqualsAndHashCodeWrong.class.getName());
         final Failure failureRATwo =
               new Failure(Severity.ERROR,
                     "6.5.3.2",
                     "A ManagedConnectionFactory must implement a \"public int hashCode()\" method.",
                     TestManagedConnectionFactoryEqualsAndHashCodeWrong.class.getName());

         assertThat(dve.getFailures(), notNullValue());
         assertThat(dve.getFailures(), hasItem(equalTo(failureRAOne)));
         assertThat(dve.getFailures(), hasItem(equalTo(failureRATwo)));
         assertThat(dve.getFailures().size(), is(2));
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
    * stress the MCFConstructor rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongConstructorRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("mcf_constructor_wrong.rar");

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
               new Failure(Severity.ERROR, "19.3", "A ManagedConnectionFactory must have a default constructor", null);
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
    * stress the MCFConfigProperties rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test
   public void deployerShouldPrintIntoLogFileWithWrongPropertyRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("mcf_property_wrong.rar");
      System.setProperty("jboss.jca.home", ".");
      File directory = new File("./log");
      directory.mkdir();
      BufferedReader input = null;

      try
      {
         //when
         embedded.deploy(archive);
         assertThat(directory.listFiles().length, is(1));
         assertThat(directory.listFiles()[0].getName(), is("mcf_property_wrong.rar.log"));
         input = new BufferedReader(new FileReader(directory.listFiles()[0]));
         assertThat(input.readLine(), is("Severity: WARNING"));
         assertThat(input.readLine(), is("Section: 20.7"));
         assertThat(input.readLine(), is("Description: Invalid config-property-type for ManagedConnectionFactory."));
         assertThat(input.readLine(), is("Code: Class: "
               + "org.jboss.jca.test.validator.rules.mcf.TestManagedConnectionFactoryPropertyWrong "
               + "Property: StringRAR Type: void"));

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
    * Deploy a correct RA not violating any rule.
    *
    * @throws Throwable and doesn't expect any Exception
    *
    */
   @Test
   public void deployerShouldNotThrowDeployExceptionWithRightRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("all_right.rar");

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
