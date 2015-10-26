/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the Eclipse Public License 1.0 as
 * published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse
 * Public License for more details.
 *
 * You should have received a copy of the Eclipse Public License 
 * along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ironjacamar.embedded.byteman;

import org.ironjacamar.core.api.deploymentrepository.Deployment;
import org.ironjacamar.core.api.deploymentrepository.DeploymentRepository;

import java.util.Collection;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Byteman support
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
@RunWith(IronJacamarWithByteman.class)
@BMRules({@BMRule(name = "Throw exception on getDeployments",
                  isInterface = true,
                  targetClass = "org.ironjacamar.core.api.deploymentrepository.DeploymentRepository",
                  targetMethod = "getDeployments",
                  action = "throw new java.lang.RuntimeException()")})
public class BytemanTestCase
{
   /** The deployment repository */
   @Inject
   private static DeploymentRepository dr;
   
   /**
    * Test that the Byteman rule is injected
    * @throws Throwable In case of an error
    */
   @Test
   public void testRule() throws Throwable
   {
      assertNotNull(dr);
      try
      {
         Collection<Deployment> deployments = dr.getDeployments();
         fail("Byteman rule not injected");
      }
      catch (RuntimeException re)
      {
         // Expected
      }
   }
}
