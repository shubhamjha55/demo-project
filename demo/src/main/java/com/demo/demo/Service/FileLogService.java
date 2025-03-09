package com.demo.demo.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Component
public class FileLogService {
    private final static String FILE_NAME = "logs.txt";
    private final static String READ_MODE = "r";
    public static final String DESTINATION = "/last/logs";

    private long offset;
    private final RandomAccessFile randomAccessFile;
    private final Queue<String> last10Logs = new LinkedList<>(); // Fixed-size queue

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public FileLogService() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
        randomAccessFile = new RandomAccessFile(file, READ_MODE);
        offset = randomAccessFile.length();

        // Load last 10 logs into queue when the server starts
        loadLast10Logs();
    }

    @Scheduled(fixedDelay = 100, initialDelay = 10000)
    public synchronized void sendRecentUpdates() throws IOException {
        List<String> recentUpdates = new ArrayList<>();
        long fileLength = randomAccessFile.length();
        randomAccessFile.seek(offset);

        while (randomAccessFile.getFilePointer() < fileLength) {
            String currentLine = randomAccessFile.readLine();
            if (currentLine != null) {
                recentUpdates.add(currentLine);

                // Maintain last 10 logs queue
                last10Logs.add(currentLine);
                if (last10Logs.size() > 10) {
                    last10Logs.poll(); // Remove oldest entry
                }
            }
        }

        if (!recentUpdates.isEmpty()) {
            simpMessagingTemplate.convertAndSend(DESTINATION, recentUpdates);
        }
        offset = fileLength;
    }

    public List<String> getLast10Logs() {
        return new ArrayList<>(last10Logs);
    }

    private synchronized void loadLast10Logs() throws IOException {
        List<String> lastLogs = generateLast10Logs();
        last10Logs.addAll(lastLogs);
    }

    private List<String> generateLast10Logs() throws IOException {
        List<String> lastTenLines = new ArrayList<>();
        long fileLength = randomAccessFile.length();
        long pointer = fileLength - 1;
        int linesAdded = 0;
        StringBuilder currentLine = new StringBuilder();

        while (pointer >= 0 && linesAdded < 10) {
            randomAccessFile.seek(pointer);
            int currentByte = randomAccessFile.readByte();
            if (currentByte == '\n') {
                if (!currentLine.isEmpty()) {
                    currentLine.reverse();
                    lastTenLines.add(currentLine.toString());
                    currentLine.setLength(0);
                    linesAdded++;
                }
            } else if (currentByte != '\r') {
                currentLine.append((char) currentByte);
            }
            pointer--;
        }

        if (lastTenLines.size() < 10 && !currentLine.isEmpty()) {
            currentLine.reverse();
            lastTenLines.add(currentLine.toString());
        }
        offset = fileLength;
        Collections.reverse(lastTenLines);
        return lastTenLines;
    }

    @PreDestroy
    public void cleanup() throws IOException {
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }
}
