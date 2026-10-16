package com.app.streamtest.domain.device

/** Tells what this TV can play. Implemented in :data with Android APIs. */
interface DeviceInfoRepository {
    suspend fun getCapabilities(): DeviceCapabilities

    /** The full picture, for the Settings screen. */
    suspend fun getDetails(): DeviceDetails
}
