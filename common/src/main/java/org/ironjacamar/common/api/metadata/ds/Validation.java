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
package org.ironjacamar.common.api.metadata.ds;

import org.ironjacamar.common.api.metadata.ValidatableMetadata;
import org.ironjacamar.common.api.metadata.common.Extension;

/**
 * A Validation.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 */
public interface Validation extends org.ironjacamar.common.api.metadata.common.Validation, ValidatableMetadata
{
   /**
    * Get the validConnectionChecker
    *
    * @return the validConnectionChecker
    */
   public Extension getValidConnectionChecker();

   /**
    * Get the checkValidConnectionSql.
    *
    * @return the checkValidConnectionSql.
    */
   public String getCheckValidConnectionSql();

   /**
    * Get the staleConnectionChecker
    *
    * @return the staleConnectionChecker
    */
   public Extension getStaleConnectionChecker();

   /**
    * Get the exceptionSorter
    *
    * @return the exceptionSorter
    */
   public Extension getExceptionSorter();
}
