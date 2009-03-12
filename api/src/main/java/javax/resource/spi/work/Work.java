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

/**
 * This models a <code>Work</code> instance that would be executed by a 
 * <code>WorkManager</code> upon submission.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface Work extends Runnable 
{
   
   /**
    * The <code>WorkManager</code> might call this method to hint the
    * active <code>Work</code> instance to complete execution as soon as 
    * possible. This would be called on a seperate thread other than the
    * one currently executing the <code>Work</code> instance.
    */
   void release();
}
