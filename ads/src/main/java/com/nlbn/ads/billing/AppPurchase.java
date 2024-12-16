package com.nlbn.ads.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.collect.ImmutableList;
import com.nlbn.ads.callback.BillingListener;
import com.nlbn.ads.callback.PurchaseListioner;
import com.nlbn.ads.util.AppUtil;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppPurchase {
    private static final String LICENSE_KEY = null;
    private static final String MERCHANT_ID = null;
    private static final String TAG = "PurchaseEG";

    public static final String PRODUCT_ID_TEST = "android.test.purchased";
    @SuppressLint("StaticFieldLeak")
    private static AppPurchase instance;

    @SuppressLint("StaticFieldLeak")
    private String price = "1.49$";
    private String oldPrice = "2.99$";
    private String productId;
    private List<String> listSubcriptionId;
    private List<String> listINAPId;
    private PurchaseListioner purchaseListioner;
    private BillingListener billingListener;
    private Boolean isInitBillingFinish = false;
    private BillingClient billingClient;
    private List<ProductDetails> skuListINAPFromStore;
    private List<ProductDetails> skuListSubsFromStore;
    final private Map<String, ProductDetails> skuDetailsINAPMap = new HashMap<>();
    final private Map<String, ProductDetails> skuDetailsSubsMap = new HashMap<>();
    private boolean isAvailable;
    private boolean isListGot;
    private boolean isConsumePurchase = false;

    //tracking purchase adjust
    private String idPurchaseCurrent = "";
    private int typeIap;
    private boolean verified = false;

    private boolean isPurchase = false;//state purchase on app

    public void setPurchaseListioner(PurchaseListioner purchaseListioner) {
        this.purchaseListioner = purchaseListioner;
    }

    /**
     * listener init billing app
     *
     * @param billingListener
     */
    public void setBillingListener(BillingListener billingListener) {
        this.billingListener = billingListener;
        if (isAvailable) {
            billingListener.onInitBillingListener(0);
            isInitBillingFinish = true;
        }
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Boolean getInitBillingFinish() {
        return isInitBillingFinish;
    }

    /**
     * listener init billing app with timeout
     *
     * @param billingListener
     * @param timeout
     */
    public void setBillingListener(BillingListener billingListener, int timeout) {
        this.billingListener = billingListener;
        if (isAvailable) {
            billingListener.onInitBillingListener(0);
            isInitBillingFinish = true;
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isInitBillingFinish) {
                    Log.e(TAG, "setBillingListener: timeout ");
                    isInitBillingFinish = true;
                    billingListener.onInitBillingListener(BillingClient.BillingResponseCode.ERROR);
                }
            }
        }, timeout);
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setConsumePurchase(boolean consumePurchase) {
        isConsumePurchase = consumePurchase;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> list) {
            Log.e(TAG, "onPurchasesUpdated code: " + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {

                    List<String> sku = purchase.getSkus();
                    handlePurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                if (purchaseListioner != null)
                    purchaseListioner.onUserCancelBilling();
                Log.d(TAG, "onPurchasesUpdated:USER_CANCELED ");
            } else {
                Log.d(TAG, "onPurchasesUpdated:... ");
            }
        }
    };

    BillingClientStateListener purchaseClientStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingServiceDisconnected() {
            isAvailable = false;
        }

        @Override
        public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
            Log.d(TAG, "onBillingSetupFinished:  " + billingResult.getResponseCode());

            if (billingListener != null && !isInitBillingFinish) {
                verifyPurchased(true);
            }

            isInitBillingFinish = true;
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                isAvailable = true;

                List<QueryProductDetailsParams.Product> products = new ArrayList<>();
                if (listINAPId.size() > 0) {
                    for (String sku : listINAPId) {
                        QueryProductDetailsParams.Product query = QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(sku)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build();
                        products.add(query);
                    }

                    QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                        .setProductList(products)
                        .build();

                    billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
                            @Override
                            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                                Log.d(TAG, "onSkuSubsDetailsResponse: " + list.size());
                                skuListINAPFromStore = list;
                                isListGot = true;
                                addSkuINAPToMap(list);
                            }
                        }
                    );
                }

                if (listSubcriptionId.size() > 0) {
                    List<QueryProductDetailsParams.Product> productSubs = new ArrayList<>();
                    for (String sku : listSubcriptionId) {
                        QueryProductDetailsParams.Product query = QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(sku)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build();
                        productSubs.add(query);
                    }

                    QueryProductDetailsParams paramsSub = QueryProductDetailsParams.newBuilder()
                        .setProductList(productSubs)
                        .build();

                    billingClient.queryProductDetailsAsync(paramsSub, new ProductDetailsResponseListener() {
                            @Override
                            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                                Log.d(TAG, "onSkuSubsDetailsResponse: " + list.size());
                                skuListSubsFromStore = list;
                                isListGot = true;
                                addSkuSubsToMap(list);
                            }
                        }
                    );
                }

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE || billingResult.getResponseCode() == BillingClient.BillingResponseCode.ERROR) {
                Log.e(TAG, "onBillingSetupFinished:ERROR ");

            }
        }
    };

    public static AppPurchase getInstance() {
        if (instance == null) {
            instance = new AppPurchase();
        }
        return instance;
    }

    private AppPurchase() {

    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void addSubcriptionId(String id) {
        if (listSubcriptionId == null)
            listSubcriptionId = new ArrayList<>();
        listSubcriptionId.add(id);
    }

    public void addProductId(String id) {
        if (listINAPId == null)
            listINAPId = new ArrayList<>();
        listINAPId.add(id);
    }

    public void initBilling(final Application application) {
        listSubcriptionId = new ArrayList<>();
        listINAPId = new ArrayList<>();
        if (AppUtil.BUILD_DEBUG) {
            listINAPId.add(PRODUCT_ID_TEST);
        }
        billingClient = BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build();

        billingClient.startConnection(purchaseClientStateListener);
    }

    public void initBilling(final Application application, List<String> listINAPId, List<String> listSubsId) {
        listSubcriptionId = listSubsId;
        this.listINAPId = listINAPId;

//        if (AppUtil.BUILD_DEBUG) {
//            listINAPId.add(PRODUCT_ID_TEST);
//        }
        billingClient = BillingClient.newBuilder(application)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build();

        billingClient.startConnection(purchaseClientStateListener);
    }


    private void addSkuSubsToMap(List<ProductDetails> skuList) {
        for (ProductDetails skuDetails : skuList) {
            skuDetailsSubsMap.put(skuDetails.getProductId(), skuDetails);
        }
    }

    private void addSkuINAPToMap(List<ProductDetails> skuList) {
        for (ProductDetails skuDetails : skuList) {
            skuDetailsINAPMap.put(skuDetails.getProductId(), skuDetails);
        }
    }

    public boolean isPurchased() {
        return isPurchase;
    }

    public boolean isPurchased(Context context) {
        return isPurchase;
    }

    private boolean verifiedINAP = false;
    private boolean verifiedSUBS = false;

    // kiểm tra trạng thái purchase
    public void verifyPurchased(boolean isCallback) {
        Log.d(TAG, "isPurchased : " + listSubcriptionId.size());
        verified = false;
        if (listINAPId != null) {
            billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    Log.d(TAG, "verifyPurchased INAPP  code:" + billingResult.getResponseCode() + " ===   size:" + list.size());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                        for (Purchase purchase : list) {
                            for (String id : listINAPId) {
                                if (purchase.getProducts().contains(id)) {
                                    Log.d(TAG, "verifyPurchased INAPP: true");
                                    isPurchase = true;
                                    if (!verified) {
                                        if (isCallback)
                                            billingListener.onInitBillingListener(billingResult.getResponseCode());
                                        verified = true;
                                        verifiedINAP = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    if (verifiedSUBS && !verified) {
                        // chưa mua subs và IAP
                        billingListener.onInitBillingListener(billingResult.getResponseCode());
                    }
                    verifiedINAP = true;
                }
            });
        }

        if (listSubcriptionId != null) {
            billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build(), new PurchasesResponseListener() {
                @Override
                public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                    Log.d(TAG, "verifyPurchased SUBS  code:" + billingResult.getResponseCode() + " ===   size:" + list.size());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                        for (Purchase purchase : list) {
                            for (String id : listSubcriptionId) {
                                if (purchase.getProducts().contains(id)) {
                                    Log.d(TAG, "verifyPurchased SUBS: true");
                                    isPurchase = true;
                                    if (!verified) {
                                        if (isCallback)
                                            billingListener.onInitBillingListener(billingResult.getResponseCode());
                                        verified = true;
                                        verifiedINAP = true;
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    if (verifiedINAP && !verified) {
                        // chưa mua subs và IAP
                        billingListener.onInitBillingListener(billingResult.getResponseCode());
                    }
                    verifiedSUBS = true;
                }
            });
        }
    }


//    private String logResultBilling(Purchase.PurchasesResult result) {
//        if (result == null || result.getPurchasesList() == null)
//            return "null";
//        StringBuilder log = new StringBuilder();
//        for (Purchase purchase : result.getPurchasesList()) {
//            for (String s : purchase.getSkus()) {
//                log.append(s).append(",");
//            }
//        }
//        return log.toString();
//    }

    //check  id INAP
//    public boolean isPurchased(Context context, String productId) {
//        Log.d(TAG, "isPurchased: " + productId);
//        Purchase.PurchasesResult resultINAP = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
//        if (resultINAP.getResponseCode() == BillingClient.BillingResponseCode.OK && resultINAP.getPurchasesList() != null) {
//            for (Purchase purchase : resultINAP.getPurchasesList()) {
//                if (purchase.getSkus().contains(productId)) {
//                    return true;
//                }
//            }
//        }
//        Purchase.PurchasesResult resultSubs = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
//        if (resultSubs.getResponseCode() == BillingClient.BillingResponseCode.OK && resultSubs.getPurchasesList() != null) {
//            for (Purchase purchase : resultSubs.getPurchasesList()) {
//                if (purchase.getOrderId().equalsIgnoreCase(productId)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public void purchase(Activity activity) {
        if (productId == null) {
            Log.e(TAG, "Purchase false:productId null");
            Toast.makeText(activity, "Product id must not be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        purchase(activity, productId);
    }


    public String purchase(Activity activity, String productId) {
        if (skuListINAPFromStore == null) {
            if (purchaseListioner != null)
                purchaseListioner.displayErrorMessage("Billing error init");
            return "";
        }
        if (AppUtil.BUILD_DEBUG) {
            // Dùng ID Purchase test khi debug
            productId = PRODUCT_ID_TEST;
        }

        ProductDetails productDetails = skuDetailsINAPMap.get(productId);


        if (productDetails == null) {
            return "Product ID invalid";
        }

        idPurchaseCurrent = productId;
        typeIap = TYPE_IAP.PURCHASE;
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
            ImmutableList.of(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build();
        BillingResult responseCode = billingClient.launchBillingFlow(activity, billingFlowParams);

        switch (responseCode.getResponseCode()) {

            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Billing not supported for type of request");
                return "Billing not supported for type of request";

            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return "";

            case BillingClient.BillingResponseCode.ERROR:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Error completing request");
                return "Error completing request";

            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                return "Error processing request.";

            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                return "Selected item is already owned";

            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                return "Item not available";

            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                return "Play Store service is not connected now";

            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                return "Timeout";

            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Network error.");
                return "Network Connection down";

            case BillingClient.BillingResponseCode.USER_CANCELED:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Request Canceled");
                return "Request Canceled";

            case BillingClient.BillingResponseCode.OK:
                return "Subscribed Successfully";
            //}

        }
        return "";
    }

    public String subscribe(Activity activity, String subsId) {

        if (skuListSubsFromStore == null) {
            if (purchaseListioner != null)
                purchaseListioner.displayErrorMessage("Billing error init");
            return "";
        }

//        if (AppUtil.BUILD_DEBUG) {
//            // sử dụng ID Purchase test
//            purchase(activity, PRODUCT_ID_TEST);
//            return "Billing test";
//        }

        ProductDetails productDetails = skuDetailsSubsMap.get(subsId);

        if (productDetails == null) {
            return "SubsId invalid";
        }

        idPurchaseCurrent = subsId;
        typeIap = TYPE_IAP.SUBSCRIPTION;

        List<ProductDetails.SubscriptionOfferDetails> offerDetails = productDetails.getSubscriptionOfferDetails();
        if (offerDetails == null) {
            return "Product ID invalid";
        }

        String offerToken = offerDetails.get(0).getOfferToken();
        ImmutableList<BillingFlowParams.ProductDetailsParams> productDetailsParamsList =
            ImmutableList.of(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            );

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build();
        BillingResult responseCode = billingClient.launchBillingFlow(activity, billingFlowParams);

        switch (responseCode.getResponseCode()) {

            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Billing not supported for type of request");
                return "Billing not supported for type of request";

            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                return "";

            case BillingClient.BillingResponseCode.ERROR:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Error completing request");
                return "Error completing request";

            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
                return "Error processing request.";

            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                return "Selected item is already owned";

            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                return "Item not available";

            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
                return "Play Store service is not connected now";

            case BillingClient.BillingResponseCode.SERVICE_TIMEOUT:
                return "Timeout";

            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Network error.");
                return "Network Connection down";

            case BillingClient.BillingResponseCode.USER_CANCELED:
                if (purchaseListioner != null)
                    purchaseListioner.displayErrorMessage("Request Canceled");
                return "Request Canceled";

            case BillingClient.BillingResponseCode.OK:
                return "Subscribed Successfully";

            //}

        }
        return "";
    }

    public void consumePurchase() {
        if (productId == null) {
            Log.e(TAG, "Consume Purchase false:productId null ");
            return;
        }
        consumePurchase(productId);
    }

    public void consumePurchase(String productId) {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(), new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (Purchase purchase : list) {
                        if (purchase.getSkus().contains(productId)) {
                            try {
                                ConsumeParams consumeParams =
                                    ConsumeParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();

                                ConsumeResponseListener listener = new ConsumeResponseListener() {
                                    @Override
                                    public void onConsumeResponse(BillingResult billingResult, @NonNull String purchaseToken) {
                                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                            Log.e(TAG, "onConsumeResponse: OK");
                                            verifyPurchased(false);
                                        }
                                    }
                                };

                                billingClient.consumeAsync(consumeParams, listener);
                                return;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            }
        });

    }

    private void handlePurchase(Purchase purchase) {

        //tracking adjust
        double price = getPriceWithoutCurrency(idPurchaseCurrent, typeIap);
        String currentcy = getCurrency(idPurchaseCurrent, typeIap);
        if (purchaseListioner != null)
            isPurchase = true;
        purchaseListioner.onProductPurchased(purchase.getOrderId(), purchase.getOriginalJson());
        if (isConsumePurchase) {
            ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.getPurchaseToken())
                    .build();

            ConsumeResponseListener listener = new ConsumeResponseListener() {
                @Override
                public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                    Log.d(TAG, "onConsumeResponse: " + billingResult.getDebugMessage());
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    }
                }
            };

            billingClient.consumeAsync(consumeParams, listener);
        } else {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                if (!purchase.isAcknowledged()) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                        @Override
                        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                            Log.d(TAG, "onAcknowledgePurchaseResponse: " + billingResult.getDebugMessage());
                        }
                    });
                }
            }
        }
    }

    //    public boolean handleActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        return bp.handleActivityResult(requestCode, resultCode, data);
