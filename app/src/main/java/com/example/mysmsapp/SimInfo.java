package com.example.mysmsapp;

import android.graphics.Bitmap;
import android.os.Build;
import android.telephony.SubscriptionInfo;

import androidx.annotation.RequiresApi;

/**
 * Created by bishwajeetbiswas on 30/01/21.
 */
public class SimInfo {
	public int subscriptionId;
	public int slotIndex;
	public String carrierName;
	public String carrierDisplayName;
	public String number;
	public int mcc;
	public int mnc;
	public String iccId;
	public Bitmap icon;


	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
	public static SimInfo convertSubscriptionInfoToSimInfo(SubscriptionInfo info) {
		SimInfo simInfo = new SimInfo();
		simInfo.subscriptionId = info.getSubscriptionId();
		simInfo.carrierName = info.getCarrierName().toString();
		simInfo.carrierDisplayName = (null == info.getDisplayName() || info.getDisplayName() == "") ? info.getCarrierName().toString() : info.getDisplayName().toString();
		simInfo.mcc = info.getMcc();
		simInfo.mnc = info.getMnc();
		simInfo.slotIndex = info.getSimSlotIndex();
		simInfo.number = info.getNumber();
		simInfo.iccId = info.getIccId();
		return simInfo;
	}

	@Override
	public String toString() {
		return "SimInfo{" +
				"subscriptionId=" + subscriptionId +
				", slotIndex=" + slotIndex +
				", carrierName='" + carrierName + '\'' +
				", carrierDisplayName='" + carrierDisplayName + '\'' +
				", number='" + number + '\'' +
				", mcc=" + mcc +
				", mnc=" + mnc +
				", icon=" + icon +
				'}';
	}
}