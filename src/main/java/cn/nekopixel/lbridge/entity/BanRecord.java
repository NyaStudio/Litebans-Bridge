package cn.nekopixel.lbridge.entity;

public class BanRecord {
    private Long id;
    private String uuid;
    private String ip;
    private String reason;
    private String bannedByUuid;
    private String bannedByName;
    private String removedByUuid;
    private String removedByName;
    private String removedByReason;
    private Long time;
    private Long until;
    private String serverScope;
    private String serverOrigin;
    private boolean silent;
    private boolean ipban;
    private boolean ipbanWildcard;
    private boolean active;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getBannedByUuid() {
        return bannedByUuid;
    }

    public void setBannedByUuid(String bannedByUuid) {
        this.bannedByUuid = bannedByUuid;
    }

    public String getBannedByName() {
        return bannedByName;
    }

    public void setBannedByName(String bannedByName) {
        this.bannedByName = bannedByName;
    }

    public String getRemovedByUuid() {
        return removedByUuid;
    }

    public void setRemovedByUuid(String removedByUuid) {
        this.removedByUuid = removedByUuid;
    }

    public String getRemovedByName() {
        return removedByName;
    }

    public void setRemovedByName(String removedByName) {
        this.removedByName = removedByName;
    }

    public String getRemovedByReason() {
        return removedByReason;
    }

    public void setRemovedByReason(String removedByReason) {
        this.removedByReason = removedByReason;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getUntil() {
        return until;
    }

    public void setUntil(Long until) {
        this.until = until;
    }

    public String getServerScope() {
        return serverScope;
    }

    public void setServerScope(String serverScope) {
        this.serverScope = serverScope;
    }

    public String getServerOrigin() {
        return serverOrigin;
    }

    public void setServerOrigin(String serverOrigin) {
        this.serverOrigin = serverOrigin;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public boolean isIpban() {
        return ipban;
    }

    public void setIpban(boolean ipban) {
        this.ipban = ipban;
    }

    public boolean isIpbanWildcard() {
        return ipbanWildcard;
    }

    public void setIpbanWildcard(boolean ipbanWildcard) {
        this.ipbanWildcard = ipbanWildcard;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}