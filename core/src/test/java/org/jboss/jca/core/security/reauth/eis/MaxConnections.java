/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2011, Red Hat Inc, and individual contributors
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

package org.jboss.jca.core.security.reauth.eis;

import java.io.Serializable;
import java.util.Arrays;

import org.jboss.logging.Logger;

/**
 * Represents a max connections command
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class MaxConnections implements Invoker
{
   /** The logger */
   private Logger log = Logger.getLogger(MaxConnections.class);

   /** The interaction */
   private Interaction interaction;

   /** Max connections */
   private Integer maxConnections;

   /**
    * Constructor
    * @param interaction The interaction
    * @param maxConnections The max connections
    */
   public MaxConnections(Interaction interaction, Integer maxConnections)
   {
      this.interaction = interaction;
      this.maxConnections = maxConnections;
   }

   /**
    * Invoke
    * @param args The arguments
    * @return The return value
    */
   public Serializable invoke(Serializable[] args)
   {
      if (args != null)
         return new IllegalArgumentException("Unsupported argument list: " + Arrays.toString(args));

      log.infof("MaxConnections: %s", maxConnections);

      return maxConnections;
   }
}
