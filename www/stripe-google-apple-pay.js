var SGAP = {
  someFunction: function (args) {
    return new Promise(function (resolve, reject) {
      cordova.exec(resolve, reject, 'stripe-google-apple-pay', 'test_function', [ 'Context?', ...args ])
    })
  }
}

module.exports = SGAP
