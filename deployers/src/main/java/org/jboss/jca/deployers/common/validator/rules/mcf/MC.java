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

package org.jboss.jca.deployers.common.validator.rules.mcf;

import org.jboss.jca.deployers.common.validator.Failure;
import org.jboss.jca.deployers.common.validator.Key;
import org.jboss.jca.deployers.common.validator.Rule;
import org.jboss.jca.deployers.common.validator.Severity;
import org.jboss.jca.deployers.common.validator.ValidateObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.resource.spi.ManagedConnection;

/**
 * Must provide an implementation of the ManagedConnection interface.
 */
public class MC implements Rule
{
   /** Section */
   private static final String SECTION = "6.5.4.5";

   /**
    * Constructor
    */
   public MC()
   {
   }

   /**
    * Validate
    * @param vo The validate object
    * @param rb The resource bundle 
    * @return The list of failures found; <code>null</code> if none
    */
   @SuppressWarnings("unchecked")
   public List<Failure> validate(ValidateObject vo, ResourceBundle rb)
   {
      if (vo != null && Key.MANAGED_CONNECTION == vo.getKey())
      {
         if (!(vo.getObject() instanceof ManagedConnection))
         {
            List<Failure> failures = new ArrayList<Failure>(1);

            Failure failure = new Failure(Severity.ERROR,
                                          SECTION,
                                          rb.getString("mc.MC"),
                                          vo.getObject().getClass().getName());
            failures.add(failure);

            return failures;
         }
      }

      return null;
   }
}

