package com.pfs.util;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class CryptoHelper {
    public final static String SIGNATURE_ALGORITHM = "SHA256withDSA";
    public final static String KEY_PAIR_ALGORITHM = "DSA";

    public final static String getSHA256HashHex(String s) throws NoSuchAlgorithmException {
        if (s == null) {
            return null;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] shash = digest.digest(s.getBytes());
        String shashhex = Converter.bytesToHex(shash);
        return shashhex;
    }

    public static PrivateKey getPrivateKey(String privateKeyEncodedBase64) {
        PrivateKey privateKey = null;
        try {
            KeyFactory kf = KeyFactory.getInstance(KEY_PAIR_ALGORITHM);
            byte[] privateKeyBytes = Base64.decode(privateKeyEncodedBase64, Base64.NO_WRAP);
            privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return privateKey;
    }

    public static PublicKey getPublicKey(String publicKeyEncodedBase64) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(KEY_PAIR_ALGORITHM);
            byte[] encoded = Base64.decode(publicKeyEncodedBase64, Base64.NO_WRAP);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encoded);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return publicKey;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final static void generateKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException {
        //Creating KeyPair generator object
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        //Initializing the KeyPairGenerator
        keyPairGen.initialize(2048);
        //Generate the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();
        //Getting the private key from the key pair
        PrivateKey privKey = pair.getPrivate();
        //Getting the public key from the key pair
        PublicKey publicKey = pair.getPublic();

        String privateKeyS = Base64.encodeToString(privKey.getEncoded(), Base64.DEFAULT);
        String publicKeyS = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);

        KeyFactory kf = KeyFactory.getInstance(KEY_PAIR_ALGORITHM); // or "EC" or whatever
        PrivateKey privateKey2 = kf.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateKeyS, Base64.DEFAULT)));
        PublicKey publicKey2 = kf.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKeyS, Base64.DEFAULT)));

        if (privateKeyS.equals(Base64.encodeToString(privateKey2.getEncoded(), Base64.DEFAULT)) &&
                publicKeyS.equals(Base64.encodeToString(publicKey2.getEncoded(), Base64.DEFAULT))
        ) {
            System.out.printf("public key\n%s\nprivate key\n%s\nSUCCESS!!!", publicKeyS, privateKeyS);
        } else {
            System.out.printf("public key\n%s\n%s\nprivate key\n%s\n%s\nERROR!!!",
                    publicKeyS, Base64.encodeToString(publicKey2.getEncoded(), Base64.DEFAULT),
                    privateKeyS, Base64.encodeToString(privateKey2.getEncoded(), Base64.DEFAULT)
            );
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException {
        generateKeyPair();
    }

}
