/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.jca.codegenerator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A AuthenMechanismType.
 * 
 * @author Jeff Zhang
 * @version $Revision: $
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthenMechanismType
{
   /** define authMechanism */
   @XmlElement(name = "authMechanism") 
   private String authMechanism = "BasicPassword";
   
   /** define credentialInterface */
   @XmlElement(name = "credentialInterface") 
   private String credentialInterface = "PasswordCredential";

   /**
    * Set the authMechanism.
    * 
    * @param authMechanism The authMechanism to set.
    */
   public void setAuthMechanism(String authMechanism)
   {
      this.authMechanism = authMechanism;
   }

   /**
    * Get the authMechanism.
    * 
    * @return the authMechanism.
    */
   public String getAuthMechanism()
   {
      return authMechanism;
   }

   /**
    * Set the credentialInterface.
    * 
    * @param credentialInterface The credentialInterface to set.
    */
   public void setCredentialInterface(String credentialInterface)
   {
      this.credentialInterface = credentialInterface;
   }

   /**
    * Get the credentialInterface.
    * 
    * @return the credentialInterface.
    */
   public String getCredentialInterface()
   {
      return credentialInterface;
   }
   
}
