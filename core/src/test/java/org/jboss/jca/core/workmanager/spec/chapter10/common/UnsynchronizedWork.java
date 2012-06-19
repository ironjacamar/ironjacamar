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

import javax.resource.spi.work.Work;

/**
 * Unsynchronized work instance.

 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 * @version $Revision: $
 */
public class UnsynchronizedWork implements Work
{
   private boolean ran;

   private boolean released;

   /**
    * Constructor.
    */
   public UnsynchronizedWork()
   {
      setRan(false);
      setReleased(false);
   }

   /**
    * Unsynchronized release method
    */
   public void release()
   {
      synchronized (this)
      {
         setRan(true);
      }
   }

   /**
    * Unsynchronized run method
    */
   public void run()
   {
      synchronized (this)
      {
         setReleased(true);
      }
   }

   /**
    * setter for ran variable
    * @param v - boolean value of ran
    */
   public void setRan(boolean v)
   {
      ran = v;
   }

   /**
    * getter for ran variable
    * @return  value of ran
    */
   public boolean isRan()
   {
      return ran;
   }

   /**
    * setter for released variable
    * @param v - boolean value of released
    */
   public void setReleased(boolean v)
   {
      released = v;
   }

   /**
    * getter for released variable
    * @return  value of released
    */
   public boolean isReleased()
   {
      return released;
   }
}
