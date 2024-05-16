package com.qnenet.qne.objects.impl;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;

import com.qnenet.qne.network.upnp.QGatewayDevice;
import com.qnenet.qne.objects.classes.*;
import com.qnenet.qne.objects.impl.customserializers.QPathSerializer;
import com.qnenet.qne.objects.impl.customserializers.QUUIDSerializer;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QNEObjects {

    private static int lastClassId;
    private ConcurrentHashMap<String, Integer> classIdByClassName = new ConcurrentHashMap<>();

    Logger log = LoggerFactory.getLogger(QNEObjects.class);

    private Pool<Kryo> kryoPool;

    public QNEObjects() {
        System.out.println("QNEObjects started");

        Kryo kryo = getKryo();
        for (int i = 0; i <= lastClassId; i++) {
            Registration reg = kryo.getRegistration(i);
            if (reg == null) {
                System.out.println("ClassId Fail -> " + i);
                continue;
            }
            classIdByClassName.put(reg.getType().getName(), i);
        }

        kryoPool = new Pool<Kryo>(true, false, 8) {
            protected Kryo create() {
                return getKryo();
            }
        };
    }

    private Kryo getKryo() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(true);

        kryo.register(int.class, 0);
        kryo.register(String.class, 1);
        kryo.register(float.class, 2);
        kryo.register(boolean.class, 3);
        kryo.register(byte.class, 4);
        kryo.register(char.class, 5);
        kryo.register(short.class, 6);
        kryo.register(long.class, 7);
        kryo.register(double.class, 8);
        kryo.register(void.class, 9);
        kryo.register(LocalDateTime.class, 10);
        kryo.register(int[].class, 11);
        kryo.register(Hashtable.class, 12);
        kryo.register(LinkedHashSet.class, 13);
        kryo.register(ArrayList.class, 14);
        kryo.register(byte[].class, 15);
        kryo.register(String[].class, 16);
        kryo.register(LocalDate.class, 17);
        kryo.register(HashMap.class, 18);
        kryo.register(ConcurrentHashMap.class, 19);
        kryo.register(char[].class, 20);
        kryo.register(Integer[].class, 21);
        kryo.register(Properties.class, 22);
        kryo.register(Vector.class, 23);
        kryo.register(CopyOnWriteArrayList.class, 24);
        kryo.register(byte[][].class, 25);
        kryo.register(Instant.class, 26);
        kryo.register(BitSet.class, 27);
        kryo.register(UUID.class, new QUUIDSerializer(), 28);
        kryo.register(QStoreObjectKey.class, 29);
        kryo.register(QStoreObject.class, 30);
        kryo.register(QEPAddrPair.class, 31);
        kryo.register(QPayload.class, 32);
        kryo.register(QEndPointProps.class, 33);
        kryo.register(QEndPointRestartInfo.class, 34);
        kryo.register(QChannelInfo.class, 35);
        kryo.register(Path.class, new QPathSerializer(), 36);
        kryo.register(QEPId.class, 37);
        kryo.register(QMappedPort.class, 38);
        kryo.register(QUser.class, 39);
        kryo.register(QGrantedAuthority.class, 40);
        kryo.register(QGatewayDevice.class, 41);
        kryo.register(QInetAddr.class, 42);
        kryo.register(QInetSocketAddr.class, 43);
        kryo.register(QSegmentItem.class, 44);
        kryo.register(QNetworkStructure.class, 45);
        lastClassId = 45; // must be set and updated after new addition
        return kryo;
    }


//    private static Kryo getKryo() {
//        Kryo kryo = new Kryo();
//        kryo.setRegistrationRequired(true);
//
//        kryo.register(int.class, 0);
//        kryo.register(String.class, 1);
//        kryo.register(float.class, 2);
//        kryo.register(boolean.class, 3);
//        kryo.register(byte.class, 4);
//        kryo.register(char.class, 5);
//        kryo.register(short.class, 6);
//        kryo.register(long.class, 7);
//        kryo.register(double.class, 8);
//        kryo.register(void.class, 9);
//        kryo.register(LocalDateTime.class, 10);
//        kryo.register(int[].class, 11);
//        kryo.register(Hashtable.class, 12);
//        kryo.register(LinkedHashSet.class, 13);
//        kryo.register(ArrayList.class, 14);
//        kryo.register(byte[].class, 15);
//        kryo.register(String[].class, 16);
//        kryo.register(LocalDate.class, 17);
//        kryo.register(HashMap.class, 18);
//        kryo.register(ConcurrentHashMap.class, 19);
//        kryo.register(char[].class, 20);
//        kryo.register(Integer[].class, 21);
//        kryo.register(Properties.class, 22);
//        kryo.register(Vector.class, 23);
//        kryo.register(CopyOnWriteArrayList.class, 24);
//        kryo.register(byte[][].class, 25);
//        kryo.register(Instant.class, 26);
//        kryo.register(BitSet.class, 27);
//        kryo.register(UUID.class, new QUUIDSerializer(), 28);
//        kryo.register(QStoreObjectKey.class, 29);
//        kryo.register(QStoreObject.class, 30);
//        kryo.register(QEndPointInfo.class, 31);
//        kryo.register(QNoiseKeypair.class, 32);
//        kryo.register(QStoreInfo.class, 33);
//        kryo.register(QSegmentInfo.class, 34);
//        lastClassId = 34; // must be set and updated after new addition
//        return kryo;
//    }

    public void free(Kryo kryo) {
        kryoPool.free(kryo);
    }


