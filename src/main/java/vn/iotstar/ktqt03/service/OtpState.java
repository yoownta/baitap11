package vn.iotstar.ktqt03.service;

import java.time.Instant;

public class OtpState {
    private String username;
    private String email;
    private String code;
    private Instant expiresAt;
    private Instant sentAt;
    private int attempts;

    public OtpState() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public void incrementAttempts() { this.attempts++; }
}
