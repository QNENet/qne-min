package com.qnenet.login;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

public class QLoginUsers {

    @Autowired
    Kryo systemKryo;

    private ConcurrentHashMap<char[], QLoginUser> loginUsers;

    public QLoginUsers() {
        loginUsers = new ConcurrentHashMap<>();
    }

    public void saveLoginUsers(String filename) {

        Output output = null;
        try {
            output = new Output(new FileOutputStream(filename));
            systemKryo.writeObject(output, loginUsers);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void loadLoginUsers(String filename) {
        Kryo kryo = new Kryo();
        Input input = null;
        try {
            input = new Input(new FileInputStream(filename));
            loginUsers = kryo.readObject(input, ConcurrentHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    public void addLoginUser(char[] littlePwd, QLoginUser loginUser) {
        loginUsers.put(littlePwd, loginUser);
    }

    public QLoginUser getLoginUser(char[] littlePwd) {
        return loginUsers.get(littlePwd);
    }

    public void removeLoginUser(char[] littlePwd) {
        loginUsers.remove(littlePwd);
    }

    public boolean containsLoginUser(char[] littlePwd) {
        return loginUsers.containsKey(littlePwd);
    }

    public boolean containsLoginUser(QLoginUser loginUser) {
        return loginUsers.containsValue(loginUser);
    }

    public boolean isEmpty() {
        return loginUsers.isEmpty();
    }

    public int size() {
        return loginUsers.size();
    }

    public void clear() {
        loginUsers.clear();
    }

    public ConcurrentHashMap<char[], QLoginUser> getLoginUsers() {
        return loginUsers;
    }

}
