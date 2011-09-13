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

package com.bccapi.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.security.GeneralSecurityException;

import com.bccapi.api.APIException;
import com.bccapi.api.AccountInfo;
import com.bccapi.api.Network;
import com.bccapi.api.SendCoinForm;
import com.bccapi.core.Account;
import com.bccapi.core.AddressUtil;
import com.bccapi.core.BccScript.BccScriptException;
import com.bccapi.core.BitcoinClientApiImpl;
import com.bccapi.core.CoinUtils;
import com.bccapi.core.DeterministicECKeyManager;
import com.bccapi.core.ECKeyManager;
import com.bccapi.core.SeedManager;
import com.bccapi.core.SeedManager.SeedGenerationTask;
import com.bccapi.core.SendCoinFormSummary;
import com.bccapi.core.SendCoinFormValidator;

/**
 * This client is a simple example of how you might create a Bitcoin client
 * using the BCCAPI. It uses the {@link DeterministicECKeyManager}, which means
 * that all keys in our wallet are generated from a strong seed which we keep in
 * an encrypted file.
 */
public class SimpleClient {

   public static void main(String[] args) {
      try {
         if (args.length != 1) {
            usage();
         }
         if (args[0].toLowerCase().equals("testnet")) {
            clientMain(Network.testNetwork);
         } else if (args[0].toLowerCase().equals("prodnet")) {
            clientMain(Network.productionNetwork);
         } else {
            usage();
         }
      } catch (Throwable t) {
         t.printStackTrace();
         print(t.getMessage());
      }
   }

   private static void usage() {
      print("Usage: SimpleClient <network>");
      print("where:");
      print("   <network> must be either \"testnet\" or \"prodnet\"");
      System.exit(1);
   }

   private static void clientMain(Network network) throws Exception {
      String seedFile;
      URL url;
      if (network.equals(Network.productionNetwork)) {
         seedFile = "seed.bin";
         url = new URL("https://prodnet.bccapi.com:443");
      } else {
         seedFile = "test-seed.bin";
         url = new URL("https://testnet.bccapi.com:444");
      }
      // Determine seed for key manager
      byte[] seed = initializeOrLoadSeedFile(seedFile);

      // Create key manager
      ECKeyManager keyManager = new DeterministicECKeyManager(seed);

      // Create BCCAPI instance
      BitcoinClientApiImpl api = new BitcoinClientApiImpl(url, network);

      // Create account instance
      Account account = new Account(keyManager, api);

      // Login. On first login we create an account on the server side
      // automatically
      print("Connecting to wallet ...");
      account.login();

      // Get the account info from the server and make sure that we have at
      // least one key in our wallet.
      if (account.getInfo().getKeys() == 0) {
         // upload first wallet public key
         account.addKey();
      }

      while (true) {
         printOptions();
         String option = readLine();
         if (option.equals("1")) {
            showBalance(account);
         } else if (option.equals("2")) {
            showAdresses(account);
         } else if (option.equals("3")) {
            createNewAddress(account);
         } else if (option.equals("4")) {
            sendCoins(account);
         } else if (option.equals("5")) {
            return;
         } else {
            print("Invalid Option");
         }
      }
   }

   private static byte[] initializeOrLoadSeedFile(String seedFilePath) throws IOException, GeneralSecurityException {
      File seedFile = new File(seedFilePath);
      if (!seedFile.exists()) {
         // No seed file found, first time we start the client
         return initializeSeedFile(seedFilePath);
      } else {
         // Seed file found, load the PIN protected file
         return loadSeed(seedFilePath);
      }
   }

