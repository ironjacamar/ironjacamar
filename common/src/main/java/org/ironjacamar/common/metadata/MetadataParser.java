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
package org.ironjacamar.common.metadata;

import org.ironjacamar.common.api.metadata.JCAMetadata;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * A MetadataParser.
 *
 * @author <a href="stefano.maestri@ironjacamar.org">Stefano Maestri</a>
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 * @param <T>
 */
public interface MetadataParser<T extends JCAMetadata>
{
   /**
    * Are system properties resolved ?
    * @return True if resolved (default); otherwise false
    */
   public boolean isSystemPropertiesResolved();

   /**
    * Set if system properties should be resolved
    * @param v The value
    */
   public void setSystemPropertiesResolved(boolean v);

   /**
    * Parse the xml file and return the JCAMetaData for which the concrete parser is designed.
    * Note that is responsibility of the client to open and close the stream
    * @param reader an XMLStreamReader opened on the xml file to parse
    * @return The metadata
    * @exception Exception Thrown if an error occurs
    */
   public T parse(XMLStreamReader reader) throws Exception;

   /**
    * Store the model to an xml file for which the concrete parser is designed.
    * Note that is responsibility of the client to open and close the stream
    * @param metadata the metadata
    * @param writer an XMLStreamWriter opened on the xml file to write
    * @exception Exception Thrown if an error occurs
    */
   public void store(T metadata, XMLStreamWriter writer) throws Exception;
}
