/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2010, Red Hat Inc, and individual contributors
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

package org.jboss.jca.adapters.jdbc.extensions.db2;

import org.jboss.jca.adapters.jdbc.spi.ExceptionSorter;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;

/**
 * A DB2ExceptionSorter current only supporting the Type 4 Universal driver.
 * Note, currently the DB2 JDBC developers guide only reports a few error codes.
 * The code -9999 implies that the condition does not have a related code.
 * 
 * TODO DB2 CLI
 * 
 * @author <a href="wprice@redhat.com">Weston Price</a>
 * @version $Revision: 71554 $
 */
public class DB2ExceptionSorter implements ExceptionSorter, Serializable
{
   
   /** The logger */
   private static Logger logger = Logger.getLogger(DB2ExceptionSorter.class);

   /** The serialVersionUID */
   private static final long serialVersionUID = -4724550353693159378L;

   private boolean consider99999Fatal = false;

   private Set<String> fatalSet = null;

   /**
    * Constructor
    */
   public DB2ExceptionSorter() 
   {
      fatalSet = new HashSet<String>();
   }

   /**
    *
    * set via <config-property name="Consider99999Fatal">TRUE/FALSE</>
    *
    * Consider DB2's -99999 error code fatal. This is regardless of Fatal99999Messages.
    * @param value The value
    */
   public void setConsider99999Fatal(String value)
   {
      consider99999Fatal = Boolean.parseBoolean(value);
   }

   /**
    *
    * set via <config-property name="Fatal99999Messages">Connection is closed, Connection reset</>
    *
    * Which -99999 are considered fatal
    * @param messages The messages to be considered fatal
    */
   public void setFatal99999Messages(String messages)
   {
      StringTokenizer st = new StringTokenizer(messages, ",");
      while (st.hasMoreTokens())
      {
         fatalSet.add(st.nextToken().toUpperCase());
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isExceptionFatal(final SQLException e)
   {
      final int code = Math.abs(e.getErrorCode());
      boolean isFatal = false;
      
      if (code == 4470)
      {
         isFatal = true;
      }
      else if (code == 4499)
      {
         isFatal = true;
      }
      else if (code == 99999 && consider99999Fatal)
      {
         isFatal = true;
      }
      else if (code == 99999 && !consider99999Fatal && !fatalSet.isEmpty())
      {
         final String errorText = (e.getMessage()).toUpperCase();
         for (String message : fatalSet)
         {
            if (message.equalsIgnoreCase(errorText))
            {
               isFatal = true;
            }
         }
      }
      
      logger.tracef("Evaluated SQL error code %d isException returned %b", code,  isFatal);
      
      return isFatal;
   }
}
