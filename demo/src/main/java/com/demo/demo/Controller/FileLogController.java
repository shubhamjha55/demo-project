package com.demo.demo.Controller;

import com.demo.demo.DTO.LogResponse;
import com.demo.demo.Service.FileLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class FileLogController {

    private FileLogService fileLogService;

    @Autowired
    public FileLogController(FileLogService fileLogService) {
        this.fileLogService = fileLogService;
    }

    @GetMapping("/initial")
    public ResponseEntity<LogResponse> getLast10Lines() {
        try {
            List<String> last10Logs = fileLogService.initialize();
            return ResponseEntity.ok(new LogResponse(last10Logs));
        }
        catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LogResponse(Collections.emptyList()));
        }
    }
}
