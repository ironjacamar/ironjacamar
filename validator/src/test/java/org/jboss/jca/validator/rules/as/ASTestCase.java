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
package org.jboss.jca.validator.rules.as;

import org.jboss.jca.fungal.deployers.DeployException;
import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.ValidatorException;
import org.jboss.jca.validator.rules.TestCaseAbstract;

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
 * TestCase stressing AS related validation rules
 *
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 *
 */
public class ASTestCase extends TestCaseAbstract
{
   /**
    * stress the ASConstructor rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test(expected = ValidatorException.class)
   public void deployerShouldThrowDeployExceptionWithWrongConstructor() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("as_constructor_wrong.rar");

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
               new Failure(Severity.ERROR, "19.3", "An ActivationSpec must have a default constructor",
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
    * stress the ASConfigProperties rule.
    *
    * @throws Throwable and expect a DeployValidatorException
    *
    */
   @Test
   public void deployerShouldPrintIntoLogFileWithWrongPropertyAS() throws Throwable
   {
      //given
      ResourceAdapterArchive archive = getArchive("as_property_wrong.rar");
      System.setProperty("jboss.jca.home", ".");
      File directory = new File("./log");
      directory.mkdir();
      BufferedReader input = null;

      try
      {
         //when
         embedded.deploy(archive);
         assertThat(directory.listFiles().length, is(1));
         assertThat(directory.listFiles()[0].getName(), is("as_property_wrong.rar.log"));
         input = new BufferedReader(new FileReader(directory.listFiles()[0]));
         assertThat(input.readLine(), is("Severity: WARNING"));
         assertThat(input.readLine(), is("Section: 20.7"));
         assertThat(input.readLine(), is("Description: Invalid config-property-type for ActivationSpec."));
         assertThat(input.readLine(), is("Code: Class: "
               + "org.jboss.jca.validator.rules.as.TestActivationSpecWrongProperties "
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
