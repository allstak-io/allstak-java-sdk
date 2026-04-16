package dev.allstak.model;

import java.util.List;

public final class HttpRequestBatch {

    private final List<HttpRequestItem> requests;

    public HttpRequestBatch(List<HttpRequestItem> requests) {
        this.requests = requests;
    }

    public List<HttpRequestItem> getRequests() { return requests; }
}
