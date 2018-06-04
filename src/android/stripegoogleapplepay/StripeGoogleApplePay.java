
package stripegoogleapplepay;

import android.content.Context;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.IsReadyToPayRequest;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;

public class StripeGoogleApplePay extends CordovaPlugin {
  private static final String IS_READY_TO_PAY = "is_ready_to_pay";

  private PaymentsClient paymentsClient;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Context context = this.cordova.getActivity().getApplicationContext();
    this.paymentsClient = Wallet.getPaymentsClient(
        context,
        new Wallet.WalletOptions.Builder() // TODO: production environment check
          .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
          .build()
        );
  }

  @Override
  public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    if (action.equals(IS_READY_TO_PAY)) {
      this.isReadyToPay(callbackContext);
    } else {
      return false;
    }
    return true;
  }

  private void isReadyToPay(CallbackContext callbackContext) {
    IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
      .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
      .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
      .build();
    Task<Boolean> task = paymentsClient.isReadyToPay(request);
    task.addOnCompleteListener(
      new OnCompleteListener<Boolean>() {
        public void onComplete(Task<Boolean> task) {
          try {
            boolean result = task.getResult(ApiException.class);
            if (result) {
              callbackContext.success();
            } else {
              callbackContext.error("Not supported");
            }
          } catch (ApiException exception) {
            callbackContext.error(exception.getMessage());
          }
        }
      });
  }
}

