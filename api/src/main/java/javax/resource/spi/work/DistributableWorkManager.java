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
 * This interface models a <code>WorkManager</code> that supports distributed
 * execution of Work instances.
 * 
 * <p> A <code>DistributableWorkManager</code> may choose to distribute a
 * <code>Work</code> instance submitted by a resource adapter to another
 * <code>WorkManager</code> instance running in a different Java virtual 
 * machine (that is running in the same host or different hosts) for 
 * achieving optimal resource utilization or for providing better 
 * response times.
 * 
 * <p> A <code>WorkManager</code> implementation that supports the submission 
 * of <code>DistributableWork</code> instances must implement the
 * <code>DistributableWorkManager</code> marker interface.
 * 
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
public interface DistributableWorkManager extends WorkManager 
{
}
