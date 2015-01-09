/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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
package org.ironjacamar.common.api.metadata;

import java.io.Serializable;

/**
 * Generic metadata representation
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface JCAMetadata extends Serializable
{
   /**
    * Has expression
    * @param key The key
    * @return The value
    */
   public boolean hasExpression(String key);

   /**
    * Get a value
    * @param key The key
    * @param v The default value
    * @return The value
    */
   public String getValue(String key, String v);

   /**
    * Get a value
    * @param key The key
    * @param subkey The subkey
    * @param v The default value
    * @return The value
    */
   public String getValue(String key, String subkey, String v);
}
