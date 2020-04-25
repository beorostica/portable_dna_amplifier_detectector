/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package no.nordicsemi.android.nrftoolbox.template;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileService;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileServiceReadyActivity;
import no.nordicsemi.android.nrftoolbox.template.savefile.SaveFileManager;
import no.nordicsemi.android.nrftoolbox.template.settings.SettingsActivity;

/**
 * Modify the Template Activity to match your needs.
 */
public class TemplateActivity extends BleProfileServiceReadyActivity<TemplateService.TemplateBinder> {
	@SuppressWarnings("unused")
	private final String TAG = "TemplateActivity";

	//The object that manages the save file:
	private SaveFileManager mSaveFileManager;

	// TODO change view references to match your need
	private TextView[] valueViewArray = new TextView[6];
	private EditText[] editTextDurationArray = new EditText[3];
	private TextView[] textViewContArray = new TextView[4];

	@Override
	protected void onCreateView(final Bundle savedInstanceState) {
		// TODO modify the layout file(s). By default the activity shows only one field - the Heart Rate value as a sample
		setContentView(R.layout.activity_feature_template);
		setGUI();

		//Create the save file manager:
		mSaveFileManager = new SaveFileManager(this);

	}

	private void setGUI() {

		// TODO assign your views to fields
		for(int i = 0; i < valueViewArray.length; i++){
			int resId = getResources().getIdentifier("value" + i, "id", getPackageName());
			valueViewArray[i] = findViewById(resId);
		}
        for(int i = 0; i < editTextDurationArray.length; i++){
            int resId = getResources().getIdentifier("editText_timeDuration" + i, "id", getPackageName());
            editTextDurationArray[i] = findViewById(resId);
            // Set a filter to receive timeDuration predefined values: 0 <= hrs <= 17, 0 <= mins <= 59, 0 <= secs <= 59
            int numberMax = 59;
            if(i == 0){ numberMax = 17;}
            editTextDurationArray[i].setFilters(new InputFilter[]{new InputFilterMinMax(0, numberMax)});
        }
        for(int i = 0; i < textViewContArray.length; i++){
            int resId = getResources().getIdentifier("textViewCont" + i, "id", getPackageName());
            textViewContArray[i] = findViewById(resId);
        }

		findViewById(R.id.action_read).setOnClickListener(v -> {
			if (isDeviceConnected()) {
				getService().performReadCharacteristicStat();
			}
		});

		findViewById(R.id.action_write_characteristic_stat).setOnClickListener(v -> {
			if (isDeviceConnected()) {
			    // Get the timeDuration from the UI:
                int[] durationTime = new int[editTextDurationArray.length];
                for(int i = 0; i < editTextDurationArray.length; i++){
                    durationTime[i]  = Integer.parseInt(editTextDurationArray[i].getText().toString());
                }
                Log.v("On BUTTON", "hrs: " + durationTime[0] + ", mins: " + durationTime[1] + ", secs: " + durationTime[2]);
				getService().performSendCommandFromPhone(durationTime);
			}
		});

	}

	@Override
	protected void onInitialize(final Bundle savedInstanceState) {
		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, makeIntentFilter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        mSaveFileManager.closeFile();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
	}

