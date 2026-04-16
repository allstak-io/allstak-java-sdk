package dev.allstak.model;

import java.util.List;

public final class DatabaseQueryBatch {

    private final List<DatabaseQueryItem> queries;

    public DatabaseQueryBatch(List<DatabaseQueryItem> queries) {
        this.queries = queries;
    }

    public List<DatabaseQueryItem> getQueries() { return queries; }
}
