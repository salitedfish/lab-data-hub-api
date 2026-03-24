package com.labdatahub.component.s7_tcp;

@FunctionalInterface
public interface S7MessageConsumeHandler {
    void handle(String componentId, S7Message message) throws Exception;
}