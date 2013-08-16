/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
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

import org.ironjacamar.common.CommonLogger;
import org.ironjacamar.common.spi.metadata.Parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * Parser factory
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ParserFactory
{
   /** The logger */
   private static CommonLogger log = Logger.getMessageLogger(CommonLogger.class,
                                                             ParserFactory.class.getName());

   /** RA_XML */
   public static final int RA_XML = 0;

   /** IRONJACAMAR_XML */
   public static final int IRONJACAMAR_XML = 1;

   /** DASH_RA_XML */
   public static final int DASH_RA_XML = 2;

   /** The available parsers */
   private static Map<Integer, Parser> parsers;

   static
   {
      parsers = new HashMap<Integer, Parser>();

      try
      {
         Enumeration<URL> enumeration =
            ParserFactory.class.getClassLoader().getResources("META-INF/services/org.ironjacamar.metadata.XmlParser");

         if (enumeration != null)
         {
            while (enumeration.hasMoreElements())
            {
               URL u = enumeration.nextElement();

               InputStream is = null;
               ByteArrayOutputStream os = new ByteArrayOutputStream();
               try
               {
                  is = u.openStream();
                  is = new BufferedInputStream(is, 8192);

                  int b;
                  while ((b = is.read()) != -1)
                  {
                     os.write(b);
                  }

                  os.flush();

                  String clz = new String(os.toByteArray(), "UTF-8");

                  Class<?> c = Class.forName(clz, true, ParserFactory.class.getClassLoader());
                  Parser p = (Parser)c.newInstance();
                  parsers.put(p.getType(), p);
               }
               catch (Exception e)
               {
                  log.debug(e.getMessage(), e);
               }
               finally
               {
                  if (is != null)
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
                  if (os != null)
                  {
                     try
                     {
                        os.close();
                     }
                     catch (IOException ioe)
                     {
                        // Ignore
                     }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         log.debug(e.getMessage(), e);
      }
   }

   /**
    * Private constructor
    */
   private ParserFactory()
   {
   }

   /**
    * Get the parser for a specific type
    * @param type The type
    * @return The parser
    */
   public static Parser getParser(int type)
   {
      return parsers.get(Integer.valueOf(type));
   }
}
