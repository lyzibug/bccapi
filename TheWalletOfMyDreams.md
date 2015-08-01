# The Wallet of my Dreams #

The wallet of my dreams is something I can carry around. Let's assume that it is an Android app on my smartphone.
It contains a number of wallets with different security settings:

  1. It has a wallet for small change. The wallet is always open, i.e I can always see the balance or spend coins by just starting the app.
  1. It has a wallet for larger sums. This wallet is closed when I start the app, i.e. I have to enter a password to see the balance and to spend coins.
  1. It has my vault. This is where I store my stash. This wallet is closed when I start the app. I can open it by entering a long passphrase, and wait say 10 minutes. When the wallet is open I can see the balance and spend coins.

I want this with the following features:
  1. If I loose my device I want to be able to recreate my wallets on a different device.
  1. I do not want to do periodic backups
  1. I do not want to trust a third party for keeping my wallet private keys

All of the above can be achieved using the BCCAPI:
  * Feature 1. All wallets are deterministically generated from passphrases. If you loose your device you can resurrect your wallets from their passphrases plus a salt.
  * Feature 2 You do not need periodic backup beacuse of feature 1. You do however need to remember the passphrases + salt or write them down. Going forward we may even avoid this. (See my wiki on [Managing Long Passphrases](ManagingLongPassphrases.md))
  * Feature 3. The wallet private keys are never leaving the device.

Implementing the wallets using the BCCAPI:
  1. Wallet for small change: You already have it in the SimpleClient example of the BCCAPI. You just need to use an empty PIN and turn it into an Android app.
  1. Wallet for larger sums: You already have it in the SimpleClient example of the BCCAPI. You just need to turn it into an Android app
  1. Vault: You already have it in the SimpleClient example of BCCAPI. You just need to reinitialize the wallet from scratch from a passphrase and salt, and never store the root seed on the device. This way every trace of your private keys are only ever in memory.

Of course each wallet should use be based on different passphrases. I have a wiki on the [Security of Deterministic Wallets](SecurityAndDeterministicWallets.md).

Apart from this the app should be able to scan QR codes containing receiver's address, display my own addresses as QR codes, have a nice UI and so forth.