/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\Projects\\Oleg\\AndroidProjects\\YourTube\\src\\com\\nokia\\payment\\iap\\aidl\\INokiaIAPService.aidl
 */
package com.nokia.payment.iap.aidl;
/**
 * All calls will give a response code with the following possible values<br/><br/>
 * RESULT_OK = 0 - success<br/>
 * RESULT_USER_CANCELED = 1 - user pressed back or canceled a dialog<br/>
 * RESULT_BILLING_UNAVAILABLE = 3 - this billing API version is not supported for the type requested or billing is otherwise impossible<br/>
 * RESULT_ITEM_UNAVAILABLE = 4 - requested ProductID is not available for purchase<br/>
 * RESULT_DEVELOPER_ERROR = 5 - invalid arguments provided to the API<br/>
 * RESULT_ERROR = 6 - Fatal error during the API action<br/>
 * RESULT_ITEM_ALREADY_OWNED = 7 - Failure to purchase since item is already owned<br/>
 * RESULT_ITEM_NOT_OWNED = 8 - Failure to consume since item is not owned<br/>
 * RESULT_NO_SIM = 9 - Billing is not available because there is no SIM card inserted<br/>
 *<br/>
 * Only supported itemtype, other values result in RESULT_DEVELOPER_ERROR<br/>
 * ITEM_TYPE_INAPP = "inapp";<br/>
 */
