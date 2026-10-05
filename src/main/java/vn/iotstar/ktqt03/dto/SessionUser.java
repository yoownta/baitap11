package vn.iotstar.ktqt03.dto;

public class SessionUser {
    private String username;
    private String fullName;
    private boolean admin;

    public SessionUser(String username, String fullName, boolean admin) {
        this.username = username;
        this.fullName = fullName;
        this.admin = admin;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public boolean isAdmin() { return admin; }
}
