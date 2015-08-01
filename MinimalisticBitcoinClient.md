# Minimalistic Bitcoin Client for Mass Adoption #

For bitcoin to succeed we need a bitcoin client that appeals not to geeks, but to the broader population. The most important feature is simply put "Simplicity". We cannot expect average users to understand archane concepts of double spends, SHA-256 and elliptic curve cyptography.

This is key:
  * Ease of installation
  * Ease of use
  * Security

Feature overview for the minimalistic client:
  * View Balance
  * View QR code of my address
  * Make payment by scanning QR code

Limitations:
  * No passphrase or salt. The seed for the keys is generated randomly
  * No pin/password entered for encrypting deterministic wallet seed.
  * No in-app backup functionality. You can do a manual backup by making a copy of the seed file.
  * Just one wallet key
  * No listing of recent transactions

The following is a description of minimal Android bitcoin client based on the BCCAPI.


## Initialization Page ##

First time the client is started it opens the Initialization page, where the random seed for the deterministic wallet is generated. To get a really secure and random seed we ask the user to shake the device, and capture the input from the phone's accelerometer. This shouldn't take more than say 10 seconds. While sampling the app should make sure that the phone is actually moved around, so there should be a minimum threshold that the accelerometer should 'move' in order to make progress. The important thing here is that we get enough entropy to capture what resembles at least 256 bits of pure randomness. The sampling process should get a lot more than 256 bits of input, and pass it through SHA-256 to finally obtain a 256 bit seed.

While this process is going on a progress indicator should display the progress of capturing input.

Concept Art:
```
###################################
#                                 #
# Initializing Bitcoin Wallet...  #
#                                 #
#       Shake your device         #
#                                 #
#    #######_____37%_________     #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
#                                 #
###################################
```
Once initialization is complete the Balance page is shown.

## Balance Page ##
Apart from first time initialization, the Balance page is the first page shown when the client starts. This page displays:
  * Your current balance, and maybe lists the amount of BTC that is on its way, but not yet confirmed.
  * Maybe it displays your bitcoin address.
  * Has a "Receive Payment" button which opens the Display QR Code page
  * Has a "Make Payment" button which opens the Make Payment screen.

When the Balance page is shown it will display the balance from last time it was open, and show an animating spindle while fetching the current balance from the server.
While the Balance page is shown it will periodically poll the server to update its balance. An Refresh button could be an alternative to this.

## Display QR Code Page ##
Displays the QR code for the single wallet key address in full screen. Tap to go back to Main page.

## Make Payment Page ##
Scans for a QR code. User enters amount to send. Fee is automatically calculated and added. The user clicks the Send button, and a "Coins Sent" message appears if everything went well. Tap to go back to Balance page.