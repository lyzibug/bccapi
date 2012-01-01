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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import com.bccapi.api.APIException;
import com.bccapi.api.AccountInfo;
import com.bccapi.api.AccountStatement;
import com.bccapi.api.AccountStatement.Record;
import com.bccapi.api.AccountStatement.Record.Type;
import com.bccapi.api.Network;
import com.bccapi.api.SendCoinForm;
import com.bccapi.core.Account;
import com.bccapi.core.AddressUtil;
import com.bccapi.core.BccScript.BccScriptException;
import com.bccapi.core.BitcoinClientApiImpl;
import com.bccapi.core.CoinUtils;
import com.bccapi.core.DeterministicECKeyExporter;
import com.bccapi.core.DeterministicECKeyManager;
import com.bccapi.core.ECKeyManager;
import com.bccapi.core.SeedManager;
import com.bccapi.core.SeedManager.SeedGenerationTask;
import com.bccapi.core.SendCoinFormSummary;
import com.bccapi.core.SendCoinFormValidator;
import com.bccapi.core.StringUtils;

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
         clientMain(args[0]);
      } catch (Throwable t) {
         t.printStackTrace();
         print(t.getMessage());
      }
   }

   private static void usage() {
      print("Usage: SimpleClient <network>");
      print("where:");
      print("   <network> must be either \"testnet\", \"prodnet\", or \"closedtestnet\"");
      print("");
      print("Use \"prodnet\" to connect to the official Bitcoin production network.");
      print("");
      print("Use \"testnet\" to connect to the official Bitcoin test network. This is");
      print("useful for testing a BCCAPI client that can send/receive coins from other");
      print("client types using the test network. Obtain free test coins from the");
      print("Testnet Fauchet at \"http://testnet.freebitcoins.appspot.com/\". The test");
      print("network may be unstable at times, as the number of mining power varies a");
      print("lot.");
      print("");
      print("Use \"closedtestnet\" to connect to a closed Bitcoin test network. This is");
      print("useful for testing sending and receiving between BCCAPI clients. The");
      print("network is reliable as the mining power is constant. Obtain free coins by");
      print("running the SimpleClient on a community shared account using \"asd\" as the");
      print("passphrase and \"asd\" as the salt, and transfer some coins to your own");
      print("BCCAPI client.");
      System.exit(1);
   }

   private static void clientMain(String net) throws Exception {
      // Determine which network to use
      String seedFile;
      URL url;
      Network network;
      if (net.toLowerCase().equals("prodnet")) {
         seedFile = "seed.bin";
         url = new URL("https://prodnet.bccapi.com");
         network = Network.productionNetwork;
      } else if (net.toLowerCase().equals("testnet")) {
         seedFile = "test-seed.bin";
         url = new URL("https://testnet.bccapi.com:444");
         network = Network.testNetwork;
      } else if (net.toLowerCase().equals("closedtestnet")) {
         seedFile = "closed-test-seed.bin";
         url = new URL("https://closedtestnet.bccapi.com:445");
         network = Network.testNetwork;
      } else {
         usage();
         return;
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
            showStatements(account);
         } else if (option.equals("3")) {
            showAdresses(account);
         } else if (option.equals("4")) {
            createNewAddress(account);
         } else if (option.equals("5")) {
            sendCoins(account);
         } else if (option.equals("6")) {
            exportPrivateKeys(account, seed);
         } else if (option.equals("7")) {
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
      while (!task.isFinished()) {
         print(task.getProgress() + "%");
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
      print("2. Get wallet statement");
      print("3. My receiving addresses");
      print("4. Create new receiving address");
      print("5. Send coins");
      print("6. Export private keys");
      print("7. Exit");
      printnln("Enter option:");
   }

   private static void showBalance(Account account) throws APIException, IOException {
      AccountInfo info = account.getInfo();
      String estimatedBalance = CoinUtils.valueString(info.getEstimatedBalance());
      String availableBalance = CoinUtils.valueString(info.getAvailableBalance());
      print("Balance: " + estimatedBalance + "(" + availableBalance + ") BTC");
   }

   private static void showStatements(Account account) throws APIException, IOException {
      AccountStatement statement = account.getRecentTransactionSummary(10);
      if (!statement.getRecords().isEmpty()) {
         Date midnight = getMidnight();
         DateFormat hourFormat = DateFormat.getDateInstance(DateFormat.SHORT);
         DateFormat dayFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
         print("---------------------------------------------------------------------------------------------------");
         print("|###|Confirmations|Date       |Description                                             |    Credit|");
         print("|---|-------------|-----------|--------------------------------------------------------|----------|");
         List<Record> records = statement.getRecords();
         Collections.reverse(records);
         for (AccountStatement.Record record : records) {
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb, Locale.US);

            // Determine which date formatter to use
            Date date = new Date(record.getDate());
            DateFormat dateFormat = date.before(midnight) ? hourFormat : dayFormat;

            // Create description
            String description;
            if (record.getType() == Type.Sent) {
               description = StringUtils.cap("Sent to:          " + record.getAddresses(), 56);
            } else if (record.getType() == Type.Received) {
               description = StringUtils.cap("Received with:    " + record.getAddresses(), 56);
            } else {
               description = StringUtils.cap("Sent to yourself  ", 56);
            }

            String valueString = CoinUtils.valueString(record.getAmount());

            // Format & print record
            f.format("|%1$3s|%2$-13s|%3$-11s|%4$-56s|%5$10s|", record.getIndex(), record.getConfirmations(),
                  dateFormat.format(date), description, valueString);
            print(sb.toString());
         }
         print("---------------------------------------------------------------------------------------------------");
      } else {
         print("There is no record of any transactions for this wallet");
      }
      // Print out current balance and balance available for sending
      String estimatedBalance = CoinUtils.valueString(statement.getInfo().getEstimatedBalance());
      String availableBalance = CoinUtils.valueString(statement.getInfo().getAvailableBalance());
      print("Balance: " + estimatedBalance + "(" + availableBalance + ") BTC");
   }

   private static Date getMidnight() {
      Calendar midnight = Calendar.getInstance();
      midnight.set(midnight.get(Calendar.YEAR), midnight.get(Calendar.MONTH), midnight.get(Calendar.DAY_OF_MONTH), 0,
            0, 0);
      return midnight.getTime();
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
      String address = readLine().trim();
      if (!AddressUtil.validateAddress(address, account.getNetwork())) {
         print("Invalid address");
         return;
      }

      // Amount to send
      printnln("Enter amount to send in BTC: ");
      Long toSend;
      try {
         BigDecimal amount = new BigDecimal(readLine().trim());
         // Convert to satoshis
         amount = amount.multiply(new BigDecimal(100000000));
         toSend = amount.longValue();
      } catch (NumberFormatException e) {
         print("Invalid value entered.");
         return;
      }

      SendCoinForm form;
      try {
         // specifying -1 as the fee tells the server to generate a transaction
         // with a fee that is guaranteed to get processed. Use
         // SendCoinFormSummary on the received SendCoinForm to determine which
         // fee the server has calculated
         form = account.getSendCoinForm(address, toSend, -1);
      } catch (APIException e) {
         print(e.getMessage());
         return;
      }

      long fee = SendCoinFormValidator.calculateFee(form);
      print("Network fee for this transaction: " + CoinUtils.valueString(fee));
      printnln("Hit enter.");
      readLine();

      // Validate that the form matches what we actually requested, and that the
      // server does not cheat on us
      if (!SendCoinFormValidator.validate(form, account.getAddresses(), account.getNetwork(), toSend, fee, address)) {
         print("Warning: Invalid SendCoinForm received from BCCAPI server ");
         return;
      }

      // Print out a summary of the form
      print("==== Summary of coins being sent ====");
      System.out.println(new SendCoinFormSummary(form, account.getAddresses(), account.getNetwork()));
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

   private static void exportPrivateKeys(Account account, byte[] seed) throws APIException, IOException {
      int keys = account.getAddresses().size();
      print("Private keys:");
      DeterministicECKeyExporter exporter = new DeterministicECKeyExporter(seed);
      for (int i = 0; i < keys; i++) {
         String privateKey = exporter.getPrivateKeyExporter(i + 1).getBase58EncodedKey(account.getNetwork());
         print(privateKey);
      }
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
