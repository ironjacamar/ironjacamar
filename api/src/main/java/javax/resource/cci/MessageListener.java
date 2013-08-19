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

package javax.resource.cci;

import javax.resource.ResourceException;

/** 
 * This serves as a request-response message listener type that message
 * endpoints (message-driven beans) may implement. This allows an EIS to
 * communicate with an endpoint using a request-response style.
 *
 *  @author  Ram Jeyaraman
 *  @version 1.0
 */    
public interface MessageListener
{
   /**
    * This method allows an EIS to call a message endpoint using a 
    * request-response style communication.
    *
    * @param inputData a <code>Record</code> instance.
    *
    * @return a <code>Record</code> instance or null.
    *
    * @throws ResourceException indicates an exceptional condition.
    */
   Record onMessage(Record inputData) throws ResourceException;
}
