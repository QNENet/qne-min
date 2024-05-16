package com.qnenet.serialization;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QSerialization {

    private static final Logger log = LoggerFactory.getLogger(QSerialization.class);

    public static void saveObjToFile(Kryo kryo, Path filePath, Object obj) {
        try (Output output = new Output(new FileOutputStream(filePath.toFile()))) {
            kryo.writeObject(output, obj);
        } catch (Exception e) {
            log.error("Error occurred while saving object to file: " + e.getMessage());
        }
    }

    

    public static Object loadObjFromFile(Kryo kryo, Path filePath) {
        if (Files.notExists(filePath))

            return null;
        try {
            byte[] objBytes = FileUtils.readFileToByteArray(filePath.toFile());
            return objFromBytes(kryo, objBytes);
        } catch (IOException e) {
            logger.error("LoadObjFromFile Failed", true);
        }
    }

    public synchronized byte[] objToEncBytes(Kryo kryo, Object obj, AES256BinaryEncryptor encryptor) {
        byte[] plainBytes = objToBytes(kryo, obj);
        if (plainBytes == null)
            return null;
        byte[] encryptedBytes = encryptor.encrypt(plainBytes);
        // byte[] decrypt = encryptor.decrypt(encrypt);
        return encryptedBytes;
    }

    public synchronized Object objFromEncBytes(byte[] bytes, AES256BinaryEncryptor encryptor) {
        byte[] plainBytes = encryptor.decrypt(bytes);
        if (plainBytes == null)
            return null;
        return objFromBytes(plainBytes);
    }

    public synchronized boolean saveObjToEncFile(Path filePath, Object obj, AES256BinaryEncryptor encryptor) {
        try {
            byte[] objEncBytes = objToEncBytes(obj, encryptor);
            if (objEncBytes == null)
                return false;
            FileUtils.writeByteArrayToFile(filePath.toFile(), objEncBytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Object loadObjFromEncFile(Path filePath, AES256BinaryEncryptor encryptor) {
        if (Files.notExists(filePath))
            return null;
        try {
            byte[] encObjBytes = FileUtils.readFileToByteArray(filePath.toFile());
            return objFromEncBytes(encObjBytes, encryptor);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /////// Obj To Bytes
    /////// ///////////////////////////////////////////////////////////////////////////////////////////////////
    public synchronized static byte[] objToBytes(Kryo kryo, Object obj) {
        Logger log = LoggerFactory.getLogger(QSerialization.class);

    public synchronized byte[] objToBytes(Kryo kryo, Object obj) {
        try (Output output = new Output(4096, -1)) {
            kryo.writeClassAndObject(output, obj);
            byte[] bytes = output.toBytes();
            return bytes;
        } catch (Exception e) {
            log.error("Class Not Registered" + e.getMessage());
            return null;
        }
    }

    /////// Obj From Bytes
    /////// ////////////////////////////////////////////////////////////////////////////

    public static Object objFromBytes(Kryo kryo, byte[] bytes) {
        try (InputStream myInputStream = new ByteArrayInputStream(bytes)) {
            Input input = new Input(myInputStream);
            return kryo.readClassAndObject(input);
        } catch (KryoException e) {
            return e;
        } catch (IOException e) {
            log.error("objFromBytes IOException -> " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
