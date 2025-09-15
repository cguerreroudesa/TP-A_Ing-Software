package model;

import java.util.Random;

public class Session {
    private final int privateKey;
    private final int publicKey;

    public Session() {
        Random rnd = new Random();
        this.privateKey = rnd.nextInt(1000) + 1; // privada aleatoria
        this.publicKey  = privateKey * 2;        // pública relacionada
    }

    public int getPrivateKey() {
        return privateKey;
    }

    public int getPublicKey() {
        return publicKey;
    }

    public int signToken(int token) {
        return token * privateKey;
    }

    public boolean verifyToken(int token, int signedToken) {
        return signedToken == token * (publicKey / 2);
    }
}
