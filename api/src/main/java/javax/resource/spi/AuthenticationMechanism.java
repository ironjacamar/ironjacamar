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
 * An annotation used to specify the authentication mechanism 
 * supported by the resource adapter.
 *
 * @since 1.6
 * @version Java EE Connector Architecture 1.6
 */
@Documented
@Retention(RUNTIME)
@Target({ })
public @interface AuthenticationMechanism 
{
   
   /** 
    * An enumerated type that represents the various interfaces
    * that a resource adapter may support for the representation
    * of the credentials.
    *
    * @since 1.6
    * @version Java EE Connector Architecture 1.6
    */
   public enum CredentialInterface 
   {
      /**
       * Corresponds to 
       * <code>javax.resource.spi.security.PasswordCredential</code>.
       * This is the default credential interface
       */
      PasswordCredential, 
        
      /**
       * Corresponds to <code>org.ietf.jgss.GSSCredential</code>
       */
      GSSCredential,
        
      /**
       * Corresponds to 
       * <code>javax.resource.spi.security.GenericCredential</code>
       */
      GenericCredential 
   };

   /**
    * The authentication-mechanismType specifies an authentication
    * mechanism supported by the resource adapter. Note that this
    * support is for the resource adapter and not for the
    * underlying EIS instance.
    *
    */
   String authMechanism() default  "BasicPassword";
   
   /**
    * The optional description specifies
    * any resource adapter specific requirement for the support of
    * security contract and authentication mechanism.
    */
   String[] description() default { };
   
   /**
    * Represents the interface that the resource adapter implementation
    * supports for the representation of the credentials.
    *
    * Note that BasicPassword mechanism type should support the
    * <code>javax.resource.spi.security.PasswordCredential</code> interface.
    * The Kerbv5 mechanism type should support the
    * <code>org.ietf.jgss.GSSCredential</code> interface or the deprecated
    * <code>javax.resource.spi.security.GenericCredential</code> interface.
    */
   CredentialInterface credentialInterface() default CredentialInterface.PasswordCredential;
}
