package org.mbach.homeautomation.discovery;

/**
 * OnAsyncNetworkTaskCompleted.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
interface OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> {
    void onNetworkScanCompleted(AsyncNetworkRequest asyncNetworkRequest);
}
