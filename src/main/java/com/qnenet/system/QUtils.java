package com.qnenet.system;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.util.Random;

public class QUtils {

     public static boolean checkDirectory(Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

        public static KeyPair createECDSAKeyPair(String keyPairAlgorithm) { // e.g. P-256, P-384
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyPairGenerator.initialize(new ECGenParameterSpec(keyPairAlgorithm));
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }


    public static char[] generatePasswordChars(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()_+[{]}|;:,<.>";
        char[] options = characters.toCharArray();
        char[] result = new char[length];
        Random r = new SecureRandom();
        for (int i = 0; i < result.length; i++) {
            result[i] = options[r.nextInt(options.length)];
        }
        System.out.println("Harder Password -> " + new String(result));

        return result;
    }

    public static char[] generateLittlePassword(int length) {
        // easy input on mobile, no numbers
        String characters = "abcdefghijklmnopqrstuvwxyz";
        char[] options = characters.toCharArray();
        char[] result = new char[length];
        Random r = new SecureRandom();
        for (int i = 0; i < result.length; i++) {
            result[i] = options[r.nextInt(options.length)];
        }

        System.out.println("Simple Password -> " + new String(result));
        return result;
    }

 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////  KeyStore /////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static KeyStore createKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("PKCS12", "BC");
            keyStore.load(null, null);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return keyStore;
    }

    public static KeyStore saveKeyStore(Path keyStoreFilePath, KeyStore keyStore, char[] ksPwd) {
        try {
            FileOutputStream fos = new FileOutputStream(keyStoreFilePath.toFile());
            keyStore.store(fos, ksPwd);
            fos.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return keyStore;
    }


    public static KeyStore loadKeyStore(Path keyStoreFilePath, char[] ksPwd) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream(keyStoreFilePath.toFile());
            keyStore.load(fis, ksPwd);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return keyStore;

    }

    public static Object isReallyHeadless() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isReallyHeadless'");
    }

}
