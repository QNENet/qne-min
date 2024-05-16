/**
 * The QSystem class represents the system component of the QNE application.
 * It provides various functionalities related to system properties, encryption,
 * and file paths.
 */
package com.qnenet.system;

import jakarta.annotation.PostConstruct;

import jakarta.annotation.PreDestroy;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.util.binary.AES256BinaryEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import com.qnenet.constants.QSysConstants;
import com.qnenet.serialization.QSerialization;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The QSystem class represents the system component of the QNE application.
 * It provides various functionalities related to system properties, encryption,
 * and file paths.
 */
@Service
public class QSystem {

    private static final Logger log = LoggerFactory.getLogger(QSystem.class);

    private ExecutorService executor;

    private Map<String, Object> sysPropsMap;

    private KeyStore keyStore;

    private Kryo systemKryo;

    private char[] littlePwd;

    private char[] keyStorePwd;

    private AES256BinaryEncryptor littleEncryptor;

    private AES256BinaryEncryptor bigEncryptor;

    private Path userHomePath;

    private Path qnePath;

    private Path repositoryPath;

    private Path keystoreFilePath;

    private Path installerPropsFilePath;

    private Path installPath;

    private Path systemPath;

    private Path sysPropsMapFilePath;

    private ConcurrentHashMap<String, Object> systemProps;

    /**
     * Constructs a new instance of the QSystem class.
     *
     * @throws IOException if an I/O error occurs.
     */
    public QSystem() throws IOException {
        Security.addProvider(new BouncyCastleProvider());
        executor = getExecutor();
        systemKryo = new Kryo();
        userHomePath = Paths.get(System.getProperty("user.home"));
        qnePath = Paths.get(userHomePath.toString(), QSysConstants.APP_NAME);
        repositoryPath = Paths.get(qnePath.toString(), "repository");
        systemPath = Paths.get(qnePath.toString(), "system");
        keystoreFilePath = Paths.get(repositoryPath.toString(), "keystore.p12");
        installPath = Paths.get(qnePath.toString(), "install");
        installerPropsFilePath = Paths.get(System.getProperty("user.home"), QSysConstants.APP_NAME, "install",
                "installer.props");
        sysPropsMapFilePath = Paths.get(systemPath.toString(), "sysProps.map");
    }

    /**
     * Returns the ExecutorService used by the QSystem.
     * If the executor is already initialized, it returns the existing executor.
     * Otherwise, it creates a new executor using the
     * Executors.newVirtualThreadPerTaskExecutor() method.
     *
     * @return the ExecutorService used by the QSystem
     */
    public ExecutorService getExecutor() {
        if (executor != null)
            return executor;
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * This method is annotated with @PostConstruct and is called after the bean has
     * been constructed,
     * but before any dependencies have been injected. It checks if the endpoints
     * directory exists,
     * and if it does, it calls the newSystem() method. Otherwise, it calls the
     * restart() method.
     */
    @PostConstruct
    public void postConstruct() {
        if (QUtils.checkDirectory(qnePath)) {
            newSystem();
        } else {
            restart();
        }
    }

    }

    /**
     * This method creates a new system by setting up a new system with a new key
     * pair,
     * generating a new password, and saving the system properties map.
     */
    private void newSystem() {
        executor.submit(() -> {
            setupNewSystem();
        });
    }

    private void setupNewSystem() {
        systemProps = new ConcurrentHashMap<>();
        createIfNecessaryAndLoadSysPasswords();

        KeyPair ecdsaKeyPair = QUtils.createECDSAKeyPair("P-256");
        sysPropsMap.put(QSysConstants.SSL_PRIVATE_KEY_BYTES, ecdsaKeyPair.getPrivate().getEncoded());
        sysPropsMap.put(QSysConstants.SSL_PUBLIC_KEY_BYTES, ecdsaKeyPair.getPublic().getEncoded());

        littleEncryptor = new AES256BinaryEncryptor();
        littleEncryptor.setPasswordCharArray(littlePwd);

        char[] bigPwd = QUtils.generatePasswordChars(16);
        sysPropsMap.put(QSysConstants.BIG_PWD, bigPwd);
        bigEncryptor = new AES256BinaryEncryptor();
        bigEncryptor.setPasswordCharArray(bigPwd);

    
        sysPropsMap.put(QSysConstants.IS_HEADLESS, QUtils.isReallyHeadless());

        if (Files.notExists(keystoreFilePath)) {
            keyStore = QUtils.createKeyStore();
            QUtils.saveKeyStore(null, keyStore, bigPwd)saveKeystore();
        }



        saveSysPropsMap();

    }

