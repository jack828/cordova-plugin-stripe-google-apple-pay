# cordova-plugin-stripe-google-apple-pay
Cordova plugin for Google &amp; Apple pay integration

## Notes

This plugin only supports Android and iOS.

This plugin will add these dependencies to your build.gradle file:

```
com.stripe:stripe-android:6.1.2
com.google.android.gms:play-services-wallet:15.0.1
com.android.support:support-v4:27.0.2
com.android.support:appcompat-v7:27.1.1
```

## Installation

```
cordova plugin add cordova-plugin-stripe-google-apple-pay
```

## Usage

This plugin puts the functions into `window.sgap`.
All functions return a promise.

```javascript
sgap.setKey('pk_test_d34db33f')
```
 - Used to set the Stripe publishable key
 - Required to request payments
 - If test key is provided, then payments are completed in `ENVIRONMENT_TEST` mode, and `ENVIRONMENT_PRODUCTION` mode if the live key is given
 - If you attempt any of the below functions without calling this first, they will throw an error

```javascript
sgap.isReadyToPay()
```
 - Used to test if the appropriate payment method is available on the current device.
 - Resolves if appropriate payment method is available
 - Rejects if not, or if it encounters an error

```javascript
sgap.requestPayment(totalPrice, currency)
```
  - Initiates the payment journey for the user to complete.
  - `totalPrice` must be a string representation of the total price - e.g. for £10.78, it would be `10.78`
  - `currency` must be a valid ISO 4217 currency code for the transaction
  - Resolves when the journey is complete, with the stripe token
  - Rejects if an error occurs

## Contributing

PRs welcome!

## License

MIT
