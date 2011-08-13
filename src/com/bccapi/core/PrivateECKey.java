/**
 * Copyright 2011 bccapi.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bccapi.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import com.bouncycastle.asn1.DERInteger;
import com.bouncycastle.asn1.DERSequenceGenerator;
import com.bouncycastle.crypto.AsymmetricCipherKeyPair;
import com.bouncycastle.crypto.generators.ECKeyPairGenerator;
import com.bouncycastle.crypto.params.ECKeyGenerationParameters;
import com.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.bouncycastle.crypto.signers.ECDSASigner;

/**
 * A private elliptic curve key used in the Bitcoin system.
 */
public class PrivateECKey {

   private final BigInteger _privateKey;
   private final PublicECKey _publicKey;

   /**
    * Create a private key based on a PRNG.
    * 
    * @param randomSource
    *           The PRNG to use.
    */
   public PrivateECKey(PRNG randomSource) {
      ECKeyGenerationParameters params = new ECKeyGenerationParameters(PublicECKey.ecParams, randomSource);
      ECKeyPairGenerator generator = new ECKeyPairGenerator();
      generator.init(params);
      AsymmetricCipherKeyPair keypair = generator.generateKeyPair();
      ECPrivateKeyParameters privParams = (ECPrivateKeyParameters) keypair.getPrivate();
      _privateKey = privParams.getD();
      ECPublicKeyParameters pubParams = (ECPublicKeyParameters) keypair.getPublic();
      _publicKey = new PublicECKey(pubParams.getQ().getEncoded());
   }

   /**
    * Generate a signature on an array of bytes using this private key.
    * 
    * @param input
    *           The bytes to sign
    * @return The signature.
    */
   public byte[] sign(byte[] input) {
      ECDSASigner signer = new ECDSASigner();
      ECPrivateKeyParameters params = new ECPrivateKeyParameters(_privateKey, PublicECKey.ecParams);
      signer.init(true, params);
      BigInteger[] signature = signer.generateSignature(input);
      try {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         DERSequenceGenerator sequenceGenerator = new DERSequenceGenerator(stream);
         sequenceGenerator.addObject(new DERInteger(signature[0]));
         sequenceGenerator.addObject(new DERInteger(signature[1]));
         sequenceGenerator.close();
         return stream.toByteArray();
      } catch (IOException e) {
         // This never happens
         throw new RuntimeException(e);
      }
   }

   /**
    * Get the public key corresponding to the private key.
    * 
    * @return The public key corresponding to the private key.
    */
   public PublicECKey getPublicKey() {
      return _publicKey;
   }

}
