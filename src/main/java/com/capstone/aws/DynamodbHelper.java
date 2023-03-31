/*
 * DynamodbHelper
 * Version 1.0
 * April 01,2023
 * @author: Chika Jinanwa
 */

package com.capstone.aws;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.capstone.ppinetwork.PpiNetworkNode;

import java.util.*;


/**
 * TODO
 */
public class DynamodbHelper {
    private static final String TABLE_NAME = "PPI_NETWORK_TABLE";
    private static final String SORT_KEY_ATTRIBUTE = "datasetId";
    private static final String PARTITION_KEY_ATTRIBUTE = "id";
    private final LambdaLogger logger;
    private final AmazonDynamoDB ddbClient;

    public DynamodbHelper(Context context) {
        this.logger = context.getLogger();
        this.ddbClient = AmazonDynamoDBClientBuilder.standard().build();
    }

    public Map<String, PpiNetworkNode> getPPiNetworkItems(Optional<String> paginationToken) {
        return new HashMap<>();
    }

    public void createNewTable() {
        try {
            CreateTableResult result = ddbClient.createTable(
                    new CreateTableRequest()
                            .withAttributeDefinitions(
                                    new AttributeDefinition(SORT_KEY_ATTRIBUTE, ScalarAttributeType.S))
                            .withAttributeDefinitions(
                                    new AttributeDefinition(PARTITION_KEY_ATTRIBUTE, ScalarAttributeType.S))
                            .withKeySchema(
                                    new KeySchemaElement(SORT_KEY_ATTRIBUTE, KeyType.HASH),
                                    new KeySchemaElement(PARTITION_KEY_ATTRIBUTE, KeyType.RANGE))
                            .withProvisionedThroughput(
                                    new ProvisionedThroughput(Long.valueOf(10),
                                            Long.valueOf(10)))
                            .withTableName(TABLE_NAME));

            logger.log("Successfully created new table : " + result.getTableDescription().getTableName());
        } catch (AmazonServiceException e) {
            logger.log("Error creating new table : " + e.getErrorMessage());
        }

    }

    public void putItems(List<PpiNetworkNode> data, String datasetId) throws AmazonServiceException {
        DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
        try {
            for (PpiNetworkNode node : data) {
                PpiNodeDynamodbWrapper ddbNode = new PpiNodeDynamodbWrapper(datasetId, node.toBuilder());
                mapper.save(ddbNode);
            }
        } catch (AmazonDynamoDBException e) {
            e.getErrorMessage();
        }
    }

    public boolean ppiNetworkDataExists(String datasetID) {
        /* Since the bottleneck is in the latency of reaching the Dynamodb servers,
         * the operations: getting the item and checking its existence in the table
         * both have the same latencies. However, since we don't need to get all the
         * attributes to check for existence, we can maximize throughput by retrieving
         * only the partition key attribute.
         */
        Map<String, AttributeValue> keyToGet = Collections.singletonMap(
                SORT_KEY_ATTRIBUTE,
                new AttributeValue(datasetID));

        try {
            Map<String, AttributeValue> returnedItem = ddbClient
                    .getItem(
                            new GetItemRequest()
                                    .withKey(keyToGet)
                                    .withTableName(TABLE_NAME)
                                    .withProjectionExpression(SORT_KEY_ATTRIBUTE))
                    .getItem();
            if (returnedItem != null) {
                logger.log("Key : " + datasetID + "exists in table " + TABLE_NAME);
                return true;
            } else {
                logger.log("Key : " + datasetID + "does not exist " + TABLE_NAME);
                return false;
            }
        } catch (AmazonServiceException e) {
            logger.log("Error : " + e.getErrorMessage());
            // If there's an error retrieving the primary key, assume that the key doesn't exist.
            return false;
        }
    }

    public boolean ddTableExists(String tableName) {
        try {
            ddbClient.describeTable(TABLE_NAME).getTable();
            logger.log("Found table : " + TABLE_NAME);
            return true;
        } catch (ResourceNotFoundException e) {
            logger.log("Table : " + TABLE_NAME + " not found!");
            return false;
        }
    }

    /**
     * TODO
     */
    @DynamoDBTable(tableName = TABLE_NAME)
    public static class PpiNodeDynamodbWrapper extends PpiNetworkNode {
        private String sortKey;
        public PpiNodeDynamodbWrapper(String sortKey, PpiNetworkNode.Builder builder){
            super(builder);
            this.sortKey = sortKey;
        }

        @Override
        @DynamoDBHashKey(attributeName = PARTITION_KEY_ATTRIBUTE)
        public String getId() {
            return super.getId();
        }

        public void setId(String id) {
            super.toBuilder().setId(id);
        }

        @DynamoDBRangeKey(attributeName = SORT_KEY_ATTRIBUTE)
        public String getSortKey() {
            return sortKey;
        }

        public void setSortKey(String sortKey) {
            this.sortKey = sortKey;
        }

        @Override
        @DynamoDBAttribute(attributeName = "preferredName")
        public String getPreferredName() {
            return super.getPreferredName();
        }

        public void setPreferredName(String preferredName) {
            super.toBuilder().setPreferredName(preferredName);
        }

        @Override
        @DynamoDBAttribute(attributeName = "proteinSize")
        public Integer getProteinSize() {
            return super.getProteinSize();
        }

        public void setProteinSize(Integer proteinSize) {
            super.toBuilder().setProteinSize(proteinSize);
        }

        @Override
        @DynamoDBAttribute(attributeName = "annotation")
        public String getAnnotation() {
            return super.getAnnotation();
        }

        public void setAnnotation(String annotation) {
            super.toBuilder().setAnnotation(annotation);
        }

        @Override
        @DynamoDBAttribute(attributeName = "neighbors")
        @DynamoDBFlattened(attributes = {
                @DynamoDBAttribute(mappedBy = "id", attributeName = "id"),
                @DynamoDBAttribute(mappedBy = "score", attributeName = "score")})
        public List<Neighbor> getNeighbors() {
            return super.getNeighbors();
        }

        public void setNeighbors(List<Neighbor> neighbors) {
            super.toBuilder().setNeighbors(neighbors);
        }
    }
}
