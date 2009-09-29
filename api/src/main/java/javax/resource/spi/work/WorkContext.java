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

package javax.resource.spi.work;

import java.io.Serializable;

/**
 * This class serves as a standard mechanism for a resource adapter to propagate
 * an imported context from an enterprise information system to an application
 * server.
 * 
 * <p> A <code>Work</code> instance, that implements the
 * <code>WorkContextProvider</code>, could provide a
 * <code>List</code> of these <code>WorkContext</code> instances
 * (through the getWorkContexts() method), and have them setup as the
 * execution context by the <code>WorkManager</code> when the
 * <code>Work</code> instance gets executed.
 * 
 * The resource adapter must not make any changes to the state of the 
 * <code>WorkContext</code> after the <code>Work</code> instance corresponding 
 * to that <code>WorkContext</code> has been submitted to the <code>WorkManager</code>.
 *
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */

public interface WorkContext extends Serializable
{
   /**
    * Get the associated name of the <code>WorkContext</code>. This could
    * be used by the WorkManager and the resource adapter for debugging
    * purposes.
    * <p>
    * 
    * @return the associated name of the <code>WorkContext</code>
    */
   String getName();
   
   /**
    * Get the brief description of the role played by the
    * <code>WorkContext</code> and any other related debugging information.
    * This could be used by the WorkManager and the resource adapter for
    * debugging purposes.
    * <p>
    * 
    * @return the associated description of the <code>WorkContext</code>
    */
   String getDescription();
}
