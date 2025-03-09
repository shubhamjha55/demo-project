package com.demo.demo.Controller;

import com.demo.demo.DTO.LogResponse;
import com.demo.demo.Service.FileLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.*;

@Controller
public class FileLogController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    private FileLogService fileLogService;

    @Autowired
    public FileLogController(FileLogService fileLogService) {
        this.fileLogService = fileLogService;
    }

    @MessageMapping("/subscribe")
    public void sendInitialLogs(Principal principal) {
        String user = principal.getName();
        simpMessagingTemplate.convertAndSendToUser(user, "/last/logs", fileLogService.getLast10Logs());
    }
}
