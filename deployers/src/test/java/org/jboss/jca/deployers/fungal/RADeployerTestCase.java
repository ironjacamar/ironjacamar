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
package org.jboss.jca.deployers.fungal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import org.jboss.jca.validator.Failure;
import org.jboss.jca.validator.FailureHelper;
import org.jboss.jca.validator.Severity;
import org.jboss.jca.validator.Validator;
import org.junit.Test;

/**
 * @author <a href="mailto:stefano.maestri@javalinux.it">Stefano Maestri</a>
 *
 */
public class RADeployerTestCase
{

   /**
    * Test method for
    * {@link
    * org.jboss.jca.deployers.fungal.RADeployer#validateArchive(java.net.URL, java.util.List, java.util.List)}.
    * given: archive validation set to false
    * when: validateArchive is called
    * then:return null
    *
    * @throws Throwable in case of error during test in case of error during test
    */
   @Test
   public void validateArchiveShouldReturnNullIfNoValidationIsPerformed() throws Throwable
   {
      //given
      RADeployer deployer = new RADeployer();
      deployer.setArchiveValidation(false);
      //when
      List<Failure> returnValue = deployer.validateArchive(null, null);
      //then
      assertThat(returnValue, is((List) null));
   }

   /**
    * printFailures should Throw Exception for WARNING level failures
    *
    * @throws Throwable in case of error during test
    */
   @Test
   public void printFailuresLogShouldReturnNotEmptyStringForWarning() throws Throwable
   {
      //given
      RADeployer deployer = new RADeployer();
      File mockedDirectory = mock(File.class);
      given(mockedDirectory.exists()).willReturn(false);
      Failure failure = mock(Failure.class);
      given(failure.getSeverity()).willReturn(Severity.WARNING);
      List<Failure> failures = Arrays.asList(failure);
      FailureHelper fh = mock(FailureHelper.class);
      given(fh.asText((ResourceBundle) anyObject())).willReturn("myText");

      deployer.setArchiveValidationFailOnWarn(true);

      //when
      String returnValue = deployer.printFailuresLog(null, mock(Validator.class), failures, mockedDirectory, fh);

      //then
      assertThat(returnValue, is("myText"));

   }

   /**
    * printFailures should Throw Exception for ERROR level failures
    *
    * @throws Throwable in case of error during test
    */
   @Test
   public void printFailuresLogShouldReturnNotEmptyStringForError() throws Throwable
   {
      //given
      RADeployer deployer = new RADeployer();
      File mockedDirectory = mock(File.class);
      given(mockedDirectory.exists()).willReturn(false);
      Failure failure = mock(Failure.class);
      given(failure.getSeverity()).willReturn(Severity.ERROR);
      List<Failure> failures = Arrays.asList(failure);
      FailureHelper fh = mock(FailureHelper.class);
      given(fh.asText((ResourceBundle) anyObject())).willReturn("myText");

      deployer.setArchiveValidationFailOnError(true);

      //when
      String returnValue = deployer.printFailuresLog(null, mock(Validator.class), failures, mockedDirectory, fh);

      //then
      assertThat(returnValue, is("myText"));
   }

   /**
    * printFailures should Throw Exception for ERROR level failures if directory log exist
    *
    * @throws Throwable in case of error during test
    */
   @Test
   public void printFailuresLogShouldReturnInformativeStringForErrorAndCreateLog() throws Throwable
   {
      BufferedReader input = null;

      //given
      RADeployer deployer = new RADeployer();
      deployer.setArchiveValidationFailOnError(true);
      Failure failure = mock(Failure.class);
      given(failure.getSeverity()).willReturn(Severity.ERROR);
      List<Failure> failures = Arrays.asList(failure);
      FailureHelper fh = mock(FailureHelper.class);
      given(fh.asText((ResourceBundle) anyObject())).willReturn("myText");
      String fileName = "myFileName.rar";

      File directory = new File("tmp-ratest");
      directory.mkdir();
      //when
      String returnValue = deployer.printFailuresLog(fileName, mock(Validator.class), failures, directory, fh);

      //then
      assertThat(returnValue, is("Validation failures - see: " + directory.getAbsolutePath() + File.separator
            + "myFileName.rar.log"));
      assertThat(directory.listFiles().length, is(1));
      assertThat(directory.listFiles()[0].getName(), is("myFileName.rar.log"));
      try
      {
         input = new BufferedReader(new FileReader(directory.listFiles()[0]));
         assertThat(input.readLine(), is("myText"));
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
   }

   private boolean deleteDirectory(File path)
   {
      if (path.exists())
      {
         File[] files = path.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            if (files[i].isDirectory())
            {
               deleteDirectory(files[i]);
            }
            else
            {
               files[i].delete();
            }
         }
      }
      return (path.delete());
   }
}
