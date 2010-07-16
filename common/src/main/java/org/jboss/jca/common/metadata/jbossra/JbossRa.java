/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jca.common.metadata.jbossra;

import org.jboss.jca.common.metadata.JCAMetadata;
import org.jboss.jca.common.metadata.jbossra.jbossra20.RaConfigProperty;

import java.util.Collections;
import java.util.List;

public abstract class JbossRa implements JCAMetadata
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;
   private final List<RaConfigProperty<?>> raConfigProperties;


   protected JbossRa(List<RaConfigProperty<?>> raConfigProperties) {
      this.raConfigProperties = raConfigProperties;
   }

   /**
    * @return raConfigProperties
    */
   public List<RaConfigProperty<?>> getRaConfigProperties()
   {
      return Collections.unmodifiableList(raConfigProperties);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((raConfigProperties == null) ? 0 : raConfigProperties.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof JbossRa))
         return false;
      JbossRa other = (JbossRa) obj;
      if (raConfigProperties == null)
      {
         if (other.raConfigProperties != null)
            return false;
      }
      else if (!raConfigProperties.equals(other.raConfigProperties))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "JbossRa [raConfigProperties=" + raConfigProperties + "]";
   }

}