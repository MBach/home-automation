package org.mbach.homeautomation.discovery;

/**
 * OnAsyncNetworkTaskCompleted.
 *
 * @author Matthieu BACHELIER
 * @since 2017-08
 */
public interface OnAsyncNetworkTaskCompleted<AsyncNetworkRequest> {
    void onCallCompleted(AsyncNetworkRequest asyncNetworkRequest);
}
