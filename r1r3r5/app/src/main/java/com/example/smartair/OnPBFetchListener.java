package com.example.smartair;

/**
 * Interface for callback when fetching Parent Configured PB (Peak Expiratory Flow Baseline).
 */
public interface OnPBFetchListener {
    /**
     * Called when PB value is successfully fetched.
     * @param pbValue The baseline PEF value configured by the parent.
     */
    void onSuccess(float pbValue);

    /**
     * Called when fetching PB value fails.
     * @param errorMsg The error message describing why the fetch failed.
     */
    void onFailure(String errorMsg);
}