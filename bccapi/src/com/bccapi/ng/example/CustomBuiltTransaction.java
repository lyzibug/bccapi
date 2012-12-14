package com.bccapi.ng.example;

import java.net.MalformedURLException;
import java.net.URL;

import com.bccapi.bitlib.model.Address;
import com.bccapi.bitlib.model.NetworkParameters;
import com.bccapi.bitlib.model.OutPoint;
import com.bccapi.bitlib.model.Script;
import com.bccapi.bitlib.model.Script.ScriptParsingException;
import com.bccapi.bitlib.model.ScriptInput;
import com.bccapi.bitlib.model.ScriptOutput;
import com.bccapi.bitlib.model.ScriptOutputStandard;
import com.bccapi.bitlib.model.Transaction;
import com.bccapi.bitlib.model.TransactionInput;
import com.bccapi.bitlib.model.TransactionOutput;
import com.bccapi.bitlib.util.HexUtils;
import com.bccapi.bitlib.util.Sha256Hash;
import com.bccapi.ng.api.ApiException;
import com.bccapi.ng.api.BitcoinClientApi;
import com.bccapi.ng.api.BroadcastTransactionRequest;
import com.bccapi.ng.api.BroadcastTransactionResponse;
import com.bccapi.ng.impl.BitcoinClientApiImpl;

public class CustomBuiltTransaction {

   /**
    * This example was made as a response to a bitcointalk.org post:
    * (https://bitcointalk.org/index.php?topic=130392.0)
    * 
    * Someone made a transaction that could be claimed without a signature if
    * you just crafted the right transaction. The transaction that eventually
    * claimed it was not made using bitlib/bccapi, but this example illustrates
    * how it could be done using bitlib/bccapi.
    */
   public static void main(String[] args) throws ScriptParsingException, MalformedURLException, ApiException {
      // Build transaction input
      Sha256Hash txHash = new Sha256Hash(
            HexUtils.toBytes("cdb553214a51ef8d4393b96a185ebbbc2c84b7014e9497fea8aec1ff990dae35"));
      OutPoint outPoint = new OutPoint(txHash, 0);
      ScriptInput si = ScriptInput.fromScriptBytes(new byte[] { 1, Script.OP_FALSE, Script.OP_DROP });
      TransactionInput ti = new TransactionInput(outPoint, si);

      // Build transaction output
      Address receiver = Address.fromString("1LqTjBzMRdXQqXpRQo1zDYRiUobAVS55Lo", NetworkParameters.productionNetwork);
      ScriptOutput so = new ScriptOutputStandard(receiver.getTypeSpecificBytes());
      TransactionOutput to = new TransactionOutput(99950000, so);

      // Build transaction
      Transaction tx = new Transaction(1, new TransactionInput[] { ti }, new TransactionOutput[] { to }, 0);
      System.out.println("Our transaction hash: " + tx.getHash().toString());

      // Broadcast transaction (here we depend on the BCCAPI server, you can use
      // whatever other means you want for broadcasting the transaction)
      URL url = new URL("http://bqs1.bccapi.com:80");
      BitcoinClientApi api = new BitcoinClientApiImpl(url, NetworkParameters.productionNetwork);
      BroadcastTransactionResponse response = api.broadcastTransaction(new BroadcastTransactionRequest(tx));
      System.out.println("Broadcasted transaction with hash: " + response.hash.toString());
   }
}
