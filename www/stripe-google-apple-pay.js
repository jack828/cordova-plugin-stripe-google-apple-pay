var SGAP = {
  isReadyToPay: function () {
    return new Promise(function (resolve, reject) {
      cordova.exec(resolve, reject, 'StripeGoogleApplePay', 'is_ready_to_pay', [])
    })
  }
}

module.exports = SGAP
