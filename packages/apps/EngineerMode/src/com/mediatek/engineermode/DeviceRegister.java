package com.mediatek.engineermode;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.mediatek.common.dm.DmAgent;

public class DeviceRegister extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "EM/DeviceRegister";

    private static final String KEY_SMS_REGISTER_SWITCH = "ct_sms_register_switch";
    private static final String DMAGENT = "DmAgent";
    private static final String TURN_ON = "On";
    private static final String TURN_OFF = "Off";
    private ListPreference mListPreference;
    private DmAgent mDMAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.device_register);

        IBinder iBinder = ServiceManager.getService(DMAGENT);
        mDMAgent = DmAgent.Stub.asInterface(iBinder);

        mListPreference = (ListPreference) findPreference(KEY_SMS_REGISTER_SWITCH);
        mListPreference.setOnPreferenceChangeListener(this);
        final int savedCTAValue = getSavedCTAValue();
        final String summary = savedCTAValue == 1 ? TURN_ON : TURN_OFF;
        mListPreference.setSummary(summary);
        mListPreference.setValue(String.valueOf(savedCTAValue));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_SMS_REGISTER_SWITCH)) {
            try {
                setCTAValue((String) newValue);
            } catch (NumberFormatException ex) {
                Log.e("@M_" + TAG, "setCTAValue NumberFormatException");
            }
            final boolean isEnabled = getSavedCTAValue() == 1;
            mListPreference.setValue(isEnabled ? "1" : "0");
            final String summary = isEnabled ? TURN_ON : TURN_OFF;
            mListPreference.setSummary(summary);
        }
        return false;
    }

    private int getSavedCTAValue() {
        if (mDMAgent == null) {
            Log.v("@M_" + TAG, "getSavedCTAValue DMAgent null");
            return 0;
        }

        int savedCTA = 1;
        try {
            byte[] cta = mDMAgent.getRegisterSwitch();
            if (cta != null) {
                savedCTA = Integer.parseInt(new String(cta));
            }
        } catch (RemoteException e) {
            Log.v("@M_" + TAG, "RemoteException ");
        } catch (NumberFormatException e) {
            Log.v("@M_" + TAG, "NumberFormatException ");
        }
        Log.i("@M_" + TAG, "Get savedCTA = [" + savedCTA + "]");
        return savedCTA;
    }

    private void setCTAValue(String cta) {
        if (mDMAgent == null) {
            Log.v("@M_" + TAG, "setCTAValue DMAgent null");
            return;
        }

        try {
            mDMAgent.setRegisterSwitch(cta.getBytes());
        } catch (RemoteException e) {
            Log.e("@M_" + TAG, "setCTAValue RemoteException!");
        }
        Log.i("@M_" + TAG, "save CTA [" + cta + "]");
    }

}
