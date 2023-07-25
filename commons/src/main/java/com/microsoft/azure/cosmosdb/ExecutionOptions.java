package com.microsoft.azure.cosmosdb;

public class ExecutionOptions extends FeedOptionsBase{
    private static int maxDegreeOfParallelism;
    private static int maxBufferedItemCount;

    public ExecutionOptions(){}

    public ExecutionOptions(ExecutionOptions options) {
        super(options);
        this.maxDegreeOfParallelism = options.maxDegreeOfParallelism;
        this.maxBufferedItemCount = options.maxBufferedItemCount;
    }

    public static int getMaxDegreeOfParallelism() {
        return maxDegreeOfParallelism;
    }

    public static void setMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        ExecutionOptions.maxDegreeOfParallelism = maxDegreeOfParallelism;
    }

    public static int getMaxBufferedItemCount() {
        return maxBufferedItemCount;
    }

    public static void setMaxBufferedItemCount(int maxBufferedItemCount) {
        ExecutionOptions.maxBufferedItemCount = maxBufferedItemCount;
    }
}