    // After disconnection close the file:
    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        mSaveFileManager.closeFile();
    }

	@Override
	protected void setDefaultUI() {
		// TODO clear your UI
		for(int i = 0; i < valueViewArray.length; i++){
			valueViewArray[i].setText(R.string.not_available_value);
		}
	}

	@Override
	protected int getLoggerProfileTitle() {
		return R.string.template_feature_title;
	}

	@Override
	protected int getAboutTextId() {
		return R.string.template_about_text;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.settings_and_about, menu);
		return true;
	}

	@Override
	protected boolean onOptionsItemSelected(final int itemId) {
		switch (itemId) {
			case R.id.action_settings:
				final Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
		}
		return true;
	}

	@Override
	protected int getDefaultDeviceName() {
		return R.string.template_default_name;
	}

	@Override
	protected UUID getFilterUUID() {
		// TODO this method may return the UUID of the service that is required to be in the advertisement packet of a device in order to be listed on the Scanner dialog.
		// If null is returned no filtering is done.
		return TemplateManager.UUID_SERVICE_STAT;
	}

	@Override
	protected Class<? extends BleProfileService> getServiceClass() {
		return TemplateService.class;
	}

	@Override
	protected void onServiceBound(final TemplateService.TemplateBinder binder) {
		// not used
	}

	@Override
	protected void onServiceUnbound() {
		// not used
	}

	@Override
	public void onServicesDiscovered(@NonNull final BluetoothDevice device, final boolean optionalServicesFound) {
		// this may notify user or show some views
	}

	//@Override
	//public void onDeviceDisconnected(@NonNull final BluetoothDevice device) {
	//	super.onDeviceDisconnected(device);
	//}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			final BluetoothDevice device = intent.getParcelableExtra(TemplateService.EXTRA_DEVICE);

			if (TemplateService.BROADCAST_CHARACTERISTIC_STAT_UPDATE.equals(action)) {

				// Get read or notified data and update UI:
				final int[] dataArray = intent.getIntArrayExtra(TemplateService.EXTRA_DATA_CHARACTERISTIC_STAT_UPDATE);

				//Update the Write Stat Characteristic Button:
				Button buttonWrite = (Button) findViewById(R.id.action_write_characteristic_stat);
				if(dataArray[0] == 0 && dataArray[2] == 0) {
					buttonWrite.setEnabled(true);
					buttonWrite.setText(R.string.template_action_write_start);
                    mSaveFileManager.closeFile();
				} else if (dataArray[0] == 1 && dataArray[2] == 1) {
					buttonWrite.setEnabled(true);
					buttonWrite.setText(R.string.template_action_write_stop);
                    mSaveFileManager.createFile(dataArray);
				} else if (dataArray[0] == 0 && dataArray[2] == 1) {
					buttonWrite.setEnabled(false);
					buttonWrite.setText(R.string.template_action_write_wait);
                    mSaveFileManager.createFile(dataArray);
				}

				// Update the timeDuration received from nRF52 (useful when first connection):
                for(int i = 0; i < editTextDurationArray.length; i++){
                    editTextDurationArray[i].setText(String.valueOf(dataArray[i+9]));
                }


            }
			if (TemplateService.BROADCAST_CHARACTERISTIC_SENS_UPDATE.equals(action)) {

				// Get read or notified data and update UI:
				final int[] dataArray = intent.getIntArrayExtra(TemplateService.EXTRA_DATA_CHARACTERISTIC_SENS_UPDATE);
				for(int i = 0; i < valueViewArray.length; i++){
					valueViewArray[i].setText(String.valueOf(dataArray[i]));
				}

				//Write a line in the file:
                mSaveFileManager.writeLine(dataArray);

			}
			if (TemplateService.BROADCAST_CHARACTERISTIC_CONT_UPDATE.equals(action)) {

				// Get read or notified data and update UI:
				final int[] dataArray = intent.getIntArrayExtra(TemplateService.EXTRA_DATA_CHARACTERISTIC_CONT_UPDATE);
				for(int i = 0; i < textViewContArray.length; i++){
                    textViewContArray[i].setText(String.valueOf(dataArray[i]));
				}

			}

		}
	};

	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(TemplateService.BROADCAST_CHARACTERISTIC_STAT_UPDATE);
		intentFilter.addAction(TemplateService.BROADCAST_CHARACTERISTIC_SENS_UPDATE);
        intentFilter.addAction(TemplateService.BROADCAST_CHARACTERISTIC_CONT_UPDATE);
		return intentFilter;
	}


    // This class is to define filters to receive timeDuration predefined values:
    // 0 <= hrs <= 17, 0 <= mins <= 59, 0 <= secs <= 59
    // in the user interface.
    private class InputFilterMinMax implements InputFilter {
        private int minimumValue;
        private int maximumValue;

        public InputFilterMinMax(int minimumValue, int maximumValue) {
            this.minimumValue = minimumValue;
            this.maximumValue = maximumValue;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length()));
                if (isInRange(minimumValue, maximumValue, input))
                    return null;
            }
            catch (NumberFormatException nfe) {
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }

    }

}
