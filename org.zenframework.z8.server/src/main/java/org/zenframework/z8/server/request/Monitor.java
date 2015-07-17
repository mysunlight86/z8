package org.zenframework.z8.server.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zenframework.z8.server.engine.ApplicationServer;
import org.zenframework.z8.server.json.Json;
import org.zenframework.z8.server.json.parser.JsonArray;
import org.zenframework.z8.server.json.parser.JsonObject;
import org.zenframework.z8.server.logs.Trace;
import org.zenframework.z8.server.types.file;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.utils.ErrorUtils;

public class Monitor extends RequestTarget implements IMonitor {
    private file outputFile;
    private file logFile;

    private List<Message> logMessages = new ArrayList<Message>();
    private List<Message> monitorMessages = new ArrayList<Message>();
    private List<String> queries = new ArrayList<String>();
    private Map<String, List<guid>> records = new HashMap<String, List<guid>>();

    public Monitor() {
        super(guid.create().toString());
    }

    public Monitor(String id) {
        super(id);
    }

    protected void collectLogMessages() {
        logMessages.addAll(monitorMessages);
    }

    @Override
    public void print(String text) {
        Trace.logEvent(text);
        monitorMessages.add(new Message(text));
    }

    @Override
    public void print(file file) {
        this.outputFile = file;
    }

    @Override
    public file getLog() {
        return logFile;
    }

    @Override
    public void log(String text) {
        if(logFile == null) {
            logFile = new file();
        }

        logFile.write(new Message(text) + file.EOL);
    }

    @Override
    public void log(Throwable exception) {
        String message = ErrorUtils.getMessage(exception) + "\r\n" + ErrorUtils.getStackTrace(exception);
        log(message);
    }

    public String[] getMessages() {
        String[] result = new String[monitorMessages.size()];

        for(int i = 0; i < monitorMessages.size(); i++) {
            result[i] = monitorMessages.get(i).text();
        }

        return result;
    }

    protected void clearMessages() {
        monitorMessages.clear();
    }

    @Override
    public void refresh(String queryId) {
        if(!queries.contains(queryId)) {
            queries.add(queryId);
            records.remove(queryId);
        }
    }

    @Override
    public void refresh(String queryId, guid recordId) {
        if(!queries.contains(queryId)) {
            List<guid> ids = records.get(queryId);

            if(ids == null) {
                ids = new ArrayList<guid>();
                records.put(queryId, ids);
            }

            if(!ids.contains(recordId)) {
                ids.add(recordId);
            }
        }
    }

    @Override
    public void writeResponse(JsonObject writer) {
        
        JsonObject refreshObj = new JsonObject();

        refreshObj.put(Json.queries, new JsonArray(queries));

        JsonObject recordsObj = new JsonObject();
        for(String queryId : records.keySet()) {
            recordsObj.put(queryId, new JsonArray(records.get(queryId)));
        }
        
        refreshObj.put(Json.records, recordsObj);

        writer.put(Json.refresh, refreshObj);

        writer.put(Json.serverId, ApplicationServer.Id);

        if (outputFile != null) {
            writer.put(Json.source, outputFile.getRelativePath().replace('\\', '/'));
        }

        writer.writeInfo(getMessages(), logFile);
        
    }
}