   private static byte[] initializeSeedFile(String seedPath) throws IOException, GeneralSecurityException {
      print("");
      print(" === Initializing Client for first use ===");
      print("");
      print("The client is about to be initialized for first use. All keys managed by");
      print("this client are generated from a strong seed which is based on a user");
      print("entered passphrase and salt. Both of these values should be memorized or");
      print("written down and kept in a secure place. You will only ever need these");
      print("values again if you lose your client device and want to recover your");
      print("wallet.");
      print("");
      print("The longer the passphrase the stronger the seed. The passphrase you enter");
      print("can be any length and contain any characters.");
      print("");
      printnln("Enter passphrase:");
      String passphrase = readLine();
      print("");
      print("The salt you enter is not secret. For maximum security it should be a");
      print("unique value which is not used by other clients. Enter something you can");
      print("remember such as your email address.");
      print("");
      printnln("Enter salt:");
      String salt = readLine();
      print("");
      print("Seed generation takes about 2 minutes on a Samsung Galaxy S, and a few");
      print("seconds on a standard desktop computer. This happens only during initialization.");
      print("The seed is stored encrypted in the file seed.bin in the current");
      print("working directory.");
      print("Generating strong seed ...");
      // The depth parameter determines how much CPU time we spend generating
      // the seed. The higher the number the longer time it takes to generate,
      // and the harder it will be to brute force the seed.
      int depth = 20;
      SeedGenerationTask task = SeedManager.getSeedGenerationTask(passphrase, salt, depth);
      while(!task.isFinished()){
         print(task.getProgress()+"%");
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            // ignore
         }
      }
      byte[] seed = task.getSeed();
      print("");
      print("The PIN you enter is used for encrypting the seed file which is kept on");
      print("your client device. You will have to enter it every time you use the client.");
      print("");
      printnln("Enter PIN:");
      String pin = readLine();
      OutputStream output = new FileOutputStream(seedPath);
      SeedManager.savePinProtectedSeed(pin, salt, output, seed);
      print("");
      print("If you at some point believe that someone has obtained a copy of the");
      print("iencrypted seed file in your client device you should immediately transfer");
      print("your funds to another client and never use the same PIN or passphrase.");
      print("");
      print("Initialization complete.");
      printnln("Hit Enter:");
      readLine();
      return seed;
   }

   private static byte[] loadSeed(String seedPath) throws IOException, GeneralSecurityException {
      byte[] seed = null;
      while (true) {
         printnln("Enter PIN:");
         String pin = readLine();
         InputStream input = new FileInputStream(seedPath);
         seed = SeedManager.loadPinProtectedSeed(pin, input);
         input.close();
         if (seed != null) {
            break;
         }
         System.out.println("Invalid PIN.");
      }
      return seed;
   }

   private static void printOptions() {
      print("=================");
      print("==   Options   ==");
      print("=================");
      print("1. Get wallet balance");
      print("2. My receiving addresses");
      print("3. Create new receiving address");
      print("4. Send coins");
      print("5. Exit");
      printnln("Enter option:");
   }

   private static void showBalance(Account account) throws APIException, IOException {
      AccountInfo info = account.getInfo();
      String estimatedBalance = CoinUtils.valueString(info.getEstimatedBalance());
      String availableBalance = CoinUtils.valueString(info.getAvailableBalance());
      System.out.println("Balance: " + estimatedBalance + "(" + availableBalance + ") BTC");
   }

   private static void showAdresses(Account account) throws APIException, IOException {
      StringBuilder sb = new StringBuilder();
      sb.append("Addresses: ");
      boolean first = true;
      for (String address : account.getAddresses()) {
         if (first) {
            sb.append(address).append('\n');
            first = false;
            continue;
         }
         sb.append("           ").append(address).append('\n');
      }
      System.out.print(sb.toString());
   }

   private static void createNewAddress(Account account) throws APIException, IOException {
      print("Adding public key to wallet");
      account.addKey();
   }

   private static void sendCoins(Account account) throws IOException, BccScriptException {
      print("Sending coins.");

      // Receiving address
      printnln("Enter receiving address: ");
      String address = readLine();
      if (!AddressUtil.validateAddress(address, account.getNetwork())) {
         print("Invalid address");
         return;
      }

      // Amount to send
      printnln("Enter amount to send in BTC: ");
      Long toSend;
      try {
         BigDecimal amount = new BigDecimal(readLine());
         // Convert to satoshis
         amount = amount.multiply(new BigDecimal(100000000));
         toSend = amount.longValue();
      } catch (NumberFormatException e) {
         print("Invalid value entered.");
         return;
      }

      // Fee
      printnln("Enter fee for miner in BTC (hit enter for zero): ");
      Long fee;
      try {
         String feeString = readLine();
         if (feeString.length() == 0) {
            fee = 0L;
         } else {
            BigDecimal amount = new BigDecimal(feeString);
            // Convert to satoshis
            amount = amount.multiply(new BigDecimal(100000000));
            fee = amount.longValue();
         }
      } catch (NumberFormatException e) {
         print("Invalid value entered.");
         return;
      }

      SendCoinForm form;
      try {
         form = account.getSendCoinForm(address, toSend, fee);
      } catch (APIException e) {
         print(e.getMessage());
         return;
      }

      // Validate that the form matches what we actually requested, and that the
      // server does not cheat on us
      if (!SendCoinFormValidator.validate(form, account, toSend, fee, address)) {
         print("Warning: Invalid SendCoinForm received from BCCAPI server ");
         return;
      }

      // Print out a summary of the form
      print("==== Summary of coins being sent ====");
      System.out.println(new SendCoinFormSummary(form, account));
      printnln("Hit enter to send coins.");
      readLine();
      try {
         account.signAndSubmitSendCoinForm(form);
      } catch (APIException e) {
         print(e.getMessage());
         return;
      }
      print("Coins sent.");
   }

   private static String readLine() throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      return in.readLine();
   }

   private static void print(String s) {
      System.out.println(s);
   }

   private static void printnln(String s) {
      System.out.print(s);
   }

}
