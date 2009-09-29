/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008-2009, Red Hat Middleware LLC, and individual contributors
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

package javax.resource.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Designates a JavaBean as an <code>ActivationSpec</code>. This annotation may
 * be placed on a JavaBean. A JavaBean annotated with the Activation annotation
 * is not required to implement the {@link ActivationSpec ActivationSpec}
 * interface.
 * 
 * <p>The ActivationSpec JavaBean contains the configuration information pertaining
 * to inbound connectivity from an EIS instance. A resource adapter capable of
 * message delivery to message endpoints must provide an JavaBean class
 * implementing the {@link ActivationSpec ActivationSpec} interface or annotate
 * a JavaBean with the <code>Activation</code> annotation for each supported
 * endpoint message listener type.
 * 
 * <p>The ActivationSpec JavaBean has a set of configurable properties specific to
 * the messaging style and the message provider.
 * 
 * <p>Together with the messageListener annotation element, this annotation
 * specifies information about a specific message listener type supported by the
 * messaging resource adapter.
 * 
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Activation 
{
   
   /**
    * Indicates the message listener type(s) associated with this activation.
    * 
    * @return The Java types of the Message Listener interface this
    *         activation-spec is associated with.
    */
   Class[] messageListeners();
}
