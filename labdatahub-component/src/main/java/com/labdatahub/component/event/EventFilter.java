package com.labdatahub.component.event;

/**
 * @Description:
 * @Author: labdatahub
 * @CreateTime: 2025-09-25
 */
@FunctionalInterface
public interface EventFilter {
    boolean accept(Object event);
}