    //////// Restart
    //////// //////////////////////////////////////////////////////////////////////////////////
    //////// Restart
    //////// //////////////////////////////////////////////////////////////////////////////////
    //////// Restart
    //////// //////////////////////////////////////////////////////////////////////////////////

    public void restart() {
        executor.submit(() -> {
            restartSystemStage2();
        });
    }

    private void restartSystemStage2() {
        createIfNecessaryAndLoadSysPasswords();
        littleEncryptor = new AES256BinaryEncryptor();
        littleEncryptor.setPasswordCharArray(littlePwd);
        loadSysPropsMap();

        bigEncryptor = new AES256BinaryEncryptor();
        bigEncryptor.setPasswordCharArray((char[]) sysPropsMap.get(QSysConstants.BIG_PWD));

        loadKeystore();

        // usersManager.init(this);

        // loadEndPointsRestartInfo();
        // openEndPoints();

        // startServers();

    }

    private void createIfNecessaryAndLoadSysPasswords() {
        Path littlePwdFilePath = Paths.get(repositoryPath.toString(), "littlepwd.prop");
        Path keyStorePwdFilePath = Paths.get(repositoryPath.toString(), "keystorepwd.prop");

        try {
            if (Files.notExists(littlePwdFilePath)) {
                littlePwd = QUtils.generateLittlePassword(4);
                FileUtils.writeStringToFile(littlePwdFilePath.toFile(), new String(littlePwd),
                        Charset.defaultCharset());
                keyStorePwd = QUtils.generatePasswordChars(16);
                FileUtils.writeStringToFile(keyStorePwdFilePath.toFile(), new String(keyStorePwd),
                        Charset.defaultCharset());
            } else {
                littlePwd = FileUtils.readFileToString(littlePwdFilePath.toFile(), Charset.defaultCharset())
                        .toCharArray();
                keyStorePwd = FileUtils.readFileToString(keyStorePwdFilePath.toFile(), Charset.defaultCharset())
                        .toCharArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //////// Pre Destroy
    //////// //////////////////////////////////////////////////////////////////////////////
    //////// Pre Destroy
    //////// //////////////////////////////////////////////////////////////////////////////
    //////// Pre Destroy
    //////// //////////////////////////////////////////////////////////////////////////////

    @PreDestroy
    public void destroy() {
        System.out.println("QSystem Callback triggered - @PreDestroy.");
    }

    /////////// Getters & Setters
    /////////// /////////////////////////////////////////////////////////////////////
    /////////// Getters & Setters
    /////////// /////////////////////////////////////////////////////////////////////
    /////////// Getters & Setters
    /////////// /////////////////////////////////////////////////////////////////////

    public Map<String, Object> getSysPropsMap() {
        return sysPropsMap;
    }

    // public ExecutorService getExecutor() {
    // return executor;
    // }

    public AES256BinaryEncryptor getBigEncryptor() {
        return bigEncryptor;
    }

    public KeyStore getKeystore() {
        return keyStore;
    }

    /////////// ///////////////////////////////////////////////////////////////////////////
    /////////// Serialization
    /////////// ///////////////////////////////////////////////////////////////////////////

    public void saveObjToFile(Pool<Kryo> kryoPool, Path filePath, Object obj) {
        executor.submit(() -> {
            try (Output output = new Output(new FileOutputStream(filePath.toFile()))) {
                Kryo kryo = kryoPool.obtain();
                kryo.writeObject(output, obj);
                kryoPool.free(kryo);
            } catch (Exception e) {
                log.error("Error occurred while saving object to file: " + e.getMessage());
            }
        });
    }

    public Object loadObjFromFile(Pool<Kryo> kryoPool, Path filePath) {
        executor.submit(() -> {
            if (Files.notExists(filePath))
                return null;
            try (Input input = new Input(new FileInputStream(filePath.toFile()))) {
                Kryo kryo = kryoPool.obtain();
                Object obj = kryo.readObject(input, Object.class);
                kryoPool.free(kryo);
                return obj;
            } catch (Exception e) {
                log.error("Error occurred while loading object from file: " + e.getMessage());
                return null;
            }
        });
    }

    public byte[] objToEncBytes(Pool<Kryo> kryoPool, Object obj, AES256BinaryEncryptor encryptor) {
        executor.submit(() -> {
            try (Output output = new Output(4096, -1)) {
                Kryo kryo = kryoPool.obtain();
                kryo.writeClassAndObject(output, obj);
                byte[] plainBytes = output.toBytes();
                kryoPool.free(kryo);
                if (plainBytes == null)
                    return null;
                byte[] encryptedBytes = encryptor.encrypt(plainBytes);
                return encryptedBytes;
            } catch (Exception e) {
                log.error("Error occurred while converting object to encrypted bytes: " + e.getMessage());
                return null;
            }
        });
    }

    public Object objFromEncBytes(Pool<Kryo> kryoPool, byte[] bytes, AES256BinaryEncryptor encryptor) {
        executor.submit(() -> {
            try (InputStream myInputStream = new ByteArrayInputStream(encryptor.decrypt(bytes))) {
                Input input = new Input(myInputStream);
                Kryo kryo = kryoPool.obtain();
                Object obj = kryo.readClassAndObject(input);
                kryoPool.free(kryo);
                return obj;
            } catch (KryoException e) {
                log.error("Error occurred while converting encrypted bytes to object: " + e.getMessage());
                return null;
            } catch (IOException e) {
                log.error("Error occurred while converting encrypted bytes to object: " + e.getMessage());
                return null;
            }
        });
    }

    public boolean saveObjToEncFile(Pool<Kryo> kryoPool, Path filePath, Object obj, AES256BinaryEncryptor encryptor) {
    executor.submit(() -> {
        try {
            byte[] objEncBytes = objToEncBytes(kryoPool, obj, encryptor);
            if (objEncBytes == null)
                return false;
            FileUtils.writeByteArrayToFile(filePath.toFile(), objEncBytes);
            return true;
        } catch (IOException e) {
            log.error("Error occurred while saving object to encrypted file: " + e.getMessage());
            return false;
        }
    }); 
    
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

    /////////// ///////////////////////////////////////////////////////////////////////////
    /////////// Persistance
    /////////// ///////////////////////////////////////////////////////////////////////////

    // private void saveSysPropsMap() {
    // qobjs.saveObjToEncFile(qnePaths.getSysPropsMapFilePath(), sysPropsMap,
    // littleEncryptor);
    // }

    // @SuppressWarnings("unchecked")
    // private void loadSysPropsMap() {
    // sysPropsMap = (Map<String, Object>)
    // systemKryo.loadObjFromEncFile(qnePaths.getSysPropsMapFilePath(),
    // littleEncryptor);
    // }

    // private void saveKeystore() {
    // QUtils.saveKeyStore(qnePaths.getKeystoreFilePath(), keyStore, keyStorePwd);
    // }

    // private void loadKeystore() {
    // keyStore = QUtils.loadKeyStore(qnePaths.getKeystoreFilePath(), keyStorePwd);
    // }

    /////////// Utilities
    /////////// /////////////////////////////////////////////////////////////////////////////
    /////////// Utilities
    /////////// /////////////////////////////////////////////////////////////////////////////
    /////////// Utilities
    /////////// /////////////////////////////////////////////////////////////////////////////

    /////////// End Class
    /////////// /////////////////////////////////////////////////////////////////////////////
} ///////// End Class
  ///////// /////////////////////////////////////////////////////////////////////////////
  /////////// End Class
  ///////// /////////////////////////////////////////////////////////////////////////////
