/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The <strong>IronJacamar</strong> project nature indicates a project is an <strong>IronJacamar</strong> project.
 * 
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 */
public class IronJacamarProjectNature implements IProjectNature
{
   /**
    * <strong>IronJacamar</strong> project nature id. 
    */
   public static final String NATURE_ID = "org.jboss.jca.eclipse.IronJacamarProjectNature";

   private IProject project;
   
   @Override
   public void configure() throws CoreException
   {
      ;
   }

   @Override
   public void deconfigure() throws CoreException
   {
      ;
   }

   @Override
   public IProject getProject()
   {
      return this.project;
   }

   @Override
   public void setProject(IProject project)
   {
      this.project = project;
   }

}
