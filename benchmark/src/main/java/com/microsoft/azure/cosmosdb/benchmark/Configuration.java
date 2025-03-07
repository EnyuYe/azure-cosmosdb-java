/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.benchmark;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;
import com.google.common.net.PercentEscaper;
import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.benchmark.Configuration.Operation.OperationTypeConverter;
import io.micrometer.azuremonitor.AzureMonitorConfig;
import io.micrometer.azuremonitor.AzureMonitorMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.NamingConvention;
import io.micrometer.core.lang.Nullable;
import io.micrometer.graphite.GraphiteConfig;
import io.micrometer.graphite.GraphiteMeterRegistry;
import java.util.Collections;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

class Configuration {

    private final static int DEFAULT_GRAPHITE_SERVER_PORT = 2003;
    private MeterRegistry azureMonitorMeterRegistry;
    private MeterRegistry graphiteMeterRegistry;

    @Parameter(names = "-serviceEndpoint", description = "Service Endpoint")
    private String serviceEndpoint;

    @Parameter(names = "-masterKey", description = "Master Key")
    private String masterKey;

    @Parameter(names = "-databaseId", description = "Database ID")
    private String databaseId;

    @Parameter(names = "-collectionId", description = "Collection ID")
    private String collectionId;

    @Parameter(names = "-useNameLink", description = "Use name Link")
    private boolean useNameLink = false;

    @Parameter(names = "-documentDataFieldSize", description = "Length of a document data field in characters (16-bit)")
    private int documentDataFieldSize = 20;

    @Parameter(names = "-maxConnectionPoolSize", description = "Max Connection Pool Size")
    private Integer maxConnectionPoolSize = 1000;

    @Parameter(names = "-consistencyLevel", description = "Consistency Level", converter = ConsistencyLevelConverter.class)
    private ConsistencyLevel consistencyLevel = ConsistencyLevel.Session;

    @Parameter(names = "-connectionMode", description = "Connection Mode")
    private ConnectionMode connectionMode = ConnectionMode.Direct;

    @Parameter(names = "-graphiteEndpoint", description = "Graphite endpoint")
    private String graphiteEndpoint;

    @Parameter(names = "-enableJvmStats", description = "Enables JVM Stats")
    private boolean enableJvmStats;

    @Parameter(names = "-operation", description = "Type of Workload:\n"
            + "\tReadThroughput- run a Read workload that prints only throughput *\n"
            + "\tWriteThroughput - run a Write workload that prints only throughput\n"
            + "\tReadLatency - run a Read workload that prints both throughput and latency *\n"
            + "\tWriteLatency - run a Write workload that prints both throughput and latency\n"
            + "\tQueryCross - run a 'Select * from c where c._rid = SOME_RID' workload that prints throughput\n"
            + "\tQuerySingle - run a 'Select * from c where c.pk = SOME_PK' workload that prints throughput\n"
            + "\tQuerySingleMany - run a 'Select * from c where c.pk = \"pk\"' workload that prints throughput\n"
            + "\tQueryParallel - run a 'Select * from c' workload that prints throughput\n"
            + "\tQueryOrderby - run a 'Select * from c order by c._ts' workload that prints throughput\n"
            + "\tQueryAggregate - run a 'Select value max(c._ts) from c' workload that prints throughput\n"
            + "\tQueryAggregateTopOrderby - run a 'Select top 1 value count(c) from c order by c._ts' workload that prints throughput\n"
            + "\tQueryTopOrderby - run a 'Select top 1000 * from c order by c._ts' workload that prints throughput\n"
            + "\tMixed - runa workload of 90 reads, 9 writes and 1 QueryTopOrderby per 100 operations *\n"
            + "\tReadMyWrites - run a workflow of writes followed by reads and queries attempting to read the write.*\n"
            + "\n\t* writes 10k documents initially, which are used in the reads", converter = OperationTypeConverter.class)
    private Operation operation = Operation.WriteThroughput;

    @Parameter(names = "-concurrency", description = "Degree of Concurrency in Inserting Documents."
            + " If this value is not specified, the max connection pool size will be used as the concurrency level.")
    private Integer concurrency;

    @Parameter(names = "-numberOfOperations", description = "Total Number Of Documents To Insert")
    private int numberOfOperations = 100000;

    static class DurationConverter implements IStringConverter<Duration> {
        @Override
        public Duration convert(String value) {
            if (value == null) {
                return null;
            }

            return Duration.parse(value);
        }
    }

    @Parameter(names = "-maxRunningTimeDuration", description = "Max Running Time Duration", converter = DurationConverter.class)
    private Duration maxRunningTimeDuration;

    @Parameter(names = "-printingInterval", description = "Interval of time after which Metrics should be printed (seconds)")
    private int printingInterval = 10;

    @Parameter(names = "-numberOfPreCreatedDocuments", description = "Total Number Of Documents To pre create for a read workload to use")
    private int numberOfPreCreatedDocuments = 1000;
    
