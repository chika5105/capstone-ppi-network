/*
 * PpiNetworkParser
 * Version 1.0
 * April 01,2023
 * @author: Chika Jinanwa
 */

package com.capstone.ppinetwork;

import com.capstone.ppinetwork.PpiNetworkNode.Neighbor;
import com.google.common.base.Charsets;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Utility class to parse protein interaction and accessory data of a particular organism from STRING DB
 * directly into a list of PpiNetworkNode objects.
 */
public class PpiNetworkParser {
    public  List<PpiNetworkNode> parseProteinData(URL interactionDataUrl, Optional<URL> accessoryDataUrl)
            throws IOException {
        /**
         * Get complete protein-interaction data for a particular organism.
         *
         * @param interactionDataUrl
         *            STRING DB url for the protein network data.
         *
         * @param accessoryDataUrl
         *            STRING DB url for metadata about each protein.
         *
         * @return List of protein-interaction data with metadata as a JSON marshallable object.
         *
         *         <pre>
         *
         *  result: [
         *  {
         *   "1280.SAXN108_1902": {
         *     "id": "1280.SAXN108_1902",
         *     "preferredName": "AID40343.1",
         *     "annotation": "annotation not available",
         *     "proteinSize": 104,
         *     "neighbors": [
         *       {
         *         "id": "1280.SAXN108_0935",
         *         "score": 196
         *       },
         *       ...
         *   ...
         * ]
         *         </pre>
         *
         * @throws IOException
         *             Error streaming data from STRING DB.
         */

        Map<String, PpiNetworkNode.Builder> accessoryData = accessoryDataUrl.isPresent() ? parseAccessoryData(
                createDataStream(accessoryDataUrl.get())) : new HashMap<>();
        Map<String, List<Neighbor>> networkData = parseNetworkData(createDataStream(interactionDataUrl));

        // Consolidate the two datasets into one.
        return new ArrayList<>(networkData.keySet())
                .parallelStream()
                .map(
                        key ->
                                accessoryData.containsKey(key)
                                        ? accessoryData
                                            .get(key)
                                            .setNeighbors(networkData.get(key))
                                            .build()
                                        : new PpiNetworkNode
                                            .Builder()
                                            .setId(key)
                                            .setNeighbors(networkData.get(key))
                                            .build())
                .collect(Collectors.toList());
    }

    private Map<String, List<Neighbor>> parseNetworkData(Reader ioStream)
            throws IOException {
        /** Processes the interaction data from STRING DB. */
        Map<String, List<Neighbor>> networkData = new HashMap<>();
        CSVReader reader = new CSVReaderBuilder(ioStream).build();

        for (Iterator<String[]> it = reader.iterator(); it.hasNext(); ) {
            String[] splitString = it.next()[0].split(" "); // Interaction data file is space-delimited.
            String key = splitString[0].trim();

            // Ignore csv headers.
            if (key.contains("protein1")) {
                continue;
            }
            Neighbor neighbor = Neighbor.create(
                    /* id = */    splitString[1].trim(),
                    /* score = */ Integer.parseInt(splitString[2].trim()));
            if (networkData.containsKey(key)) {
                networkData.get(key).add(neighbor);
            } else {
                networkData.put(key, new ArrayList<>(List.of(neighbor)));
            }
        }

        reader.close();
        return networkData;
    }

    private Map<String, PpiNetworkNode.Builder> parseAccessoryData(Reader ioStream)
            throws IOException{
        Map<String, PpiNetworkNode.Builder> proteinData = new HashMap<>();
        CSVReader reader = new CSVReaderBuilder(ioStream).build();

        for (Iterator<String[]> it = reader.iterator(); it.hasNext(); ) {
            String[] splitString = it.next()[0].split("\t"); // Accessory data file is tab delimited.
            String key = splitString[0].trim();
            // Ignore csv headers.
            if (key.contains("string_protein_id")) {
                continue;
            }

            String preferredName = splitString[1].trim();
            Integer proteinSize = new Integer(splitString[2].trim());
            String annotation = splitString[3].trim();

            proteinData.put(key, new PpiNetworkNode.Builder()
                    .setId(key)
                    .setAnnotation(annotation)
                    .setProteinSize(proteinSize)
                    .setPreferredName(preferredName)
            );
        }

        reader.close();
        return proteinData;
    }

    private Reader createDataStream(URL url) throws IOException {
        /** IOStream to read data directly from the STRING DB URL. */
        return new BufferedReader(
                new InputStreamReader(
                        new GZIPInputStream(
                                url.openStream()), // STRING file is GZipped, so unzip.
                        Charsets.UTF_8));
    }
}
