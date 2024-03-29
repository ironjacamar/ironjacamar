/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2012, Red Hat Inc, and individual contributors
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
package org.jboss.jca.deployers.test.rars.inout;

import java.io.Serializable;

import jakarta.resource.Referenceable;

/**
 * SimpleAdminObject
 *
 * @version $Revision: $
 */
public interface SimpleAdminObject1 extends Referenceable, Serializable
{

   /** 
    * Set first
    * @param first The value
    */
   public void setFirst(String first);

   /** 
    * Get first
    * @return The value
    */
   public String getFirst();

   /** 
    * Set second
    * @param second The value
    */
   public void setSecond(Double second);

   /** 
    * Get second
    * @return The value
    */
   public Double getSecond();


}
