package com.easyweigh.trancell;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.easyweigh.R;
import com.easyweigh.vsp.ble.BleBaseDeviceManager;
import com.easyweigh.vsp.ble.IBleBaseActivityUiCallback;


/**
 * Base activity for all the activities that will be doing BLE functionalities
 * 
 * @author Kyriakos.Alexandrou
 */
public abstract class WeighBaseActivity extends BaseActivity implements
		IBleBaseActivityUiCallback
{
	private final static String TAG = "BleBaseActivity";
	public BleBaseDeviceManager mBleBaseDeviceManager;

	protected TextView mValueName, mValueDeviceAddress, mValueRSSI,
			mValueBattery;
	protected Button mBtnScan;

	@Override
	public void onCreate(Bundle savedInstanceState, int layoutResID)
	{
		super.onCreate(savedInstanceState, layoutResID);
	}

	/**
	 * Set the mBleBaseDeviceManager to the specific device manager for each
	 * application within the toolkit. This ensures it is not null
	 * 
	 * @param bleBaseDeviceManager
	 *            the specific BLE manager to set it to.
	 */
	protected void setBleBaseDeviceManager(
			BleBaseDeviceManager bleBaseDeviceManager)
	{
		mBleBaseDeviceManager = bleBaseDeviceManager;
	}

	/**
	 * get the generic BLE base manager and cast it in order to get the correct
	 * manager.
	 * <p>
	 * example: (ThermometerManager) getBleBaseDeviceManager();
	 * 
	 * @return
	 */
	protected BleBaseDeviceManager getBleBaseDeviceManager()
	{
		return mBleBaseDeviceManager;
	}

	@Override
	public void bindViews()
	{
		super.bindViews();
		mBtnScan = (Button) findViewById(R.id.btnScan);
	}

	@Override
	public void setListeners()
	{
		super.setListeners();

		// set onClickListener for the scan button
		mBtnScan.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				switch (v.getId())
				{
				case R.id.btnScan:
				{
					if (mBluetoothAdapterWrapper.isEnabled() == false)
					{
						Log.w(TAG, "Bluetooth must be on to start scanning.");
						Toast.makeText(getApplication(),
								"Bluetooth must be on to start scanning.",
								Toast.LENGTH_SHORT).show();
						return;
					}
					else if (mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTED
							&& mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTING)
					{

						// do a scan operation
						if (isPrefPeriodicalScan == true)
						{
							mBluetoothAdapterWrapper.startBleScanPeriodically();
						}
						else
						{
							mBluetoothAdapterWrapper.startBleScan();
						}
						mDialogFoundDevices.show();
					}
					else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
					{
						mBleBaseDeviceManager.disconnect();

					}
					else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTING)
					{
						Toast.makeText(getApplication(),
								"Wait for connection!", Toast.LENGTH_SHORT)
								.show();
					}
					uiInvalidateBtnState();

					break;
				}
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{

		case android.R.id.home:
			mBleBaseDeviceManager.disconnect();
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (isInNewScreen == true || isPrefRunInBackground == true)
		{
			// let the app run normally in the background
		}
		else
		{
			// stop scanning or disconnect if we are connected
			if (mBluetoothAdapterWrapper.isBleScanning())
			{
				mBluetoothAdapterWrapper.stopBleScan();
			}
			else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTING
					|| mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
			{
				mBleBaseDeviceManager.disconnect();
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		mBleBaseDeviceManager.disconnect();
		finish();
	}


	@Override
	public void uiInvalidateBtnState()
	{
		this.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{

				if (mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTED
						&& mBleBaseDeviceManager.getConnectionState() != BluetoothProfile.STATE_CONNECTING)
				{
					mBtnScan.setText(R.string.btn_scan);
				}
				else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED)
				{
					mBtnScan.setText(R.string.btn_disconnect);
				}
				else if (mBleBaseDeviceManager.getConnectionState() == BluetoothProfile.STATE_CONNECTING)
				{
					mBtnScan.setText(R.string.btn_connecting);
				}

				invalidateOptionsMenu();
			}
		});
	}

	@Override
	public void onUiConnecting()
	{}

	@Override
	public void onUiDisconnecting()
	{}



	@Override
	public void onUiDisconnected(final int status)
	{
		// set views to default
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Log.i(TAG, "onUiDisconnected status: " + status);
				mBtnScan.setText(getResources().getString(R.string.btn_scan));
			}
		});
	}

	@Override
	public void onUiBatteryRead(final String valueBattery)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueBattery.setText(valueBattery);
			}
		});
	}

	@Override
	public void onUiReadRemoteRssi(final int valueRSSI)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				mValueRSSI.setText(valueRSSI + " db");
			}
		});
	}

	@Override
	protected void onDialogFoundDevicesItemClick(AdapterView<?> arg0,
			View view, int position, long id)
	{
		final BluetoothDevice device = mListFoundDevicesHandler
				.getDevice(position);
		if (device == null)
			return;

		mBluetoothAdapterWrapper.stopBleScan();
		mDialogFoundDevices.dismiss();
		mBleBaseDeviceManager.connect(device, false);
		uiInvalidateBtnState();
	}

	@Override
	protected void onDialogFoundDevicesCancel(DialogInterface arg0)
	{
		mBluetoothAdapterWrapper.stopBleScan();
		uiInvalidateBtnState();
	}

	/**
	 * bind the device name, device address, device RSSI and device battery to
	 * its views
	 */
	protected void bindCommonViews()
	{
		mValueName = (TextView) findViewById(R.id.valueDeviceName);
		mValueDeviceAddress = (TextView) findViewById(R.id.valueDeviceAddress);
		mValueRSSI = (TextView) findViewById(R.id.valueDeviceRssi);
		mValueBattery = (TextView) findViewById(R.id.valueDeviceBattery);
	}

	/**
	 * set device battery, device name, device address
	 */
	protected void setCommonViews()
	{
		mValueBattery
				.setText(getResources().getString(R.string.non_applicable));
		mValueName
				.setText(mBleBaseDeviceManager.getBluetoothDevice().getName());
		mValueDeviceAddress.setText(mBleBaseDeviceManager.getBluetoothDevice()
				.getAddress());
	}

	/**
	 * set device battery, device name, device address to non applicable
	 */
	protected void setCommonViewsToNonApplicable()
	{
		mValueBattery
				.setText(getResources().getString(R.string.non_applicable));
		mValueName.setText(getResources().getString(R.string.non_applicable));
		mValueRSSI.setText(getResources().getString(R.string.non_applicable));
		mValueDeviceAddress.setText(getResources().getString(
				R.string.non_applicable));
	}

}
