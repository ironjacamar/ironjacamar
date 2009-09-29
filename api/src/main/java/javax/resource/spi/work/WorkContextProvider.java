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
import java.util.List;

/**
 * This interface specifies the methods a {@link Work Work} instance uses to
 * associate a <code>List</code> of {@link WorkContext WorkContext}
 * instances to be set when the <code>Work</code> instance gets executed by a
 * {@link WorkManager WorkManager}.
 * 
 * <p> A <code>Work</code> instance could optionally implement this interface to
 * indicate to the <code>WorkManager</code>, that the
 * <code>WorkContext</code>s provided by this <code>Work</code> instance
 * through the {@link #getWorkContexts() getWorkContexts} method must be
 * used while setting the execution context of the <code>Work</code> instance.<p>
 * 
 * If the {@link #getWorkContexts() getWorkContexts} method returns an empty List
 * or null, the WorkManager must treat it as if no additional execution contexts
 * are associated with that Work instance.
 *
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
public interface WorkContextProvider extends Serializable
{
   /**
    * Gets an instance of <code>WorkContexts</code> that needs to be used
    * by the <code>WorkManager</code> to set up the execution context while
    * executing a <code>Work</code> instance.
    * 
    * @return an <code>List</code> of <code>WorkContext</code> instances.
    */
   List<WorkContext> getWorkContexts();
}