public interface INokiaIAPService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.nokia.payment.iap.aidl.INokiaIAPService
{
private static final java.lang.String DESCRIPTOR = "com.nokia.payment.iap.aidl.INokiaIAPService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.nokia.payment.iap.aidl.INokiaIAPService interface,
 * generating a proxy if needed.
 */
public static com.nokia.payment.iap.aidl.INokiaIAPService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.nokia.payment.iap.aidl.INokiaIAPService))) {
return ((com.nokia.payment.iap.aidl.INokiaIAPService)iin);
}
return new com.nokia.payment.iap.aidl.INokiaIAPService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_isBillingSupported:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
int _result = this.isBillingSupported(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getProductDetails:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
android.os.Bundle _arg3;
if ((0!=data.readInt())) {
_arg3 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg3 = null;
}
android.os.Bundle _result = this.getProductDetails(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getBuyIntent:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
android.os.Bundle _result = this.getBuyIntent(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getPurchases:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
android.os.Bundle _arg3;
if ((0!=data.readInt())) {
_arg3 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg3 = null;
}
java.lang.String _arg4;
_arg4 = data.readString();
android.os.Bundle _result = this.getPurchases(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_consumePurchase:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
int _result = this.consumePurchase(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setProductMappings:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
android.os.Bundle _arg2;
if ((0!=data.readInt())) {
_arg2 = android.os.Bundle.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
int _result = this.setProductMappings(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.nokia.payment.iap.aidl.INokiaIAPService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Checks support for the requested billing API version, package and in-app type.
     * @param apiVersion the billing version which the app is using
     * @param packageName the package name of the calling app
       @param type must always be "inapp"     
     * @return RESULT_OK(0) on success, corresponding result code on failures
     */
@Override public int isBillingSupported(int apiVersion, java.lang.String packageName, java.lang.String type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(apiVersion);
_data.writeString(packageName);
_data.writeString(type);
mRemote.transact(Stub.TRANSACTION_isBillingSupported, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Provides details of a list of products<br/>
     * Given a list of Productids of a valid type in the productBundle, this returns a bundle
     * with a list JSON strings containing the productId, price, title and description.
     * This API can be called with a maximum of 20 Productids. 
     * @param apiVersion billing API version that the Third-party is using
     * @param packageName the package name of the calling app
     * @param type must always be "inapp"     
     * @param productBundle bundle containing a StringArrayList of Productids with key "ITEM_ID_LIST", when setProductMappings has been called this parameter can be null
     * @return Bundle containing the following key-value pairs<br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "DETAILS_LIST" with a StringArrayList containing purchase information
     *              in JSON format similar to:<br/>
     <br/>
     *              '{ "productId" : "1264321", "isvalid", true, "title" : "Product title",<br/> 
     *				    "shortdescription" : "Short description of the product", <br/>
     *                  "description" : "Longer description of the product", "priceValue" : "3.00",<br/>
     *                  "price" : "$3.00", "currency", "USD",<br/>
     *                  "purchaseToken" : "ZXlKMlpYSWlPaUl4TGpBaUxDSjBlRzVKWkNJNklrNVFRVmxmVkVWVFZGOVVXRTVmTVRFeE1TSXNJbkJ5YjJSSlpDSTZJakV3TWpNMk1qUWlmUT09",<br/>
     *                  "taxesincluded": true, "restorable" : true, "type" : "inapp" }'<br/><br/>
     *  or if requested productId is not valid then<br/>
     *              ''{ "productId" : "invalidproductid", "isvalid", false }'<br/>
     */
@Override public android.os.Bundle getProductDetails(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle productBundle) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(apiVersion);
_data.writeString(packageName);
_data.writeString(type);
if ((productBundle!=null)) {
_data.writeInt(1);
productBundle.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_getProductDetails, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns a pending intent to launch the purchase flow for an in-app item by providing a ProductID,
     * the type, a unique purchase token and an optional developer payload.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param productId the ProductID of the in-app item as published in the developer console or as mapped through setProductMappings
     * @param type must always be "inapp"
     * @param developerPayload optional argument to be sent back with the purchase information
     * @return Bundle containing the following key-value pairs<br/><br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "BUY_INTENT" - PendingIntent to start the purchase flow
     *
     * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
     * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.<br/><br/>
     * If the purchase is successful, the result data will contain the following key-value pairs<br/><br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "INAPP_PURCHASE_DATA" - String in JSON format similar to<br/><br/>
     *              '{"orderId":"X393XDAFFDAFAD",<br/>
     *                "packageName":"com.your.app",<br/>
     *                "productId":"1264321",<br/>
     *                "purchaseToken" : "ZXlKMlpYSWlPaUl4TGpBaUxDSjBlRzVKWkNJNklrNVFRVmxmVkVWVFZGOVVXRTVmTVRFeE1TSXNJbkJ5YjJSSlpDSTZJakV3TWpNMk1qUWlmUT09",<br/>
     *                "developerPayload":"" }'<br/><br/>
     *         "INAPP_DATA_SIGNATURE" - currently empty string<br/>
     */
@Override public android.os.Bundle getBuyIntent(int apiVersion, java.lang.String packageName, java.lang.String productID, java.lang.String type, java.lang.String developerPayload) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(apiVersion);
_data.writeString(packageName);
_data.writeString(productID);
_data.writeString(type);
_data.writeString(developerPayload);
mRemote.transact(Stub.TRANSACTION_getBuyIntent, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Returns the current products associated with current imei
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param type must always be "inapp"
     * @param productBundle bundle containing a StringArrayList of Productids with key "ITEM_ID_LIST", when setProductMappings has been called this parameter can be null
     * @param continuationToken - currently ignored    
     * @return Bundle containing the following key-value pairs<br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "INAPP_PURCHASE_ITEM_LIST" - StringArrayList containing the list of owned products<br/>
     *         "INAPP_PURCHASE_DATA_LIST" - StringArrayList containing for each owned product json string
     *             similar to following (please note that delveloperPayload field is currently empty):<br/><br/>
     *                { "productId":"1264321",<br/>
     *                "purchaseToken" : "ZXlKMlpYSWlPaUl4TGpBaUxDSjBlRzVKWkNJNklrNVFRVmxmVkVWVFZGOVVXRTVmTVRFeE1TSXNJbkJ5YjJSSlpDSTZJakV3TWpNMk1qUWlmUT09",<br/>
     *                "developerPayload":"" }<br/>
     */
@Override public android.os.Bundle getPurchases(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle productBundle, java.lang.String continuationToken) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.os.Bundle _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(apiVersion);
_data.writeString(packageName);
_data.writeString(type);
if ((productBundle!=null)) {
_data.writeInt(1);
productBundle.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(continuationToken);
mRemote.transact(Stub.TRANSACTION_getPurchases, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.os.Bundle.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Consume the last purchase of the given product. This will result in this item being removed
     * from all subsequent responses to getPurchases() and allow re-purchase of this item.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param productId productId of purchase to be consumed, this argument can be empty or null
     * @param purchaseToken token in the purchase information JSON that identifies the purchase
     *        to be consumed
     * @return 0 if consumption succeeded. Appropriate error values for failures.
     */
@Override public int consumePurchase(int apiVersion, java.lang.String packageName, java.lang.String productId, java.lang.String purchaseToken) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(apiVersion);
_data.writeString(packageName);
_data.writeString(productId);
_data.writeString(purchaseToken);
mRemote.transact(Stub.TRANSACTION_consumePurchase, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
     * Set mapping between nokia productid-s and application internal productid-s
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param mappingsBundle - bundle containing mapping as key, value pairs where key is Nokia productid and value is calling applications internal id 
    */
@Override public int setProductMappings(int apiVersion, java.lang.String packageName, android.os.Bundle mappingsBundle) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(apiVersion);
_data.writeString(packageName);
if ((mappingsBundle!=null)) {
_data.writeInt(1);
mappingsBundle.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setProductMappings, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_isBillingSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getProductDetails = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getBuyIntent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getPurchases = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_consumePurchase = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_setProductMappings = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
}
/**
     * Checks support for the requested billing API version, package and in-app type.
     * @param apiVersion the billing version which the app is using
     * @param packageName the package name of the calling app
       @param type must always be "inapp"     
     * @return RESULT_OK(0) on success, corresponding result code on failures
     */
public int isBillingSupported(int apiVersion, java.lang.String packageName, java.lang.String type) throws android.os.RemoteException;
/**
     * Provides details of a list of products<br/>
     * Given a list of Productids of a valid type in the productBundle, this returns a bundle
     * with a list JSON strings containing the productId, price, title and description.
     * This API can be called with a maximum of 20 Productids. 
     * @param apiVersion billing API version that the Third-party is using
     * @param packageName the package name of the calling app
     * @param type must always be "inapp"     
     * @param productBundle bundle containing a StringArrayList of Productids with key "ITEM_ID_LIST", when setProductMappings has been called this parameter can be null
     * @return Bundle containing the following key-value pairs<br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "DETAILS_LIST" with a StringArrayList containing purchase information
     *              in JSON format similar to:<br/>
     <br/>
     *              '{ "productId" : "1264321", "isvalid", true, "title" : "Product title",<br/> 
     *				    "shortdescription" : "Short description of the product", <br/>
     *                  "description" : "Longer description of the product", "priceValue" : "3.00",<br/>
     *                  "price" : "$3.00", "currency", "USD",<br/>
     *                  "purchaseToken" : "ZXlKMlpYSWlPaUl4TGpBaUxDSjBlRzVKWkNJNklrNVFRVmxmVkVWVFZGOVVXRTVmTVRFeE1TSXNJbkJ5YjJSSlpDSTZJakV3TWpNMk1qUWlmUT09",<br/>
     *                  "taxesincluded": true, "restorable" : true, "type" : "inapp" }'<br/><br/>
     *  or if requested productId is not valid then<br/>
     *              ''{ "productId" : "invalidproductid", "isvalid", false }'<br/>
     */
public android.os.Bundle getProductDetails(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle productBundle) throws android.os.RemoteException;
/**
     * Returns a pending intent to launch the purchase flow for an in-app item by providing a ProductID,
     * the type, a unique purchase token and an optional developer payload.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param productId the ProductID of the in-app item as published in the developer console or as mapped through setProductMappings
     * @param type must always be "inapp"
     * @param developerPayload optional argument to be sent back with the purchase information
     * @return Bundle containing the following key-value pairs<br/><br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "BUY_INTENT" - PendingIntent to start the purchase flow
     *
     * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
     * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.<br/><br/>
     * If the purchase is successful, the result data will contain the following key-value pairs<br/><br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "INAPP_PURCHASE_DATA" - String in JSON format similar to<br/><br/>
     *              '{"orderId":"X393XDAFFDAFAD",<br/>
     *                "packageName":"com.your.app",<br/>
     *                "productId":"1264321",<br/>
     *                "purchaseToken" : "ZXlKMlpYSWlPaUl4TGpBaUxDSjBlRzVKWkNJNklrNVFRVmxmVkVWVFZGOVVXRTVmTVRFeE1TSXNJbkJ5YjJSSlpDSTZJakV3TWpNMk1qUWlmUT09",<br/>
     *                "developerPayload":"" }'<br/><br/>
     *         "INAPP_DATA_SIGNATURE" - currently empty string<br/>
     */
public android.os.Bundle getBuyIntent(int apiVersion, java.lang.String packageName, java.lang.String productID, java.lang.String type, java.lang.String developerPayload) throws android.os.RemoteException;
/**
     * Returns the current products associated with current imei
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param type must always be "inapp"
     * @param productBundle bundle containing a StringArrayList of Productids with key "ITEM_ID_LIST", when setProductMappings has been called this parameter can be null
     * @param continuationToken - currently ignored    
     * @return Bundle containing the following key-value pairs<br/>
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.<br/>
     *         "INAPP_PURCHASE_ITEM_LIST" - StringArrayList containing the list of owned products<br/>
     *         "INAPP_PURCHASE_DATA_LIST" - StringArrayList containing for each owned product json string
     *             similar to following (please note that delveloperPayload field is currently empty):<br/><br/>
     *                { "productId":"1264321",<br/>
     *                "purchaseToken" : "ZXlKMlpYSWlPaUl4TGpBaUxDSjBlRzVKWkNJNklrNVFRVmxmVkVWVFZGOVVXRTVmTVRFeE1TSXNJbkJ5YjJSSlpDSTZJakV3TWpNMk1qUWlmUT09",<br/>
     *                "developerPayload":"" }<br/>
     */
public android.os.Bundle getPurchases(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle productBundle, java.lang.String continuationToken) throws android.os.RemoteException;
/**
     * Consume the last purchase of the given product. This will result in this item being removed
     * from all subsequent responses to getPurchases() and allow re-purchase of this item.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param productId productId of purchase to be consumed, this argument can be empty or null
     * @param purchaseToken token in the purchase information JSON that identifies the purchase
     *        to be consumed
     * @return 0 if consumption succeeded. Appropriate error values for failures.
     */
public int consumePurchase(int apiVersion, java.lang.String packageName, java.lang.String productId, java.lang.String purchaseToken) throws android.os.RemoteException;
/**
     * Set mapping between nokia productid-s and application internal productid-s
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param mappingsBundle - bundle containing mapping as key, value pairs where key is Nokia productid and value is calling applications internal id 
    */
public int setProductMappings(int apiVersion, java.lang.String packageName, android.os.Bundle mappingsBundle) throws android.os.RemoteException;
}
