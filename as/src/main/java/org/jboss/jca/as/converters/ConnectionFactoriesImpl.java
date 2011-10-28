/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.as.converters;

import java.util.ArrayList;
import java.util.List;
/**
 * A ConnectionFactoriesImpl .
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
public class ConnectionFactoriesImpl implements ConnectionFactories
{
   private List<NoTxConnectionFactory> noTxConnectionFactory = new ArrayList<NoTxConnectionFactory>();
   private List<TxConnectionFactory> txConnectionFactory = new ArrayList<TxConnectionFactory>();
   
   /**
    * ConnectionFactoriesImpl
    * @param noTxConnectionFactory noTxConnectionFactory
    * @param txConnectionFactory txConnectionFactory
    */
   public ConnectionFactoriesImpl(
         List<NoTxConnectionFactory> noTxConnectionFactory,
         List<TxConnectionFactory> txConnectionFactory)
   {
      this.noTxConnectionFactory = noTxConnectionFactory;
      this.txConnectionFactory = txConnectionFactory;
   }
   
   @Override
   public String toString()
   {
      StringBuilder out = new StringBuilder();
      out.append("<resource-adapters>");

      for (ConnectionFactory cf : noTxConnectionFactory)
      {
         
         out.append(cf.toString());
      }
      for (ConnectionFactory cf : txConnectionFactory)
      {
         out.append(cf.toString());
      }
      out.append("</resource-adapters>");
      
      return out.toString();
   }

   @Override
   public List<TxConnectionFactory> getTxConnectionFactory()
   {
      return txConnectionFactory;
   }

   @Override
   public List<NoTxConnectionFactory> getNoTxConnectionFactory()
   {
      return noTxConnectionFactory;
   }

}