//    }
//
    public String getPrice() {
        return getPrice(productId);
    }

    public String getPrice(String productId) {
        ProductDetails productDetails = skuDetailsINAPMap.get(productId);
        if (productDetails == null)
            return "";
        if (productDetails.getOneTimePurchaseOfferDetails() == null) {
            return "";
        }
        return String.valueOf(productDetails.getOneTimePurchaseOfferDetails().getPriceAmountMicros() / 1000000);
    }

    public String getFormattedPriceINAP(String productId) {
        ProductDetails productDetails = skuDetailsINAPMap.get(productId);
        if (productDetails == null)
            return "";
        if (productDetails.getOneTimePurchaseOfferDetails() == null) {
            return "";
        }
        return productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
    }

    public String getPriceSub(String productId) {
        ProductDetails productDetails = skuDetailsSubsMap.get(productId);
        if (productDetails == null)
            return "";
        long priceSub = 0L;
        List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
        if (subscriptionOfferDetails != null && subscriptionOfferDetails.size() > 0) {
            for (ProductDetails.SubscriptionOfferDetails offerDetails : subscriptionOfferDetails) {
                if (offerDetails.getOfferId() == null) {
                    priceSub = offerDetails.getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros();
                    break;
                }
            }
        }
        return String.valueOf(priceSub / 1000000);
    }

    public String getFormattedPriceSub(String productId) {
        ProductDetails productDetails = skuDetailsSubsMap.get(productId);
        if (productDetails == null)
            return "";
        String formattedPriceSub = "";
        List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
        if (subscriptionOfferDetails != null && subscriptionOfferDetails.size() > 0) {
            for (ProductDetails.SubscriptionOfferDetails offerDetails : subscriptionOfferDetails) {
                if (offerDetails.getOfferId() == null) {
                    formattedPriceSub = offerDetails.getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
                    break;
                }
            }
        }
        return formattedPriceSub;
    }

    public String getFormattedOfferPriceSub(String productId) {
        ProductDetails productDetails = skuDetailsSubsMap.get(productId);
        if (productDetails == null)
            return "";
        String formattedPriceSub = "";
        List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
        if (subscriptionOfferDetails != null && subscriptionOfferDetails.size() > 0) {
            for (ProductDetails.SubscriptionOfferDetails offerDetails : subscriptionOfferDetails) {
                if (offerDetails.getOfferId() != null) {
                    List<ProductDetails.PricingPhase> pricingPhases = offerDetails.getPricingPhases().getPricingPhaseList();
                    Collections.sort(pricingPhases, (o1, o2) -> (int) (o1.getPriceAmountMicros() - o2.getPriceAmountMicros()));
                    if (pricingPhases.size() > 0) {
                        formattedPriceSub = pricingPhases.get(0).getFormattedPrice();
                    }
                    break;
                }
            }
        }
        return formattedPriceSub;
    }

    public String getOfferPriceSub(String productId) {
        ProductDetails productDetails = skuDetailsSubsMap.get(productId);
        if (productDetails == null)
            return "";
        long formattedPriceSub = 0;
        List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
        if (subscriptionOfferDetails != null && subscriptionOfferDetails.size() > 0) {
            for (ProductDetails.SubscriptionOfferDetails offerDetails : subscriptionOfferDetails) {
                if (offerDetails.getOfferId() != null) {
                    List<ProductDetails.PricingPhase> pricingPhases = offerDetails.getPricingPhases().getPricingPhaseList();
                    Collections.sort(pricingPhases, (o1, o2) -> (int) (o1.getPriceAmountMicros() - o2.getPriceAmountMicros()));
                    if (pricingPhases.size() > 0) {
                        formattedPriceSub = pricingPhases.get(0).getPriceAmountMicros();
                    }
                    break;
                }
            }
        }
        return String.valueOf(formattedPriceSub / 1000000);
    }

    public String getIntroductorySubPrice(String productId) {
//        SkuDetails skuDetails = skuDetailsSubsMap.get(productId);
//        if (skuDetails == null) {
//            return "";
//        }
        return "";
    }

    public String getCurrency(String productId, int typeIAP) {
        ProductDetails productDetails = typeIAP == TYPE_IAP.PURCHASE ? skuDetailsINAPMap.get(productId) : skuDetailsSubsMap.get(productId);
        String currencyCode = "";
        if (typeIAP == TYPE_IAP.PURCHASE) {
            if (productDetails != null && productDetails.getOneTimePurchaseOfferDetails() != null)
                currencyCode = productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
        } else {
            if (productDetails != null && productDetails.getSubscriptionOfferDetails() != null) {
                List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();
                if (subscriptionOfferDetails.size() > 0)
                    currencyCode = subscriptionOfferDetails.get(0).getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode();
            }

        }
        return currencyCode;
    }

    public double getPriceWithoutCurrency(String productId, int typeIAP) {
//        SkuDetails skuDetails = typeIAP == TYPE_IAP.PURCHASE ? skuDetailsINAPMap.get(productId) : skuDetailsSubsMap.get(productId);
//        if (skuDetails == null) {
//            return 0;
//        }
        return 0.0;
    }
//
//    public String getOldPrice() {
//        SkuDetails skuDetails = bp.getPurchaseListingDetails(productId);
//        if (skuDetails == null)
//            return "";
//        return formatCurrency(skuDetails.priceValue / discount, skuDetails.currency);
//    }

    private String formatCurrency(double price, String currency) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setMaximumFractionDigits(0);
        format.setCurrency(Currency.getInstance(currency));
        return format.format(price);
    }

    private double discount = 1;

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDiscount() {
        return discount;
    }


    @IntDef({TYPE_IAP.PURCHASE, TYPE_IAP.SUBSCRIPTION})
    public @interface TYPE_IAP {
        int PURCHASE = 1;
        int SUBSCRIPTION = 2;
    }
}
