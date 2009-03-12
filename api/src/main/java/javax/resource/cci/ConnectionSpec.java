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

package javax.resource.cci;

/** ConnectionSpec is used by an application component to pass 
 *  connection request-specific properties to the ConnectionFactory.
 *  getConnection method.
 *
 *  <p>It is recommended that the ConnectionSpec interface be 
 *  implemented as a JavaBean to support tools. The properties 
 *  on the ConnectionSpec implementation class must be defined 
 *  through the getter and setter methods pattern. 
 *  
 *  <p>The CCI specification defines a set of standard properties 
 *  for an ConnectionSpec. The properties are defined either on
 *  a derived interface or an implementation class of an empty
 *  ConnectionSpec interface. In addition, a resource adapter may 
 *  define additional properties specific to its underlying EIS.
 *  
 *  @author  Rahul Sharma
 *  @version 1.0 Public Draft 1
 *  @see     javax.resource.cci.ConnectionFactory
 **/

public interface ConnectionSpec 
{

}
