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

package org.ironjacamar.common.metadata.common;

/**
 * CommonXML
 */
public interface CommonXML
{
   /**
    * class-name
    */
   public static final String ATTRIBUTE_CLASS_NAME = "class-name";

   /**
    * module-name
    */
   public static final String ATTRIBUTE_MODULE_NAME = "module-name";

   /**
    * module-slot
    */
   public static final String ATTRIBUTE_MODULE_SLOT = "module-slot";

   /**
    * connectable
    */
   public static final String ATTRIBUTE_CONNECTABLE = "connectable";

   /**
    * enabled
    */
   public static final String ATTRIBUTE_ENABLED = "enabled";

   /**
    * enlistment
    */
   public static final String ATTRIBUTE_ENLISTMENT = "enlistment";

   /**
    * from
    */
   public static final String ATTRIBUTE_FROM = "from";

   /**
    * id
    */
   public static final String ATTRIBUTE_ID = "id";

   /**
    * jndi-name
    */
   public static final String ATTRIBUTE_JNDI_NAME = "jndi-name";

   /**
    * name
    */
   public static final String ATTRIBUTE_NAME = "name";

   /**
    * no-recovery
    */
   public static final String ATTRIBUTE_NO_RECOVERY = "no-recovery";

   /**
    * sharable
    */
   public static final String ATTRIBUTE_SHARABLE = "sharable";

   /**
    * to
    */
   public static final String ATTRIBUTE_TO = "to";

   /**
    * tracking
    */
   public static final String ATTRIBUTE_TRACKING = "tracking";

   /**
    * use-ccm
    */
   public static final String ATTRIBUTE_USE_CCM = "use-ccm";

   /**
    * type
    */
   public static final String ATTRIBUTE_TYPE = "type";

   /**
    * janitor
    */
   public static final String ATTRIBUTE_JANITOR = "janitor";

   /**
    * admin-object
    */
   public static final String ELEMENT_ADMIN_OBJECT = "admin-object";

   /**
    * admin-objects
    */
   public static final String ELEMENT_ADMIN_OBJECTS = "admin-objects";

   /**
    * allocation-retry
    */
   public static final String ELEMENT_ALLOCATION_RETRY = "allocation-retry";

   /**
    * allocation-retry-wait-millis
    */
   public static final String ELEMENT_ALLOCATION_RETRY_WAIT_MILLIS = "allocation-retry-wait-millis";


   /**
    * background-validation
    */
   public static final String ELEMENT_BACKGROUND_VALIDATION = "background-validation";

   /**
    * background-validation-millis
    */
   public static final String ELEMENT_BACKGROUND_VALIDATION_MILLIS = "background-validation-millis";

   /**
    * bean-validation-group
    */
   public static final String ELEMENT_BEAN_VALIDATION_GROUP = "bean-validation-group";

   /**
    * bean-validation-groups
    */
   public static final String ELEMENT_BEAN_VALIDATION_GROUPS = "bean-validation-groups";

   /**
    * blocking-timeout-millis
    */
   public static final String ELEMENT_BLOCKING_TIMEOUT_MILLIS = "blocking-timeout-millis";

   /**
    * bootstrap-context
    */
   public static final String ELEMENT_BOOTSTRAP_CONTEXT = "bootstrap-context";

   /**
    * capacity
    */
   public static final String ELEMENT_CAPACITY = "capacity";


   /**
    * config-property
    */
   public static final String ELEMENT_CONFIG_PROPERTY = "config-property";

   /**
    * connection-definition
    */
   public static final String ELEMENT_CONNECTION_DEFINITION = "connection-definition";

   /**
    * connection-definitions
    */
   public static final String ELEMENT_CONNECTION_DEFINITIONS = "connection-definitions";

   /**
    * decrementer
    */
   public static final String ELEMENT_DECREMENTER = "decrementer";

   /**
    * default-groups
    */
   public static final String ELEMENT_DEFAULT_GROUPS = "default-groups";

