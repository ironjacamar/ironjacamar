/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jca.embedded.dsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.descriptor.api.DescriptorExportException;

/**
 * An InputStream descriptor. 
 * <p/>
 * Note, that this class will close the passed <code>InputStream</code> once one of the export methods
 * have been called.
 * 
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class InputStreamDescriptor implements Descriptor
{
   private final String name;
   private final InputStream is;

   /**
    * Create an InputStreamDescriptor
    * @param name The descriptor name
    * @param is The input stream
    */
   public InputStreamDescriptor(final String name, final InputStream is)
   {
      if (name == null)
         throw new IllegalArgumentException("Name is null");

      if (is == null)
         throw new IllegalArgumentException("InputStream is null");

      this.name = name;
      this.is = is;
   }

   /**
    * {@inheritDoc}
    */
   public String getDescriptorName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public String exportAsString() throws DescriptorExportException
   {
      try
      {
         StringBuilder sb = new StringBuilder();

         int read = 0;
         while ((read = is.read()) != -1)
         {
            sb = sb.append((char)read);
         }

         return sb.toString();
      }
      catch (IOException ioe)
      {
         throw new DescriptorExportException("Error during exportAsString: " + ioe.getMessage(), ioe);
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void exportTo(OutputStream output) throws DescriptorExportException, IllegalArgumentException
   {
      if (output == null)
         throw new IllegalArgumentException("Output is null");

      try
      {
         int read = 0;
         while ((read = is.read()) != -1)
         {
            output.write(read);
         }

         output.flush();
      }
      catch (IOException ioe)
      {
         throw new DescriptorExportException("Error during exportTo: " + ioe.getMessage(), ioe);
      }      
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }
}
