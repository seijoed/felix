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
package org.apache.felix.dm.test.bundle.annotation.publisher;

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Init;
import org.apache.felix.dm.annotation.api.LifecycleController;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.test.bundle.annotation.sequencer.Sequencer;

/**
 * A Service that just registers/unregisters its service, using the @ServiceLifecycle annotation.
 */
public class ServiceTestWthPublisher
{
    public interface Provider
    {
    }

    @Component
    public static class Consumer
    {
        @ServiceDependency(filter="(test=ServiceTestWthPublisher)")
        Sequencer m_sequencer;
        
        @ServiceDependency(required=false, removed = "unbind")
        void bind(Map properties, Provider provider)
        {
            m_sequencer.step(1);
            if ("bar".equals(properties.get("foo")))
            {
                m_sequencer.step(2);
            }
            if ("bar2".equals(properties.get("foo2")))
            {
                m_sequencer.step(3);
            }
        }

        void unbind(Provider provider)
        {
            m_sequencer.step(4);
        }
    }
    
    @Component(properties={@Property(name="foo", value="bar")})
    public static class ProviderImpl implements Provider
    {
        @LifecycleController
        Runnable m_publisher; // injected and used to register our service
        
        @LifecycleController(start=false)
        Runnable m_unpublisher; // injected and used to unregister our service
        
        @ServiceDependency(filter="(test=ServiceTestWthPublisher)")
        Sequencer m_sequencer;

        @Init
        void init()
        {
            // register service in 1 second
            Utils.schedule(m_publisher, 1000);
            // unregister the service in 2 seconds
            Utils.schedule(m_unpublisher, 2000);
        }
        
        @Start
        Map start()
        {
            // Add some extra service properties ... they will be appended to the one we have defined
            // in the @Service annotation.
            return new HashMap() {{ put("foo2", "bar2"); }};
        }
    }
}
