package stripegoogleapplepay;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.identity.intents.model.UserAddress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardInfo;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.IsReadyToPayRequest;

import com.stripe.android.model.Token;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;

public class StripeGoogleApplePay extends CordovaPlugin {
  private static final String SET_KEY = "set_key";
  private static final String IS_READY_TO_PAY = "is_ready_to_pay";
  private static final String REQUEST_PAYMENT = "request_payment";

  private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 42;

  private PaymentsClient paymentsClient = null;
  private CallbackContext callback;
  private int environment;
  private String stripePublishableKey;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  @Override
  public boolean execute(final String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    this.callback = callbackContext;

    if (action.equals(SET_KEY)) {
      this.setKey(data.getString(0));
    }

    // These actions require the key to be already set
    if (this.isInitialised()) {
      this.callback.error("SGAP not initialised. Please run sgap.setKey(STRIPE_PUBLISHABLE).");
    }

    if (action.equals(IS_READY_TO_PAY)) {
      this.isReadyToPay();
    } else if (action.equals(REQUEST_PAYMENT)) {
      this.requestPayment(data.getString(0), data.getString(1));
    } else {
      return false;
    }
    return true;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case LOAD_PAYMENT_DATA_REQUEST_CODE:
        switch (resultCode) {
          case Activity.RESULT_OK:
            PaymentData paymentData = PaymentData.getFromIntent(data);
            // You can get some data on the user's card, such as the brand and last 4 digits
            CardInfo info = paymentData.getCardInfo();
            // You can also pull the user address from the PaymentData object.
            UserAddress address = paymentData.getShippingAddress();
            // This is the raw JSON string version of your Stripe token.
            String rawToken = paymentData.getPaymentMethodToken().getToken();

            // Now that you have a Stripe token object, charge that by using the id
            Token stripeToken = Token.fromString(rawToken);
            if (stripeToken != null) {
              // This chargeToken function is a call to your own server, which should then connect
              // to Stripe's API to finish the charge.
              //chargeToken(stripeToken.getId());
              this.callback.success(stripeToken.getId());
            } else {
              this.callback.error("An error occurred");
            }
            break;
          case Activity.RESULT_CANCELED:
            this.callback.error("Payment cancelled");
            break;
          case AutoResolveHelper.RESULT_ERROR:
            Status status = AutoResolveHelper.getStatusFromIntent(data);
            // Log the status for debugging
            // Generally there is no need to show an error to
            // the user as the Google Payment API will do that
            break;
          default:
            // Do nothing.
        }
        break; // Breaks the case LOAD_PAYMENT_DATA_REQUEST_CODE
      // Handle any other startActivityForResult calls you may have made.
      default:
        // Do nothing.
    }
  }

  private boolean isInitialised() {
    return this.paymentsClient == null;
  }

  private void setKey(String key) {
    if (key.contains("pk_test")) {
      this.environment = WalletConstants.ENVIRONMENT_TEST;
    } else if (key.contains("pk_live")) {
      this.environment = WalletConstants.ENVIRONMENT_PRODUCTION;
    } else {
      this.callback.error("Invalid key");
      return;
    }
    this.stripePublishableKey = key;
    this.paymentsClient = Wallet.getPaymentsClient(
        this.cordova.getActivity().getApplicationContext(),
        new Wallet.WalletOptions.Builder()
            .setEnvironment(this.environment)
            .build()
    );
    this.callback.success();
  }

  private void isReadyToPay() {
    IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
      .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
      .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
      .build();

    Task<Boolean> task = this.paymentsClient.isReadyToPay(request);
    final CallbackContext callbackContext = this.callback;
    task.addOnCompleteListener(
      new OnCompleteListener<Boolean>() {
        public void onComplete(Task<Boolean> task) {
          try {
            boolean result = task.getResult(ApiException.class);
            if (!result) callbackContext.error("Not supported");
            else callbackContext.success();

          } catch (ApiException exception) {
            callbackContext.error(exception.getMessage());
          }
        }
      });
  }

  private void requestPayment (String totalPrice, String currency) {
    PaymentDataRequest request = this.createPaymentDataRequest(totalPrice, currency);
    Activity activity = this.cordova.getActivity();
    if (request != null) {
      cordova.setActivityResultCallback(this);
      AutoResolveHelper.resolveTask(
          this.paymentsClient.loadPaymentData(request),
          activity,
          LOAD_PAYMENT_DATA_REQUEST_CODE);
    }
  }

  private PaymentMethodTokenizationParameters createTokenisationParameters() {
    return PaymentMethodTokenizationParameters.newBuilder()
        .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
        .addParameter("gateway", "stripe")
        .addParameter("stripe:publishableKey", this.stripePublishableKey)
        .addParameter("stripe:version", "5.1.0")
        .build();
  }

  private PaymentDataRequest createPaymentDataRequest(String totalPrice, String currency) {
    PaymentDataRequest.Builder request =
        PaymentDataRequest.newBuilder()
            .setTransactionInfo(
                TransactionInfo.newBuilder()
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setTotalPrice(totalPrice)
                    .setCurrencyCode(currency)
                    .build())
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(Arrays.asList(
                        WalletConstants.CARD_NETWORK_AMEX,
                        WalletConstants.CARD_NETWORK_DISCOVER,
                        WalletConstants.CARD_NETWORK_VISA,
                        WalletConstants.CARD_NETWORK_MASTERCARD))
                    .build());

    request.setPaymentMethodTokenizationParameters(this.createTokenisationParameters());
    return request.build();
  }
}
