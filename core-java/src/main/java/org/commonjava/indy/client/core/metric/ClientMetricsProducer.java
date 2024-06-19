/**
 * Copyright (C) 2023 Red Hat, Inc. (https://github.com/Commonjava/indy-client)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.indy.client.core.metric;

import org.commonjava.o11yphant.metrics.TrafficClassifier;
import org.commonjava.o11yphant.metrics.conf.DefaultMetricsConfig;
import org.commonjava.o11yphant.metrics.conf.MetricsConfig;
import org.commonjava.o11yphant.metrics.sli.GoldenSignalsMetricSet;
import org.commonjava.o11yphant.metrics.system.StoragePathProvider;

import javax.enterprise.inject.Produces;

/**
 * This producer is used to provide the missing CDI deps for indy client metrics sets. User can specify
 * customized producers with @Alternative to provide alternative functions.
 */
public class ClientMetricsProducer
{
    @Produces
    public TrafficClassifier getClientTrafficClassifier()
    {
        return new ClientTrafficClassifier();
    }

    @Produces
    public GoldenSignalsMetricSet getClientMetricSet()
    {
        return new ClientGoldenSignalsMetricSet();
    }

    @Produces
    public MetricsConfig getMetricsConfig()
    {
        return new DefaultMetricsConfig();
    }

    @Produces
    public StoragePathProvider getStoragePathProvider()
    {
        return () -> null;
    }
}
