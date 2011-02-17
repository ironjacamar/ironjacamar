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
package org.jboss.jca.core.security;

import org.jboss.jca.core.security.subjectfactory.BaseCciConnectionFactory;
import org.jboss.jca.core.security.subjectfactory.TestManagedConnectionFactory;
import org.jboss.jca.embedded.arquillian.ArquillianJCATestUtils;

import javax.annotation.Resource;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class SubjectFactoryInjectionTestCase
{
   //-------------------------------------------------------------------------------------||
   //---------------------- GIVEN --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||
   /**
    * Define the deployment
    * @return The deployment archive
    * @throws Exception in case of errors
    */
   @Deployment
   public static ResourceAdapterArchive createDeployment() throws Exception
   {
      String archiveName = "subjectfactory.rar";
      String packageName = "org.jboss.jca.core.security.subjectfactory";
      ResourceAdapterArchive raa = ArquillianJCATestUtils.buidShrinkwrapRa(archiveName, packageName);
      raa.addManifestResource(archiveName + "/META-INF/ra.xml", "ra.xml");
      raa.addManifestResource(archiveName + "/META-INF/ironjacamar.xml", "ironjacamar.xml");

      return raa;
   }

   //-------------------------------------------------------------------------------------||
   //---------------------- WHEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   @Resource(mappedName = "java:/eis/SubjectFactoryInjection")
   private BaseCciConnectionFactory connectionFactory;

   //-------------------------------------------------------------------------------------||
   //---------------------- THEN  --------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Basic
    * @exception Throwable Thrown if case of an error
    */
   @Test
   public void testBasic() throws Throwable
   {
      //given
      assertNotNull(connectionFactory);
      Subject expectedSubject = new Subject();
      PasswordCredential credential = new PasswordCredential("username", "password".toCharArray());
      expectedSubject.getPrivateCredentials().add(credential);

      //when
      connectionFactory.getConnection();
      //then

      assertThat(TestManagedConnectionFactory.getSubjectUsed().equals(expectedSubject), is(true));
      //generated connetionfactory createManagedConnection method called with Subject equalsto expected subject
   }

}

