/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package javax.resource.spi;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * This represents a resource adapter instance and contains operations for
 * lifecycle management and message endpoint setup. A concrete implementation
 * of this interface is required to be a JavaBean.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface ResourceAdapter 
{
   // lifecycle operations
   
   /**
    * This is called when a resource adapter instance is bootstrapped. This
    * may be during resource adapter deployment or application server startup.
    * This is a startup notification from the application server, and this 
    * method is called by an application server thread. The application server
    * thread executes in an unspecified context.
    *
    * <p>During this method call a ResourceAdapter JavaBean is
    * responsible for initializing the resource adapter
    * instance. Any exception thrown during this method
    * call causes the application server to abort the bootstrap procedure 
    * for this specific resource adapter instance.
    *
    * @param ctx a bootstrap context containing references to
    * useful facilities that could be used by a resource adapter instance.
    *
    * @throws ResourceAdapterInternalException indicates bootstrap failure.
    * The resource adapter instance is unusable and must be discarded.
    */
   void start(BootstrapContext ctx) throws ResourceAdapterInternalException; 
   
   /**
    * This is called when a resource adapter instance is undeployed or
    * during application server shutdown. This is a shutdown notification 
    * from the application server, and this method is called by an 
    * application server thread.  The application server
    * thread executes in an unspecified context.
    *
    * <p>During this method call, a ResourceAdapter 
    * JavaBean is responsible for performing an orderly shutdown of the
    * resource adapter instance. Any exception thrown by this 
    * method call does not alter the 
    * processing of the application server shutdown or resource 
    * adapter undeployment that caused this method call. The application 
    * server may log the exception information for error reporting purposes.
    */
   void stop(); 
   
   // message endpoint setup operations
   
   /**
    * This is called during the activation of a message endpoint. This causes
    * the resource adapter instance to do the necessary setup (ie., setup
    * message delivery for the message endpoint with a message provider).
    * Note that message delivery to the message endpoint might start even 
    * before this method returns.
    *
    * <p>Endpoint activation is deemed successful only when this method 
    * completes successfully without throwing any exceptions.
    *
    * @param endpointFactory a message endpoint factory instance.
    *
    * @param spec an activation spec JavaBean instance.
    *
    * @throws ResourceException Thrown if an error occurs
    * @throws javax.resource.NotSupportedException indicates message endpoint 
    * activation rejection due to incorrect activation 
    * setup information.
    */
   void endpointActivation(MessageEndpointFactory endpointFactory, 
                           ActivationSpec spec) throws ResourceException;
   
   /**
    * This is called when a message endpoint is deactivated. The instances
    * passed as arguments to this method call should be identical to those
    * passed in for the corresponding </code>endpointActivation</code> call.
    * This causes the resource adapter to stop delivering messages to the
    * message endpoint. 
    *
    * <p>Any exception thrown by this method is ignored. After
    * this method call, the endpoint is deemed inactive.
    *
    * @param endpointFactory a message endpoint factory instance.
    *
    * @param spec an activation spec JavaBean instance.
    */
   void endpointDeactivation(MessageEndpointFactory endpointFactory, 
                             ActivationSpec spec);
   
   /**
    * This method is called by the application server during crash recovery.
    * This method takes in an array of <code>ActivationSpec</code> JavaBeans 
    * and returns an array of <code>XAResource</code> objects each of which 
    * represents a unique resource manager.
    *
    * The resource adapter may return null if it does not implement the 
    * <code>XAResource</code> interface. Otherwise, it must return an array 
    * of <code>XAResource</code> objects, each of which represents a unique 
    * resource manager that was used by the endpoint applications. 
    * 
    * The application server uses the <code>XAResource</code> objects to 
    * query each resource manager for a list of in-doubt transactions.
    * It then completes each pending transaction by sending the commit 
    * decision to the participating resource managers.
    * 
    * @param specs an array of <code>ActivationSpec</code> JavaBeans each of 
    * which corresponds to an deployed endpoint application that was 
    * active prior to the system crash.
    *
    * @return an array of <code>XAResource</code> objects each of which 
    * represents a unique resource manager.
    *
    * @throws ResourceException generic exception if operation fails due to an
    * error condition.
    */
   XAResource[] getXAResources(ActivationSpec[] specs) 
      throws ResourceException;
}
