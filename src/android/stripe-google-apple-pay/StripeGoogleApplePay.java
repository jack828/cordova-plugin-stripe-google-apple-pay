package StripeGoogleApplePay;

import org.apache.cordova.*;
import com.stripe.android.*;

public class StripeGoogleApplePay extends CordovaPlugin {
  private static final String TEST_ACTION = "test_action";

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    this.paymentsClient =
      Wallet.getPaymentsClient(this, new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST).build());
  }

  @Override
  public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    if (action.equals(TEST_ACTION)) {
      this.runTestAction(data, callbackContext);
    } else {
      return false;
    }
    return true;
  }


  private void runTestAction (JSONArray data, CallbackContext callbackContext) {
    callbackContext.success(data);
  }
}
