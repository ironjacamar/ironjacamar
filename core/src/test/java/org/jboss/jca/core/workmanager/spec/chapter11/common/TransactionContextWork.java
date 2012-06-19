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

package org.jboss.jca.core.workmanager.spec.chapter11.common;

import java.util.ArrayList;
import java.util.List;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkContextProvider;

/**
 * TransactionContextWork.
 * @version $Rev$ $Date$
 *
 */
public class TransactionContextWork implements Work, WorkContextProvider
{
   /** Serial version uid */
   private static final long serialVersionUID = 2855367727578670465L;

   /**
    * The <code>WorkManager</code> might call this method to hint the
    * active <code>Work</code> instance to complete execution as soon as 
    * possible. 
    */
   public void release()
   {
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
      List<WorkContext> ctxts = new ArrayList<WorkContext>();
      ctxts.add(new TransactionContextCustom());

      return ctxts;
   }
}
