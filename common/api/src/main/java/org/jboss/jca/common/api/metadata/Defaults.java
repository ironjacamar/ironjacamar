/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2008, Red Hat Inc, and individual contributors
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

package org.jboss.jca.common.api.metadata;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.common.api.metadata.ds.Statement;

/**
 * Default values for the JCA metadata
 *
 * @author <a href="jesper.pedersen@ironjacamar.org">Jesper Pedersen</a>
 */
public interface Defaults
{
   //DATASOURCE

   /**
    * Use Java context
    */
   public static final Boolean USE_JAVA_CONTEXT = Boolean.TRUE;
   
   /**
    * Use Java context
    */
   public static final Boolean ENABLED = Boolean.TRUE;

   /**
    * SPY
    */
   public static final Boolean SPY = Boolean.FALSE;
   
   /**
    * Use ccm
    */
   public static final Boolean USE_CCM = Boolean.TRUE;
   
   /**
    * JTA
    */
   public static final boolean JTA = Boolean.TRUE;
   
   /**
    * SHARABLE
    */
   public static final Boolean SHARABLE = Boolean.TRUE;
   
   /**
    * ENLISTMENT
    */
   public static final Boolean ENLISTMENT = Boolean.TRUE;
   
   /**
    * CONNECTABLE
    */
   public static final Boolean CONNECTABLE = Boolean.FALSE;
   
   /**
    * TRACKING
    */
   public static final Boolean TRACKING = null;
   
   /**
    * MCP
    */
   public static final String MCP = null;
   
   /**
    * ENLISTMENT_TRACE
    */
   public static final Boolean ENLISTMENT_TRACE = false;
   
   // POOL

   /**
    * Min pool size
    */
   public static final Integer MIN_POOL_SIZE = Integer.valueOf(0);

   /**
    * Initial pool size
    */
   public static final Integer INITIAL_POOL_SIZE = null;

   /**
    * Max pool size
    */
   public static final Integer MAX_POOL_SIZE = Integer.valueOf(20);
   
   /**
    * Prefill
    */
   public static final Boolean PREFILL = Boolean.FALSE;
   
   /**
    * Fair
    */
   public static final Boolean FAIR = Boolean.TRUE;

   /**
    * Use strict min
    */
   public static final Boolean USE_STRICT_MIN = Boolean.FALSE;
   
   /**
    * Flush strategy
    */
   public static final FlushStrategy FLUSH_STRATEGY = FlushStrategy.FAILING_CONNECTION_ONLY;
   
   /**
    * Interleaving
    */
   public static final Boolean INTERLEAVING = Boolean.FALSE;
   
   /**
    * Is same rm override
    */
   public static final Boolean IS_SAME_RM_OVERRIDE = null;
   
   /**
    * Pad Xid
    */
   public static final Boolean PAD_XID = Boolean.FALSE;
   
   /**
    * No tx separate pool
    */
   public static final Boolean NO_TX_SEPARATE_POOL = Boolean.FALSE;
   
   /**
    * Wrap XAResource
    */
   public static final Boolean WRAP_XA_RESOURCE = Boolean.TRUE;
   
   /**
    * Allow multiple users
    */
   public static final Boolean ALLOW_MULTIPLE_USERS = Boolean.FALSE;
   
   //Statement
   
   /**
    * share prepared statement
    */
   public static final Boolean SHARE_PREPARED_STATEMENTS = Boolean.FALSE;
   
   /**
    * Track statements
    */
   public static final Statement.TrackStatementsEnum TRACK_STATEMENTS = Statement.TrackStatementsEnum.NOWARN;
   
   
   //timeout
   
   /**
    * SET TX QUERY TIMEOUT
    */
   public static  final  Boolean SET_TX_QUERY_TIMEOUT = Boolean.FALSE;
   
   //Validation
   
   /**
    * Background validation
    */
   public static final Boolean BACKGROUND_VALIDATION = null;

   /**
    * Use fast fail
    */
   public static final Boolean USE_FAST_FAIL = Boolean.FALSE;

   /**
    * Validate on match
    */
   public static final Boolean VALIDATE_ON_MATCH = null;

   //security
   /**
    * No recovery
    */
   public static final Boolean NO_RECOVERY = Boolean.FALSE;

   /**
    * application managed security
    */
   public static final Boolean APPLICATION_MANAGED_SECURITY = Boolean.FALSE;

}
