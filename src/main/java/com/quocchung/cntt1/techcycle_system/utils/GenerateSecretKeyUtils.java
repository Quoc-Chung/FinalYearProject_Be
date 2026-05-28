package com.quocchung.cntt1.techcycle_system.utils;

import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

public class GenerateSecretKeyUtils {

    public static void main(String[] args) {
      String secret = Encoders.BASE64.encode(
          Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512)
              .getEncoded()
      );

      System.out.println(secret);
    }
}