    @Parameter(names = "-preferredLocations", description = "Comma-separated list of Preferred regions")
    private List<String> preferredLocations;

    @Parameter(names = {"-h", "-help", "--help"}, description = "Help", help = true)
    private boolean help = false;

    enum Operation {
        ReadThroughput,
        WriteThroughput,
        ReadLatency,
        WriteLatency,
        QueryCross,
        QuerySingle,
        QuerySingleMany,
        QueryParallel,
        QueryOrderby,
        QueryAggregate,
        QueryAggregateTopOrderby,
        QueryTopOrderby,
        Mixed,
        ReadMyWrites;

        static Operation fromString(String code) {

            for (Operation output : Operation.values()) {
                if (output.toString().equalsIgnoreCase(code)) {
                    return output;
                }
            }

            return null;
        }

        static class OperationTypeConverter implements IStringConverter<Operation> {

            /*
             * (non-Javadoc)
             *
             * @see com.beust.jcommander.IStringConverter#convert(java.lang.String)
             */
            @Override
            public Operation convert(String value) {
                Operation ret = fromString(value);
                if (ret == null) {
                    throw new ParameterException("Value " + value + " can not be converted to ClientType. "
                                                         + "Available values are: " + Arrays.toString(Operation.values()));
                }
                return ret;
            }
        }
    }

    private static ConsistencyLevel fromString(String code) {
        for (ConsistencyLevel output : ConsistencyLevel.values()) {
            if (output.toString().equalsIgnoreCase(code)) {
                return output;
            }
        }
        return null;
    }

    static class ConsistencyLevelConverter implements IStringConverter<ConsistencyLevel> {

        /*
         * (non-Javadoc)
         *
         * @see com.beust.jcommander.IStringConverter#convert(java.lang.String)
         */
        @Override
        public ConsistencyLevel convert(String value) {
            ConsistencyLevel ret = fromString(value);
            if (ret == null) {
                throw new ParameterException("Value " + value + " can not be converted to ClientType. "
                                                     + "Available values are: " + Arrays.toString(Operation.values()));
            }
            return ret;
        }
    }

    Duration getMaxRunningTimeDuration() {
        return maxRunningTimeDuration;
    }

    Operation getOperationType() {
        return operation;
    }

    int getNumberOfOperations() {
        return numberOfOperations;
    }

    String getServiceEndpoint() {
        return serviceEndpoint;
    }

    String getMasterKey() {
        return masterKey;
    }

    boolean isHelp() {
        return help;
    }

    int getDocumentDataFieldSize() {
        return documentDataFieldSize;
    }

    ConnectionPolicy getConnectionPolicy() {
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.setConnectionMode(connectionMode);
        policy.setMaxPoolSize(maxConnectionPoolSize);
        policy.setPreferredLocations(preferredLocations != null ? preferredLocations : Collections.emptyList());
        return policy;
    }

    ConsistencyLevel getConsistencyLevel() {
        return consistencyLevel;
    }

    String getDatabaseId() {
        return databaseId;
    }

    String getCollectionId() {
        return collectionId;
    }

    int getNumberOfPreCreatedDocuments() {
        return numberOfPreCreatedDocuments;
    }

    int getPrintingInterval() {
        return printingInterval;
    }

    int getConcurrency() {
        if (this.concurrency != null) {
            return concurrency;
        } else {
            return this.maxConnectionPoolSize;
        }
    }

    boolean isUseNameLink() {
        return useNameLink;
    }

    public boolean isEnableJvmStats() {
        return enableJvmStats;
    }