   /**
    * default-principal
    */
   public static final String ELEMENT_DEFAULT_PRINCIPAL = "default-principal";

   /**
    * domain
    */
   public static final String ELEMENT_DOMAIN = "domain";

   /**
    * flush-strategy
    */
   public static final String ELEMENT_FLUSH_STRATEGY = "flush-strategy";

   /**
    * group
    */
   public static final String ELEMENT_GROUP = "group";

   /**
    * groups
    */
   public static final String ELEMENT_GROUPS = "groups";

   /**
    * idle-timeout-minutes
    */
   public static final String ELEMENT_IDLE_TIMEOUT_MINUTES = "idle-timeout-minutes";

   /**
    * incrementer
    */
   public static final String ELEMENT_INCREMENTER = "incrementer";

   /**
    * initial-pool-size
    */
   public static final String ELEMENT_INITIAL_POOL_SIZE = "initial-pool-size";

   /**
    * is-same-rm-override
    */
   public static final String ELEMENT_IS_SAME_RM_OVERRIDE = "is-same-rm-override";

   /**
    * map
    */
   public static final String ELEMENT_MAP = "map";

   /**
    * mappings
    */
   public static final String ELEMENT_MAPPINGS = "mappings";

   /**
    * mapping-required
    */
   public static final String ELEMENT_MAPPING_REQUIRED = "mapping-required";

   /**
    * max-pool-size
    */
   public static final String ELEMENT_MAX_POOL_SIZE = "max-pool-size";

   /**
    * min-pool-size
    */
   public static final String ELEMENT_MIN_POOL_SIZE = "min-pool-size";

   /**
    * pad-xid
    */
   public static final String ELEMENT_PAD_XID = "pad-xid";

   /**
    * pool
    */
   public static final String ELEMENT_POOL = "pool";

   /**
    * prefill
    */
   public static final String ELEMENT_PREFILL = "prefill";

   /**
    * recovery
    */
   public static final String ELEMENT_RECOVERY = "recovery";

   /**
    * recovery-credential
    */
   public static final String ELEMENT_RECOVERY_CREDENTIAL = "recovery-credential";

   /**
    * recovery-plugin
    */
   public static final String ELEMENT_RECOVERY_PLUGIN = "recovery-plugin";

   /**
    * resource-adapter
    */
   public static final String ELEMENT_RESOURCE_ADAPTER = "resource-adapter";

   /**
    * security
    */
   public static final String ELEMENT_SECURITY = "security";

   /**
    * security-domain
    */
   public static final String ELEMENT_SECURITY_DOMAIN = "security-domain";


   /**
    * timeout
    */
   public static final String ELEMENT_TIMEOUT = "timeout";

   /**
    * transaction-support
    */
   public static final String ELEMENT_TRANSACTION_SUPPORT = "transaction-support";

   /**
    * users
    */
   public static final String ELEMENT_USERS = "users";

   /**
    * use-fast-fail
    */
   public static final String ELEMENT_USE_FAST_FAIL = "use-fast-fail";

   /**
    * validate-on-match
    */
   public static final String ELEMENT_VALIDATE_ON_MATCH = "validate-on-match";

   /**
    * validation
    */
   public static final String ELEMENT_VALIDATION = "validation";

   /**
    * workmanager
    */
   public static final String ELEMENT_WORKMANAGER = "workmanager";

   /**
    * security
    */
   public static final String ELEMENT_WORKMANAGER_SECURITY = "security";

   /**
    * wrap-xa-resource
    */
   public static final String ELEMENT_WRAP_XA_RESOURCE = "wrap-xa-resource";

   /**
    * xa-pool
    */
   public static final String ELEMENT_XA_POOL = "xa-pool";

   /**
    * xa-resource-timeout
    */
   public static final String ELEMENT_XA_RESOURCE_TIMEOUT = "xa-resource-timeout";
}
