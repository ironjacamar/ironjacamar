/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2015, Red Hat Inc, and individual contributors
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


package org.ironjacamar.core.spi.workmanager.policy;


import org.ironjacamar.core.api.workmanager.DistributedWorkManager;

import javax.resource.spi.work.DistributableWork;

/**
 * The policy interface defines how the distributed work manager to
 * decide to distribute the work instance to another node.
 *
 * The work instance may contain a DistributableWorkContext which may
 * provide additional information 
 */
public interface Policy
{
   /**
    * Should a distribution of the work happen
    * @param dwm The invoking distributed work manager
    * @param work The work instance
    * @return True, if the work should be distributed; otherwise false
    */
   public boolean shouldDistribute(DistributedWorkManager dwm, DistributableWork work);
}
