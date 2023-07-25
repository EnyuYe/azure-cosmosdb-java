package com.microsoft.azure.cosmosdb;

public class QueryOptions extends FeedOptionsBase{
    private static Boolean enableScanInQuery;
    private static Boolean emitVerboseTracesInQuery;

    public QueryOptions() {}

    public QueryOptions(QueryOptions options) {
        super(options);
        this.enableScanInQuery = options.enableScanInQuery;
        this.emitVerboseTracesInQuery = options.emitVerboseTracesInQuery;
    }

    public static Boolean getEnableScanInQuery() {
        return enableScanInQuery;
    }

    public void setEnableScanInQuery(Boolean enableScanInQuery) {
        this.enableScanInQuery = enableScanInQuery;
    }

    public static Boolean getEmitVerboseTracesInQuery() {
        return emitVerboseTracesInQuery;
    }

    public void setEmitVerboseTracesInQuery(Boolean emitVerboseTracesInQuery) {
        this.emitVerboseTracesInQuery = emitVerboseTracesInQuery;
    }
}
