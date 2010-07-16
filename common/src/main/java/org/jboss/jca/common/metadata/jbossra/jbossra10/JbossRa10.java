/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jca.common.metadata.jbossra.jbossra10;

import org.jboss.jca.common.metadata.jbossra.JbossRa;
import org.jboss.jca.common.metadata.jbossra.jbossra20.RaConfigProperty;

import java.util.List;

public class JbossRa10 extends JbossRa
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   public JbossRa10(List<RaConfigProperty<?>> raConfigProperties)
   {
      super(raConfigProperties);
   }

   @Override
   public String toString()
   {
      return "JbossRa10 [getRaConfigProperties()=" + getRaConfigProperties() + "]";
   }

}