    public MeterRegistry getAzureMonitorMeterRegistry() {
        String instrumentationKey = System.getProperty("cosmos.monitoring.azureMonitor.instrumentationKey",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("AZURE_INSTRUMENTATION_KEY")), null));
        return instrumentationKey == null ? null : this.azureMonitorMeterRegistry(instrumentationKey);
    }

    public MeterRegistry getGraphiteMeterRegistry() {
        String serviceAddress = System.getProperty("cosmos.monitoring.graphite.serviceAddress",
            StringUtils.defaultString(Strings.emptyToNull(
                System.getenv().get("GRAPHITE_SERVICE_ADDRESS")), null));
        return serviceAddress == null ? null : this.graphiteMeterRegistry(serviceAddress);
    }

    public String getGraphiteEndpoint() {
        if (graphiteEndpoint == null) {
            return null;
        }
        return StringUtils.substringBeforeLast(graphiteEndpoint, ":");
    }

    public int getGraphiteEndpointPort() {
        if (graphiteEndpoint == null) {
            return -1;
        }

        String portAsString = Strings.emptyToNull(StringUtils.substringAfterLast(graphiteEndpoint, ":"));
        if (portAsString == null) {
            return DEFAULT_GRAPHITE_SERVER_PORT;
        } else {
            return Integer.parseInt(portAsString);
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    void tryGetValuesFromSystem() {
        serviceEndpoint = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("SERVICE_END_POINT")),
                                                    serviceEndpoint);

        masterKey = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("MASTER_KEY")), masterKey);

        databaseId = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("DATABASE_ID")), databaseId);

        collectionId = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("COLLECTION_ID")),
                                                 collectionId);

        documentDataFieldSize = Integer.parseInt(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("DOCUMENT_DATA_FIELD_SIZE")),
                                          Integer.toString(documentDataFieldSize)));

        maxConnectionPoolSize = Integer.parseInt(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("MAX_CONNECTION_POOL_SIZE")),
                                          Integer.toString(maxConnectionPoolSize)));

        ConsistencyLevelConverter consistencyLevelConverter = new ConsistencyLevelConverter();
        consistencyLevel = consistencyLevelConverter.convert(StringUtils
                                                                     .defaultString(Strings.emptyToNull(System.getenv().get("CONSISTENCY_LEVEL")), consistencyLevel.name()));

        OperationTypeConverter operationTypeConverter = new OperationTypeConverter();
        operation = operationTypeConverter.convert(
                StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("OPERATION")), operation.name()));

        String concurrencyValue = StringUtils.defaultString(Strings.emptyToNull(System.getenv().get("CONCURRENCY")),
                                                            concurrency == null ? null : Integer.toString(concurrency));
        concurrency = concurrencyValue == null ? null : Integer.parseInt(concurrencyValue);

        String numberOfOperationsValue = StringUtils.defaultString(
                Strings.emptyToNull(System.getenv().get("NUMBER_OF_OPERATIONS")), Integer.toString(numberOfOperations));
        numberOfOperations = Integer.parseInt(numberOfOperationsValue);
    }

    private synchronized MeterRegistry azureMonitorMeterRegistry(String instrumentationKey) {

        if (this.azureMonitorMeterRegistry == null) {

            Duration step = Duration.ofSeconds(Integer.getInteger("cosmos.monitoring.azureMonitor.step", this.printingInterval));
            boolean enabled = !Boolean.getBoolean("cosmos.monitoring.azureMonitor.disabled");

            final AzureMonitorConfig config = new AzureMonitorConfig() {

                @Override
                @Nullable
                public String get(@Nullable String key) {
                    return null;
                }

                @Override
                @Nullable
                public String instrumentationKey() {
                    return instrumentationKey;
                }

                @Override
                public Duration step() {
                    return step;
                }

                @Override
                public boolean enabled() {
                    return enabled;
                }
            };

            this.azureMonitorMeterRegistry = new AzureMonitorMeterRegistry(config, Clock.SYSTEM);
        }

        return this.azureMonitorMeterRegistry;
    }

    @SuppressWarnings("UnstableApiUsage")
    private synchronized MeterRegistry graphiteMeterRegistry(String serviceAddress) {

        if (this.graphiteMeterRegistry == null) {

            HostAndPort address = HostAndPort.fromString(serviceAddress);

            String host = address.getHost();
            int port = address.getPortOrDefault(DEFAULT_GRAPHITE_SERVER_PORT);
            boolean enabled = !Boolean.getBoolean("cosmos.monitoring.graphite.disabled");
            Duration step = Duration.ofSeconds(Integer.getInteger("cosmos.monitoring.graphite.step", this.printingInterval));

            final GraphiteConfig config = new GraphiteConfig() {

                private String[] tagNames = { "source" };

                @Override
                @Nullable
                public String get(@Nullable String key) {
                    return null;
                }

                @Override
                public boolean enabled() {
                    return enabled;
                }

                @Override
                @Nullable
                public String host() {
                    return host;
                }

                @Override
                @Nullable
                public int port() {
                    return port;
                }

                @Override
                @Nullable
                public Duration step() {
                    return step;
                }

                @Override
                @Nullable
                public String[] tagsAsPrefix() {
                    return this.tagNames;
                }
            };

            this.graphiteMeterRegistry = new GraphiteMeterRegistry(config, Clock.SYSTEM);
            String source;

            try {
                PercentEscaper escaper = new PercentEscaper("_-", false);
                source = escaper.escape(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException error) {
                source = "unknown-host";
            }

            this.graphiteMeterRegistry.config()
                .namingConvention(NamingConvention.dot)
                .commonTags("source", source);
        }

        return this.graphiteMeterRegistry;
    }

    public boolean shouldContinue(long startTimeMillis, long iterationCount) {

        Duration maxDurationTime = getMaxRunningTimeDuration();
        int maxNumberOfOperations = getNumberOfOperations();

        if (maxDurationTime == null) {
            return iterationCount < maxNumberOfOperations;
        }

        if (startTimeMillis + maxDurationTime.toMillis() < System.currentTimeMillis()) {
            return false;
        }

        if (maxNumberOfOperations < 0) {
            return true;
        }

        return iterationCount < maxNumberOfOperations;
    }
}
