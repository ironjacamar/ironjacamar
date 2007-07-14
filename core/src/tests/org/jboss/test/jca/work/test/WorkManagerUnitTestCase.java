/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.test.jca.work.test;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;

import junit.framework.Test;

import org.jboss.test.jca.JCATest;
import org.jboss.test.jca.rar.support.TestResourceAdapter;

/**
 * WorkManager Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class WorkManagerUnitTestCase extends JCATest
{

   public WorkManagerUnitTestCase (String name)
   {
      super(name);
   }

   public void testWorkManager() throws Throwable
   {
      TestResourceAdapter adapter = (TestResourceAdapter) getBean("TestRAR");
      WorkManager wm = adapter.getWorkManagerFromBootstrapContext();
      TestWork work = new TestWork(); 
      getLog().debug("doWork " + work);
      wm.doWork(work);
      getLog().debug("doneWork " + work);
      if (work.complete == false)
         throw new Exception("Work was not done");
   }
   
   public class TestWork implements Work
   {
      public boolean complete = false;

      public void run()
      {
         getLog().debug("runWork " + this);
         complete = true;
      }

      public void release()
      {
      }
   }
   
   public static Test suite() throws Exception
   {
      return suite(WorkManagerUnitTestCase.class);
   }
}
