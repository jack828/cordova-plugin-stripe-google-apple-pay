var SGAP = {
  setKey: function (key) {
    return new Promise(function (resolve, reject) {
      cordova.exec(resolve, reject, 'StripeGoogleApplePay', 'set_key', [ key ])
    })
  },
  isReadyToPay: function () {
    return new Promise(function (resolve, reject) {
      cordova.exec(resolve, reject, 'StripeGoogleApplePay', 'is_ready_to_pay', [])
    })
  },
  requestPayment: function (totalPrice, currency) {
    return new Promise(function (resolve, reject) {
      cordova.exec(resolve, reject, 'StripeGoogleApplePay', 'request_payment', [ totalPrice, currency ])
    })
  }
}

module.exports = SGAP
