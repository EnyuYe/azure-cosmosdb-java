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

package com.microsoft.azure.cosmosdb;
/**
 * Specifies the options associated with feed methods (enumeration operations) in the Azure Cosmos DB database service.
 */
public final class FeedOptions extends FeedOptionsBase {
    private String sessionToken;
    private String partitionKeyRangeId;
    private Boolean enableCrossPartitionQuery;
    private int responseContinuationTokenLimitInKb;
    private boolean allowEmptyPages;

    public FeedOptions() {}

    public FeedOptions(FeedOptions options) {
        super(options);
        this.sessionToken = options.sessionToken;
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.enableCrossPartitionQuery = options.enableCrossPartitionQuery;
        this.responseContinuationTokenLimitInKb = options.responseContinuationTokenLimitInKb;
        this.allowEmptyPages = options.allowEmptyPages;
    }

    /**
     * Gets the partitionKeyRangeId.
     *
     * @return the partitionKeyRangeId.
     */
    public String getPKRangeId() {
        return this.partitionKeyRangeId;
    }

    /**
     * Sets the partitionKeyRangeId.
     *
     * @param partitionKeyRangeId
     *           the partitionKeyRangeId.
     */
    public void setPartitionKeyRangeIdInternal(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
    }

    /**
     * Gets the session token for use with session consistency.
     *
     * @return the session token.
     */
    public String getSessionToken() {
        return this.sessionToken;
    }

    /**
     * Sets the session token for use with session consistency.
     *
     * @param sessionToken
     *            the session token.
     */
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    /**
     * Gets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @return whether to allow queries to run across all partitions of the
     *         collection.
     */
    public Boolean getEnableCrossPartitionQuery() {
        return this.enableCrossPartitionQuery;
    }

    /**
     * Sets the option to allow queries to run across all partitions of the
     * collection.
     *
     * @param enableCrossPartitionQuery
     *            whether to allow queries to run across all partitions of the
     *            collection.
     */
    public void setEnableCrossPartitionQuery(Boolean enableCrossPartitionQuery) {
        this.enableCrossPartitionQuery = enableCrossPartitionQuery;
    }

    /**
     * Sets the ResponseContinuationTokenLimitInKb request option for document query requests
     * in the Azure Cosmos DB service.
     *
     * ResponseContinuationTokenLimitInKb is used to limit the length of continuation token in the query response.
     * Valid values are &gt;= 1.
     *
     * The continuation token contains both required and optional fields.
     * The required fields are necessary for resuming the execution from where it was stooped.
     * The optional fields may contain serialized index lookup work that was done but not yet utilized.
     * This avoids redoing the work again in subsequent continuations and hence improve the query performance.
     * Setting the maximum continuation size to 1KB, the Azure Cosmos DB service will only serialize required fields.
     * Starting from 2KB, the Azure Cosmos DB service would serialize as much as it could fit till it reaches the maximum specified size.
     *
     * @param limitInKb continuation token size limit.
     */
    public void setResponseContinuationTokenLimitInKb(int limitInKb) {
        this.responseContinuationTokenLimitInKb = limitInKb;
    }

    /**
     * Gets the ResponseContinuationTokenLimitInKb request option for document query requests
     * in the Azure Cosmos DB service. If not already set returns 0.
     *
     * ResponseContinuationTokenLimitInKb is used to limit the length of continuation token in the query response.
     * Valid values are &gt;= 1.
     *
     * @return return set ResponseContinuationTokenLimitInKb, or 0 if not set
     */
    public int getResponseContinuationTokenLimitInKb() {
        return responseContinuationTokenLimitInKb;
    }

    /**
     * Gets the option to allow empty result pages in feed response.
     */
    public boolean getAllowEmptyPages() {
        return allowEmptyPages;
    }

    /**
     * Sets the option to allow empty result pages in feed response. Defaults to false
     * @param allowEmptyPages whether to allow empty pages in feed response
     */
    public void setAllowEmptyPages(boolean allowEmptyPages) {
        this.allowEmptyPages = allowEmptyPages;
    }
}
