/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.webconsole;

/**
 * The <code>WebConsoleSecurityProvider</code> is a service interface allowing
 * to use an external system to authenticate users before granting access to
 * the Web Console.
 *
 * @since Web Console 3.0.2
 */
public interface WebConsoleSecurityProvider {

    /**
     * Check if the user with the specified password exists and return an
     * object identifying the user, else null
     */
    public Object authenticate(String username, String password);

    /**
     * Check that the authenticated user has the given role permission
     */
    public boolean authorize(Object user, String role);

}
