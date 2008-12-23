/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * A standard <code>WorkContext</code> that allows a <code>Work</code>
 * instance to propagate transaction related context information from an EIS to
 * an application server.<p>
 * 
 * This class extends <code>ExecutionContext</code> so that a resource adapter
 * developer could migrate their existing code from
 * <code>ExecutionContext</code> to <code>TransactionWorkContext</code>
 * easily.<p>
 * 
 * @since 1.6
 * @see javax.resource.spi.work.WorkContext
 * @see javax.resource.spi.work.ExecutionContext
 * @version JSR322-EarlyDraft
 */

public class TransactionWorkContext extends ExecutionContext implements WorkContext 
{
   /**
    * {@inheritDoc}
    */
   public String getDescription() {
      return "Transaction Inflow Context";
   }

   /**
    * {@inheritDoc}
    */
   public String getName() {
      return "TransactionWorkContext";
   }
}
