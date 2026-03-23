package com.iscms.service;

public class LoginResult {

    public enum Status {
        SUCCESS, NOT_FOUND, WRONG_PASSWORD, LOCKED, SUSPENDED, ARCHIVED,
        SUGGEST_RESET, PENDING, REGISTRATION_FAILED, FROZEN, PASSIVE
    }

    private final Status status;
    private final Object user;
    private final int remainingTries;

    private LoginResult(Status status, Object user, int remainingTries) {
        this.status = status;
        this.user = user;
        this.remainingTries = remainingTries;
    }

    public static final LoginResult NOT_FOUND            = new LoginResult(Status.NOT_FOUND,            null, 0);
    public static final LoginResult LOCKED               = new LoginResult(Status.LOCKED,               null, 0);
    public static final LoginResult SUSPENDED            = new LoginResult(Status.SUSPENDED,            null, 0);
    public static final LoginResult ARCHIVED             = new LoginResult(Status.ARCHIVED,             null, 0);
    public static final LoginResult SUGGEST_RESET        = new LoginResult(Status.SUGGEST_RESET,        null, 0);
    public static final LoginResult PENDING              = new LoginResult(Status.PENDING,              null, 0);
    public static final LoginResult REGISTRATION_FAILED  = new LoginResult(Status.REGISTRATION_FAILED,  null, 0);
    public static final LoginResult FROZEN               = new LoginResult(Status.FROZEN,               null, 0);
    public static final LoginResult PASSIVE              = new LoginResult(Status.PASSIVE,              null, 0);

    public static LoginResult success(Object user) {
        return new LoginResult(Status.SUCCESS, user, 0);
    }

    public static LoginResult wrong(int remaining) {
        return new LoginResult(Status.WRONG_PASSWORD, null, remaining);
    }

    public boolean isSuccess()         { return status == Status.SUCCESS; }
    public Status  getStatus()         { return status; }
    public Object  getUser()           { return user; }
    public int     getRemainingTries() { return remainingTries; }
}