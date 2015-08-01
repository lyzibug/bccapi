# Trying out the BCCAPI with SimpleClient #

SimpleClient is a text-based bitcoin client implemented on top of the BCCAPI. It is written in Java and open source. You find SimpleClient as an example within the BCCAPI sources (com.bccapi.example.SimpleClient.java).

The SimpleClient uses a deterministic wallet approach, where all keys are generated based on a passphrase and salt both of which are entered the first time the client is launched. A seed file, which is stored in the current working directory, is created from the passphrase and salt, which is encrypted using a PIN. Whenever the SimpleClient is launched it will ask for that PIN, decrypt the seed and generate the keys needed.

SimpleClient is just one of many possible Bitcoin clients that you can build  on top of BCCAPI. However, it is fully functional, secure, leightweight, and a good starting point if you want to make your own client.

## Building Everything Yourself ##
Software needed: [Subversion](http://subversion.apache.org),  [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html), [Ant](http://ant.apache.org/bindownload.cgi).
  1. Get the code: `svn checkout http://bccapi.googlecode.com/svn/trunk/ bccapi`
  1. Compile the jar: `cd bccapi && ant clean bccapi-jar`
  1. The jar file is located here: bccapi/build/jar/bccapi.jar

## Using a Pre-Built JAR ##
Software needed: [JRE](http://www.java.com/en/download/) or [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  1. Download [bccapi.jar](http://code.google.com/p/bccapi/downloads/list)

## Testing it on the Bitcoin Test Network ##
This is using test bitcoins who have no real value. For testnet SimpleClient needs to be able to communicate to the internet using destination port 444 (HTTPS port +1).
  1. Run it from a command line like this: `java -jar bccapi.jar testnet`
  1. Follow the instructions
  1. Use option 3 to see your bitcoin addresses.
  1. Go to the [Bitcoin Testnet Fauchet](https://testnet.freebitcoins.appspot.com/) and transfer some coins to one of your addresses.
  1. Follow the transaction on the [Testnet Block Explorer](http://blockexplorer.com/testnet). Note that the test net may have long periods where no blocks are generated. If this is a problem use the closed testnet described below.
  1. Once it ticks in check your balance using SimlpeClient

## Testing it on the Closed BCCAPI Bitcoin Test Network ##
Since the number of miners on the official Bitcoin testnet varies a lot, there may be long periods where no blocks are generated. This may obviously be a problem when testing your Bitcoin client.
For alleviating this we provide a small closed test network with a constant amount of mining power. Bitcoins on this network cannot be used on the official Bitcoin testnet.
To access the closed testnet SimpleClient needs to be able to communicate to the internet using destination port 445 (HTTPS port +2).
  1. Run it from a command line like this: `java -jar bccapi.jar closedtestnet`
  1. Follow the instructions
  1. Use option 3 to see your Bitcoin addresses.

To send some test coins to your Bitcoin address launch the !Simpleclient in another folder and use "asd" as your passphrase and seed. This will give you access to a community shared wallet with test Bitcoins. Please don't empty the shared wallet as others may want some too. If it is empty send me an email (jan.moller@gmail.com) and I'll fill it up.

## Testing it on the Bitcoin Production Network ##
This is using production bitcoins with real value.
  1. Run it from a command line like this: `java -jar bccapi.jar prodnet`
  1. Follow the instructions
  1. Use option 3 to see your bitcoin addresses.
  1. Transfer some bitcoins to one of the listed addresses using the official bitcoin client. Yes, you need to have some real Bitcoins to do this.
  1. Follow the transaction on the [Block Explorer](http://blockexplorer.com).
  1. Once it ticks in check your balance using SimlpeClient