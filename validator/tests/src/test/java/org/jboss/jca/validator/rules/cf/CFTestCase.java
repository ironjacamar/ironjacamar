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
package org.jboss.jca.validator.rules.cf;

import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.ValidatorException;
import org.jboss.jca.validator.rules.TestCaseAbstract;

import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import com.github.fungal.spi.deployers.DeployException;

import org.junit.Test;

import jakarta.resource.spi.ResourceAdapter;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * TestCase stressing CF related validation rules
 *
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 *
 */
public class CFTestCase extends TestCaseAbstract
{

   /**
    * stress the CF rule.
    *
    * @throws Throwable and expect a ValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithNullCF() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("cf_null_wrong.rar");
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
               new Failure(Severity.ERROR, "17.5.1.1", "ConnectionFactory must be a non-null value", null);
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
    * stress the CFReferenciable rule.
    *
    * @throws Throwable and expect a ValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongReferenciable() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("cf_referenceable_wrong.rar");

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
                     "17.5.1.1",
                     "ConnectionFactory must implement jakarta.resource.Referenceable",
                     ConnectionFactoryWithWrongReferenceable.class.getName());
         assertThat(dve.getFailures(), notNullValue());
         //FIXME validator.properties not transformed
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
      //no exception = fail!
      fail();
   }

   /**
    * stress the CFSerializable rule.
    *
    * @throws Throwable and expect a ValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongSerializableCF() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("cf_serializable_wrong.rar");

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
                     "17.5.1.1",
                     "ConnectionFactory must implement java.io.Serializable",
                     ConnectionFactoryWithWrongSerializable.class.getName());
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
    * stress the CFConstructor rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongConstructor() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("cf_constructor_wrong.rar");

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
               new Failure(Severity.ERROR, "17.5.1.1", "A ConnectionFactory must have a default constructor ",
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
    * Deploy a correct RA not violating any rule.
    *
    * @throws Throwable and doesn't expect any Exception
    *
    */
   @Test
   public void deployerShouldNotThrowDeployExceptionWithAllRight() throws Throwable
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
