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

import java.io.Serializable;

/** An InteractionSpec holds properties for driving an Interaction 
 *  with an EIS instance. An InteractionSpec is used by an Interaction
 *  to execute the specified function on an underlying EIS.
 *
 *  <p>The CCI specification defines a set of standard properties for
 *  an InteractionSpec. An InteractionSpec implementation is not 
 *  required to support a standard property if that property does 
 *  not apply to its underlying EIS.
 *
 *  <p>The InteractionSpec implementation class must provide getter and
 *  setter methods for each of its supported properties. The getter and 
 *  setter methods convention should be based on the Java Beans design
 *  pattern.
 * 
 *  <p>The standard properties are as follows:
 *  <UL>
 *     <LI>FunctionName: name of an EIS function
 *     <LI>InteractionVerb: mode of interaction with an EIS instance:
 *         SYNC_SEND, SYNC_SEND_RECEIVE, SYNC_RECEIVE
 *     <LI>ExecutionTimeout: the number of milliseconds an Interaction 
 *         will wait for an EIS to execute the specified function
 *  </UL>
 *  
 *  <p>The following standard properties are used to give hints to an 
 *  Interaction instance about the ResultSet requirements:
 *  <UL>
 *     <LI>FetchSize
 *     <LI>FetchDirection
 *     <LI>MaxFieldSize
 *     <LI>ResultSetType
 *     <LI>ResultSetConcurrency
 *  </UL>
 *
 *  <p>A CCI implementation can provide additional properties beyond
 *  that described in the InteractionSpec interface. Note that the 
 *  format and type of the additional properties is specific to an EIS 
 *  and is outside the scope of the CCI specification.
 *  
 *  <p>It is required that the InteractionSpec interface be implemented
 *  as a JavaBean for the toolability support. The properties on the 
 *  InteractionSpec implementation class should be defined through the 
 *  getter and setter methods pattern. An implementation class for 
 *  InteractionSpec interface is  required to implement the 
 *  java.io.Serializable interface.
 *
 *  @author  Rahul Sharma
 *  @version 0.8
 *  @since   0.8
 *  @see     javax.resource.cci.Interaction
**/

public interface InteractionSpec extends Serializable 
{
  
   /**Interaction Verb type: The execution of an Interaction does only a 
    * send to the target EIS instance. The input record is sent to the
    * EIS instance without any synchronous response in terms of an 
    * output Record or ResultSet.
    */
   public static final int SYNC_SEND = 0;
   
   /**Interaction Verb type: The execution of an Interaction sends a 
    * request to the EIS instance and receives response synchronously. 
    * The input record is sent to the EIS instance with the output 
    * received either as Record or CCIResultSet.
    **/
   public static final int SYNC_SEND_RECEIVE = 1;
   
   /**The execution of an Interaction results in a synchronous 
    * receive of an output Record. An example is: a session bean gets
    * a method invocation and it uses this SEND_RECEIVE form of 
    * interaction to retrieve messages that have been delivered to a 
    * message queue. 
    **/
   public static final int SYNC_RECEIVE = 2;
   
}
