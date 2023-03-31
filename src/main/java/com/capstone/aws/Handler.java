package com.capstone.aws;

/*
 * Handler
 * Version 1.0
 * April 01,2023
 * @author: Chika Jinanwa
 */

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.capstone.ppinetwork.PpiNetworkParser;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Map;

/**
 * TODO
 * Handler value: com.capstone.aws.Handler
 */
public class Handler implements RequestHandler<Map<String,String>, String>{
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    @Override
    public String handleRequest(Map<String, String> event, Context context){
        LambdaLogger logger = context.getLogger();

        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        // process event
        logger.log("EVENT: " + gson.toJson(event));
        logger.log("EVENT TYPE: " + event.getClass());


        URL interactionDataUrl;
        try {
            interactionDataUrl = new URL(event.get("interactionDataUrl"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }


        Optional<URL> accessoryDataUrl;
        try {
            accessoryDataUrl = event.get("accessoryDataUrl") != null
                    ? Optional.of(new URL(event.get("accessoryDataUrl")))
                    : Optional.empty();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            DynamodbHelper ddbHelper = new DynamodbHelper(context);
            ddbHelper.createNewTable();
            ddbHelper.putItems(new PpiNetworkParser().parseProteinData(interactionDataUrl, accessoryDataUrl),
                    interactionDataUrl + " " + accessoryDataUrl);
            return "Done putting items in db";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
