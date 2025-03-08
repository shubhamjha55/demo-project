package com.demo.demo.Service;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class FileLogService {
    private final static String FILE_NAME = "logs.txt";
    private final static String READ_MODE = "r";
    public static final String DESTINATION = "/last/logs";
    private long offset;

    private final RandomAccessFile randomAccessFile;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public FileLogService() throws IOException {
        File file = new File(FILE_NAME);

        // If file does not exist, create it
        if (!file.exists()) {
            file.createNewFile();
        }

        randomAccessFile = new RandomAccessFile(file, READ_MODE);
        offset = randomAccessFile.length();
    }

    @Scheduled(fixedDelay = 100, initialDelay = 10000)
    public synchronized void sendRecentUpdates() throws IOException {
        List<String> recentUpdates = new ArrayList<>();
        long fileLength = randomAccessFile.length();
        randomAccessFile.seek(offset);

        while(randomAccessFile.getFilePointer() < fileLength) {
            String currentLine = randomAccessFile.readLine();
            recentUpdates.add((currentLine));
        }
        if(recentUpdates.size() > 0) {
            simpMessagingTemplate.convertAndSend(DESTINATION, recentUpdates);
        }
        offset = fileLength;
    }

    public synchronized List<String> initialize() throws IOException {
        List<String> lastTenLines = new ArrayList<>();

        long fileLength = randomAccessFile.length();
        long pointer = fileLength - 1;
        int linesAdded = 0;

        StringBuilder currentLine = new StringBuilder();

        while(pointer >= 0 && linesAdded < 10) {
            randomAccessFile.seek(pointer);
            int currentByte = randomAccessFile.readByte();
            if(currentByte == '\n') {
                if(!currentLine.isEmpty()) {
                    currentLine.reverse();
                    lastTenLines.add(currentLine.toString());

                    currentLine.setLength(0);
                    linesAdded += 1;
                }
            } else if (currentByte != '\r'){
                currentLine.append((char) currentByte);
            }
            pointer--;
        }
        if(lastTenLines.size() < 10 && !currentLine.isEmpty()) {
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
