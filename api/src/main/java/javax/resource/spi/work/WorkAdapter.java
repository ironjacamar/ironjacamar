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

package javax.resource.spi.work;

/**
 * This class is provided as a convenience for easily creating 
 * <code>WorkListener</code> instances by extending this class
 * and overriding only those methods of interest.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public class WorkAdapter implements WorkListener 
{
   
   /** 
    * Invoked when a <code>Work</code> instance has been accepted.
    * @param e The work event
    */
   public void workAccepted(WorkEvent e) 
   {
   }
   
   /** 
    * Invoked when a <code>Work</code> instance has been rejected.
    * @param e The work event
    */
   public void workRejected(WorkEvent e) 
   {
   }
   
   /** 
    * Invoked when a <code>Work</code> instance has started execution.
    * This only means that a thread has been allocated.
    * @param e The work event
    */
   public void workStarted(WorkEvent e) 
   {
   }
   
   /** 
    * Invoked when a <code>Work</code> instance has completed execution.
    * @param e The work event
    */
   public void workCompleted(WorkEvent e) 
   {
   }
}
