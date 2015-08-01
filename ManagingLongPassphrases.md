# Managing Long Passphrases #

The security of BCCAPI's deterministic wallet depends on the strength of the seed from which the keys are derived. BitcoinSpinner uses a randomly generated seed based on a cryptographically secure random generator. SimpleClient uses a passphrase + salt approach and several rounds of key stretching.
For the passphrase + salt approach the user is required to either memorize the passphrase or write it down on a piece of paper. Memorizing may not be a valid option as you simply risk forgetting the passphrase. Writing the passphrase on paper poses different challenges:

  1. It may get lost
  1. Someone else may find it
  1. Entering long passphrases on a mobile device is cumbersome

To address these shortcomings I propose that we use Shamir's Secret Sharing and QR codes.

## Shamir's Secret Sharing ##

Shamir's Secret Sharing is a mechanism where you split a secret into N pieces and decide for a number K which is less than N. If you have K different pieces of the original N it is trivial to calculate the secret. If you have K-1 pieces the secret is completely undetermined, meaning that any possible value is likely. Details here: [Shamir's Secret Sharing](http://en.wikipedia.org/wiki/Shamir's_Secret_Sharing)

Example Choosing N=3 and K=2:
  1. You split your secret into 3 pieces.
  1. You then store those pieces in three different secure locations.
  1. You delete any other trace of the secret.

If you ever want to recreate the secret you can do so by retrieving any 2 of the 3 pieces.
If an attacker gets hold of one of your pieces he will not be able to determine the secret.
If one of the pieces are lost you can still rebuild the secret from the other two.

You may also want to choose N=5 and K=3 or whatever makes you feel safe.

## How can we Apply This ##

Assume that we want to use this mechanism for protecting the seed for the BCCAPI's deterministic wallet. Thisis roughly how it would work:

  1. Start your Android Bitcoin Tool for the first time
  1. A totally random seed gets generated, (not based on passphrase + salt)
  1. Choose your parameters for N and K (say N=3 and K=2).
  1. The tool generates 3 bitmaps with QR codes for the 3 shares.
  1. The tool prints out the shares on a printer you trust.

Now with your three pieces of paper you go ahead and initialize your Android wallet by scanning 2 of the 3 QR codes. Next thing is to store the 3 shares in a proper place.

Now the interesting thing here is that we can actually turn things around. With the SimpleClient you have to enter a passphrase + salt and a password before even starting to use it for the interesting stuff. This is really cumbersome and may scare new users away. With the sharing scheme above the app can initialize itself with a random seed, and the user can eventually export the seed as a number of shares once he has tried it out.


So why are we not doing this today?

The Android Bitcoin Tool does not yet exist. I'll include evertyhing needed to generate/combine shares in the BCCAPI, but someone has to make an Android app that uses it. I am not an Android developer and not good at making UIs anyway.