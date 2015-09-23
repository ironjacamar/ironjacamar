/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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

package org.ironjacamar.core.workmanager.support;

import java.util.ArrayList;
import java.util.List;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;

/**
 * UniversalProviderWork allows to add contexts
 * 
 * @author <a href="mailto:vrastsel@redhat.com">Vladimir Rastseluev</a>
 *
 *
 */
public class UniversalProviderWork implements WorkContextProvider, Work
{
   private static final long serialVersionUID = 374498650817259220L;

   private List<WorkContext> ctxs;

   private boolean released;

   /**
    * constructor
    */
   public UniversalProviderWork()
   {
      setReleased(false);
   }

   /**
    * setter for relased
    * @param v the value
    */
   public void setReleased(boolean v)
   {
      released = v;
   }

   /**
    * getter
    * @return released value
    */
   public boolean isReleased()
   {
      return released;
   }

   /**
    * The <code>WorkManager</code> might call this method to hint the
    * active <code>Work</code> instance to complete execution as soon as 
    * possible. 
    */
   public void release()
   {
      setReleased(true);
   }

   /**
    * When an object implementing interface <code>Runnable</code> is used 
    * to create a thread, starting the thread causes the object's 
    * <code>run</code> method to be called in that separately executing 
    * thread. 
    */
   public void run()
   {
   }

   /**
    * Gets an instance of <code>WorkContexts</code> that needs to be used
    * by the <code>WorkManager</code> to set up the execution context while
    * executing a <code>Work</code> instance.
    * 
    * @return an <code>List</code> of <code>WorkContext</code> instances.
    */
   public List<WorkContext> getWorkContexts()
   {
      return ctxs;
   }

   /**
    * Adds work context to the list
    * @param wc - added work context
    */
   public void addContext(WorkContext wc)
   {
      if (ctxs == null)
         ctxs = new ArrayList<WorkContext>();
      if (wc != null)
         ctxs.add(wc);
   }
}
