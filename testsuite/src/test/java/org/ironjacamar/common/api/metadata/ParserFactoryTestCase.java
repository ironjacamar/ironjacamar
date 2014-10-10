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

import org.jboss.logging.Logger;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Parser factory test case
 * @author <a href="mailto:jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public class ParserFactoryTestCase
{
   /** The logger */
   private static CommonLogger log = Logger.getMessageLogger(CommonLogger.class,
                                                             ParserFactoryTestCase.class.getName());

   /**
    * Test there is a instance of each parser
    * @exception Exception Thrown in case of an error
    */
   @Test
   public void testParsers() throws Exception
   {
      Parser raXmlParser = ParserFactory.getParser(ParserFactory.RA_XML);
      assertNull(raXmlParser);

      Parser ironjacamarXmlParser = ParserFactory.getParser(ParserFactory.IRONJACAMAR_XML);
      assertNull(ironjacamarXmlParser);

      Parser dashRaXmlParser = ParserFactory.getParser(ParserFactory.DASH_RA_XML);
      assertNull(dashRaXmlParser);
   }
}
