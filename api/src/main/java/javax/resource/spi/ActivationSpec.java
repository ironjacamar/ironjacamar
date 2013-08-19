/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

package javax.resource.spi;

/**
 * This interface serves as a marker. An instance of an ActivationSpec must be a
 * JavaBean and must be serializable. This holds the activation configuration
 * information for a message endpoint.
 * 
 * @version 1.0
 * @author Ram Jeyaraman
 */
public interface ActivationSpec extends ResourceAdapterAssociation 
{
   
   /**
    * This method may be called by a deployment tool to validate the overall
    * activation configuration information provided by the endpoint deployer.
    * This helps to catch activation configuration errors earlier on without
    * having to wait until endpoint activation time for configuration
    * validation. The implementation of this self-validation check behavior is
    * optional.
    * 
    * Note: As of Java EE Connectors 1.6 specification, resource adapter
    *       implementations are recommended to use the annotations or the
    *       XML validation deployment descriptor facilities defined by
    *       the Bean Validation specification to express their validation
    *       requirements of its configuration properties to the
    *       application server.
    *
    * @throws InvalidPropertyException indicates invalid configuration property settings.
    */         
   void validate() throws InvalidPropertyException;
}
