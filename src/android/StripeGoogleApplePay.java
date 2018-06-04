package stripegoogleapplepay;

import org.apache.cordova.*;
import com.stripe.android.*;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;

public class StripeGoogleApplePay extends CordovaPlugin {
  private static final String TEST_ACTION = "test_action";

  private PaymentsClient paymentsClient;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Context context = this.cordova.getActivity().getApplicationContext();
    this.paymentsClient = Wallet.getPaymentsClient(
        context,
        new Wallet.WalletOptions.Builder()
          .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
          .build()
        );
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
