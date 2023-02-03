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
package org.jboss.jca.validator.rules.ra;

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

import jakarta.resource.spi.ResourceAdapter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * TestCase stressing RA related validation rules
 *
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 *
 */
public class RATestCase extends TestCaseAbstract
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
   //FIXME validator.properties not transformed
   public void deployerShouldThrowDeployExceptionWithWrongRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_wrong.rar");
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
               new Failure(Severity.ERROR, "5.3.1", "A ResourceAdapter must implement " + "the jakarta.resource.spi."
                     + "ResourceAdapter interface.", TestResourceAdapterWrong.class.getName());
         assertThat(dve.getFailures(), notNullValue());
         //FIXME validator.properties transformation doesn't work
         if(!ResourceAdapter.class.getPackage().toString().contains("jakarta")) {
            assertThat(dve.getFailures(), hasItem(equalTo(failureRA)));
            assertThat(dve.getFailures().size(), is(1));
         }
         //success
         throw dve;
      }
      finally
      {
         embedded.undeploy(archive);
      }

   }

   /**
    * stress the RAHashCode rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongHashCodeRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_hashcode_wrong.rar");

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
                     "19.4.2",
                     "A ResourceAdapter must implement a \"public int hashCode()\" method.",
                     TestResourceAdapterHashCodeWrong.class.getName());
         assertThat(dve.getFailures(), notNullValue());
         assertThat(dve.getFailures(), hasItem(equalTo(failureRA)));
         assertThat(dve.getFailures().size(), is(1));
         //success//success
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
    * stress the RANull rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithNullRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_null_wrong.rar");

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

         assertThat(dve.getFailures(), notNullValue());
         assertThat(dve.getFailures().size(), is(1));

         final Failure checkRA =
               new Failure(Severity.ERROR, "5.3.1", "A ResourceAdapter must be a non-null instance.", null);

         Failure failureRA = dve.getFailures().iterator().next();

         assertEquals(checkRA.getSeverity(), failureRA.getSeverity());
         assertEquals(checkRA.getSection(), failureRA.getSection());
         assertEquals(checkRA.getDescription(), failureRA.getDescription());
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
    * stress the RAEquals rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongEqualsRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_equals_wrong.rar");

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
                     "19.4.2",
                     "A ResourceAdapter must implement a \"public boolean equals(Object)\" method.",
                     TestResourceAdapterEqualsWrong.class.getName());
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
    * stress the RAEquals rule and RAHashCode rule, demonstrating also how DeployValidatorException can chains
    * more than one failure
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithFailuresChaininCaseOfWrongEqualsAndHashCodeRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_equals_and_hashcode_wrong.rar");

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
                     "19.4.2",
                     "A ResourceAdapter must implement a \"public boolean equals(Object)\" method.",
                     TestResourceAdapterEqualsAndHashCodeWrong.class.getName());
         final Failure failureRATwo =
               new Failure(Severity.ERROR,
                     "19.4.2",
                     "A ResourceAdapter must implement a \"public int hashCode()\" method.",
                     TestResourceAdapterEqualsAndHashCodeWrong.class.getName());

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
    * stress the RAConstructor rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongConstructorRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_constructor_wrong.rar");

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
               new Failure(Severity.ERROR, "19.3", "A ResourceAdapter must have a default constructor", null);
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
    * stress the RAConfigProperties rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test
   public void deployerShouldPrintIntoLogFileWithWrongPropertyRA() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("ra_property_wrong.rar");
      System.setProperty("iron.jacamar.home", ".");
      File directory = new File("./log");
      directory.mkdir();
      BufferedReader input = null;

      try
      {
         //when
         embedded.deploy(archive);
         assertThat(directory.listFiles().length, is(1));
         assertThat(directory.listFiles()[0].getName(), is("ra_property_wrong.log"));
         input = new BufferedReader(new FileReader(directory.listFiles()[0]));
         assertThat(input.readLine(), is("Severity: WARNING"));
         assertThat(input.readLine(), is("Section: 20.7"));
         assertThat(input.readLine(), is("Description: Invalid config-property-type for ResourceAdapter."));
         assertThat(input.readLine(), is("Code: Class: "
               + "org.jboss.jca.validator.rules.ra.TestResourceAdapterPropertyWrong "
               + "Property: StringRAR Type: int"));

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
