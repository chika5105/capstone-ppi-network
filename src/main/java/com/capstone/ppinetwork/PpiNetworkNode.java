/*
 * PpiNetworkNode
 * Version 1.0
 * April 01,2023
 * @author: Chika Jinanwa
 */

package com.capstone.ppinetwork;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single node in the protein-interaction network with its relevant attributes.
 * The builder design pattern is used to instantiate objects of the PpiNetworkNode to easily construct the complex
 * object step by step. The creation and assemblage of the parts of the PpiNetworkNode is encapsulated in a separate
 * Builder object without directly creating the class.
 * Reference: https://en.wikipedia.org/wiki/Builder_pattern#:~:text=The%20builder%20pattern%20is%20a,Gang%20of%20Four%20design%20patterns.
 */

public class PpiNetworkNode {
    private final String id;
    private final String preferredName;
    private final String annotation;
    private final Integer proteinSize;
    private final List<Neighbor> neighbors;

    protected PpiNetworkNode(Builder builder) {
        this.id = builder.id;
        this.preferredName = builder.preferredName;
        this.annotation = builder.annotation;
        this.proteinSize = builder.proteinSize;
        this.neighbors = builder.neighbors;
    }


    public String getId() {
        return id;
    }

    public String getPreferredName() {
        return preferredName;
    }

    public Integer getProteinSize() {
        return proteinSize;
    }

    public String getAnnotation() {
        return annotation;
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }


    public PpiNetworkNode.Builder toBuilder() {
        PpiNetworkNode.Builder builder = new Builder();

        if (id != null) {
            builder.setId(id);
        }
        if (preferredName != null) {
            builder.setPreferredName(preferredName);
        }
        if (annotation != null) {
            builder.setAnnotation(annotation);
        }
        if (proteinSize != null) {
            builder.setProteinSize(proteinSize);
        }
        if (neighbors != null) {
            builder.setNeighbors(neighbors);
        }
        return builder;
    }


    public static class Builder {
        private List<Neighbor> neighbors;
        private String id;
        private String preferredName;
        private String annotation;
        private Integer proteinSize;

        public Builder() {
            this.neighbors = new ArrayList<>();
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setPreferredName(String preferredName) {
            this.preferredName = preferredName;
            return this;
        }

        public Builder setAnnotation(String annotation) {
            this.annotation = annotation;
            return this;
        }

        public Builder setProteinSize(Integer proteinSize) {
            this.proteinSize = proteinSize;
            return this;
        }

        public Builder setNeighbors(List<Neighbor> neighbors) {
            this.neighbors = neighbors;
            return this;
        }

        public Builder addNeighbors(List<Neighbor> neighbors) {
            for (Neighbor neighbor : neighbors) {
                addNeighbor(neighbor);
            }
            return this;
        }

        public Builder addNeighbor(Neighbor neighbor) {
            this.neighbors.add(neighbor);
            return this;
        }

        public PpiNetworkNode build() throws IllegalStateException {
            validate();
            return new PpiNetworkNode(this);
        }

        private void validate() throws IllegalStateException {
            List<String> errors = new ArrayList<>();

            if (id == null) {
                errors.add("Protein Id cannot be null");
            }
            if (neighbors == null) {
                errors.add("Neighbors cannot be null, use empty collection object instead");
            }

            if (!errors.isEmpty()) {
                throw new IllegalStateException(errors.toString());
            }
        }
    }


    // Reference: https://chromium.googlesource.com/external/github.com/google/auto/+/auto-value-1.0/value/README.md
    @AutoValue
    protected abstract static class Neighbor {
        /** Represents a single instance of a node's neighbor. */
        static Neighbor create(String id, Integer score) {
            return new AutoValue_PpiNetworkNode_Neighbor(id, score);
        }

        abstract String getId();
        abstract Integer getScore(); // STRING DB's confidence score for an edge connection.
    }
}
