/*
 * Main
 * Version 1.0
 * April 01,2023
 * @author: Chika Jinanwa
 */

package com.capstone.local;

import com.capstone.ppinetwork.PpiNetworkNode;
import com.capstone.ppinetwork.PpiNetworkParser;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

/**
 * Entrypoint to run the parser locally. Outputs the parsed data into a JSON file in the root directory.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        URL interactionUrl = new URL(
                "https://stringdb-static.org/download/protein.links.v11.5/1280.protein.links.v11.5.txt.gz");
        Optional<URL> accessoryDataUrl = Optional.of(
                new URL("https://stringdb-static.org/download/protein.info.v11.5/1280.protein.info.v11.5.txt.gz"));
        List<PpiNetworkNode> data = new PpiNetworkParser().parseProteinData(interactionUrl, accessoryDataUrl);
        new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create()
                .toJson(data, new FileWriter("protein_data.json"));
        System.out.println(
                "Done preprocessing the dataset, you can see the processed dataset as a file called protein_data.json");

    }
}