///////////////////////////////////////////////////////////////////////////////////////////////////
/////// Serialization /////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////


    public synchronized void saveObjToFile(Path filePath, Object obj) {
        try {
            byte[] objBytes = objToBytes(obj);
            FileUtils.writeByteArrayToFile(filePath.toFile(), objBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized Object loadObjFromFile(Path filePath) {
        if (Files.notExists(filePath)) return null;
        try {
            byte[] objBytes = FileUtils.readFileToByteArray(filePath.toFile());
            return objFromBytes(objBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public synchronized byte[] objToEncBytes(Object obj, AES256BinaryEncryptor encryptor) {
        byte[] plainBytes = objToBytes(obj);
        if (plainBytes == null) return null;
        byte[] encryptedBytes = encryptor.encrypt(plainBytes);
//        byte[] decrypt = encryptor.decrypt(encrypt);
        return encryptedBytes;
    }


    public synchronized Object objFromEncBytes(byte[] bytes, AES256BinaryEncryptor encryptor) {
        byte[] plainBytes = encryptor.decrypt(bytes);
        if (plainBytes == null) return null;
        return objFromBytes(plainBytes);
    }


    public synchronized boolean saveObjToEncFile(Path filePath, Object obj, AES256BinaryEncryptor encryptor) {
        try {
            byte[] objEncBytes = objToEncBytes(obj, encryptor);
            if (objEncBytes == null) return false;
            FileUtils.writeByteArrayToFile(filePath.toFile(), objEncBytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public synchronized Object loadObjFromEncFile(Path filePath, AES256BinaryEncryptor encryptor) {
        if (Files.notExists(filePath)) return null;
        try {
            byte[] encObjBytes = FileUtils.readFileToByteArray(filePath.toFile());
            return objFromEncBytes(encObjBytes, encryptor);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

/////// Obj To Bytes ///////////////////////////////////////////////////////////////////////////////////////////////////


    public synchronized byte[] objToBytes(Object obj) {
        Kryo kryo = kryoPool.obtain();
        try (Output output = new Output(4096, -1)) {
            kryo.writeClassAndObject(output, obj);
            byte[] bytes = output.toBytes();
            return bytes;
        } catch (Exception e) {
            log.error("Class Not Registered" + e.getMessage());
            return null;
        } finally {
            free(kryo);
        }
    }

/////// Obj From Bytes ////////////////////////////////////////////////////////////////////////////


    public synchronized Object objFromBytes(byte[] bytes) {
        Kryo kryo = kryoPool.obtain();
        try (InputStream myInputStream = new ByteArrayInputStream(bytes)) {
            Input input = new Input(myInputStream);
            return kryo.readClassAndObject(input);
        } catch (KryoException e) {
            return e;
        } catch (IOException e) {
            log.error("objFromBytes IOException -> " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            free(kryo);
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////
/////// Utils /////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////


    public Integer getClassId(Class clazz) {
        String name = clazz.getName();
        return classIdByClassName.get(name);
    }

    public boolean ifNotExists(Path path) {
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


    public String longZeros(long value, int paddingLength) {
        return String.format("%0" + paddingLength + "d", value);
    }


    public String int2(int val) {
        return String.format("%02d", val);
    }

    public String int3(int val) {
        return String.format("%03d", val);
    }

    public String int4(int val) {
        return String.format("%04d", val);
    }

    public String int6(int val) {
        return String.format("%06d", val);
    }

    public String int8(int val) {
        return String.format("%08d", val);
    }

    public String long19(long val) {
        return longZeros(val, 19);
    }

//    public int getObjClassId(Object obj) {
//        return classIdMap.get(obj.getClass());
//    }


///////////////////////////////////////////////////////////////////////////////////////////////////
} /////// End Class ///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////
