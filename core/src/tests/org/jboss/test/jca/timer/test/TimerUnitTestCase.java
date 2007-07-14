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
package org.jboss.test.jca.timer.test;

import java.util.Timer;
import java.util.TimerTask;

import junit.framework.Test;

import org.jboss.test.jca.JCATest;
import org.jboss.test.jca.rar.support.TestResourceAdapter;

/**
 * Timer Unit Tests
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class TimerUnitTestCase extends JCATest
{
   public TimerUnitTestCase (String name)
   {
      super(name);
   }

   public void testTimer() throws Throwable
   {
      TestResourceAdapter adapter = (TestResourceAdapter) getBean("TestRAR");
      Timer timer = adapter.createTimerFromBootstrapContext();
      TestTimerTask task = new TestTimerTask();
      getLog().debug("Schedule for 5 seconds " + task);
      timer.schedule(task, 5000l);
      getLog().debug("Sleep for 10 seconds");
      Thread.sleep(10000);
      if (task.complete == false)
         throw new Exception("Task was not run");

   }
   
   public class TestTimerTask extends TimerTask
   {
      public boolean complete = false;
      
      public void run()
      {
         getLog().debug("Run " + this);
         complete = true;
      }
   }
   
   public static Test suite() throws Exception
   {
      return suite(TimerUnitTestCase.class);
   }
}
