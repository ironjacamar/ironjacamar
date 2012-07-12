/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.adapters.jdbc.spi;

import java.util.List;

/**
 * URLXASelectorStrategy
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public interface URLXASelectorStrategy 
{
   /**
    * Init the plugin with the XAData entries
    * @param xds The value
    */
   public void init(List<XAData> xds);

   /**
    * Does the plugin has more valid XAData entries ?
    * @return True, if more are available, otherwise false
    */
   public boolean hasMore();

   /**
    * Get the active XAData entry
    * @return The value
    */
   public XAData active();

   /**
    * Fail a XAData entry - e.g. mark it as bad
    * @param xd The value
    */
   public void fail(XAData xd);

   /**
    * Reset the plugin
    */
   public void reset();

   /**
    * Get the data for an error presentation
    * @return The value
    */
   public String getData();
}
