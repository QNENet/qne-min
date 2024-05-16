package com.qnenet.login;

public class QLoginUser {
    private char[] littlePwd;
    private QUserRole role;

    public QLoginUser(char[] littlePwd, QUserRole role) {
        this.littlePwd = littlePwd;
        this.role = role;
    }

    public char[] getLittlePwd() {
        return littlePwd;
    }

    public QUserRole getRole() {
        return role;
    }

}
