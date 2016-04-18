/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2014, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.metadata.resourceadapter;

import org.ironjacamar.common.api.metadata.resourceadapter.Activation;
import org.ironjacamar.common.api.metadata.resourceadapter.Activations;
import org.ironjacamar.common.metadata.common.AbstractMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activations of a resource adapter
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public class ActivationsImpl extends AbstractMetadata implements Activations
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   private final List<Activation> activations;

   /**
    * Constructor
    * @param activations activations
    */
   public ActivationsImpl(List<Activation> activations)
   {
      super(null);
      if (activations != null)
      {
         this.activations = new ArrayList<Activation>(activations.size());
         this.activations.addAll(activations);
      }
      else
      {
         this.activations = new ArrayList<Activation>(0);
      }
   }

   /**
    * Get the activations.
    *
    * @return the activations.
    */
   @Override
   public final List<Activation> getActivations()
   {
      return Collections.unmodifiableList(activations);
   }
   
   @Override
   public int hashCode()
   {
      return 37 + ((activations == null) ? 0 : activations.hashCode());
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (!(obj instanceof ActivationsImpl))
         return false;

      ActivationsImpl other = (ActivationsImpl) obj;
      if (activations == null)
      {
         if (other.activations != null)
            return false;
      }
      else if (!activations.equals(other.activations))
         return false;

      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder(1024);

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sb.append("<resource-adapters>");

      if (activations != null && !activations.isEmpty())
      {
         for (Activation a : activations)
         {
            sb.append(a);
         }
      }

      sb.append("</resource-adapters>");

      return sb.toString();
   }
}

