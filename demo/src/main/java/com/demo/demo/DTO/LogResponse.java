package com.demo.demo.DTO;

import java.util.List;

public class LogResponse {
    private final List<String> logs;

    public LogResponse(List<String> logs) {
        this.logs = logs;
    }

    public List<String> getLogs() {
        return logs;
    }
}
