/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.core.workmanager.spec.chapter10.common;

import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.work.Work;


/**
 * SimpleWork.

 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
public class AssociationWork implements Work, ResourceAdapterAssociation
{
   /** The resource adapter */
   private ResourceAdapter ra;
   

   /**
    * AssociationWork
    */
   public AssociationWork()
   {
   }
   
   /**
    * Get the resource adapter
    *
    * @return The handle
    */
   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }

   /**
    * Set the resource adapter
    *
    * @param ra The handle
    */
   public void setResourceAdapter(ResourceAdapter ra)
   {
      this.ra = ra;
   }

   /**
    * The <code>WorkManager</code> might call this method to hint the
    * active <code>Work</code> instance to complete execution as soon as 
    * possible. This would be called on a seperate thread other than the
    * one currently executing the <code>Work</code> instance.
    */
   public void release()
   {
   }

   /**
    * When an object implementing interface <code>Runnable</code> is used 
    * to create a thread, 
    *
    * @see     java.lang.Thread#run()
    */
   public void run()
   {

   }
}
