package dev.allstak.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public final class UserContext {

    private final String id;
    private final String email;
    private final String ip;

    private UserContext(String id, String email, String ip) {
        this.id = id;
        this.email = email;
        this.ip = ip;
    }

    public static UserContext of(String id, String email, String ip) {
        return new UserContext(id, email, ip);
    }

    public static UserContext ofId(String id) {
        return new UserContext(id, null, null);
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getIp() { return ip; }
}
