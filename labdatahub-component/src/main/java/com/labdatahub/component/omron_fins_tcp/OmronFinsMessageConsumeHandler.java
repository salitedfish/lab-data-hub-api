package com.labdatahub.component.omron_fins_tcp;

@FunctionalInterface
public interface OmronFinsMessageConsumeHandler {
    void handle(String componentId, OmronFinsMessage message) throws Exception;
}