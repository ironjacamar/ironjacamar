/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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
package org.jboss.jca.test.deployers.spec.rars.ra16inoutijmultianno;

import javax.resource.spi.ConfigProperty;

/**
 * 
 * A TestManagedConnectionFactoryParent.
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 */
public abstract class TestManagedConnectionFactoryParent
{
   /** byteProperty */
   @ConfigProperty(defaultValue = "2")
   protected Byte byteProperty;

   /**
    * Set byteProperty
    * 
    * @param byteProperty The value
    */
   public void setByteProperty(Byte byteProperty)
   {
      this.byteProperty = byteProperty;
   }

   /**
    * Get byteProperty
    * 
    * @return The value
    */
   public Byte getByteProperty()
   {
      return byteProperty;
   }

}
