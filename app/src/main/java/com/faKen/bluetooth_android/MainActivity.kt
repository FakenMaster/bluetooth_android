package com.faKen.bluetooth_android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.faKen.bluetooth_android.databinding.ActivityMainBinding
import com.faKen.bluetooth_android.databinding.ItemPairedDevicesBinding
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_ENABLE_BT = 100
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(viewBinding.root)
        viewBinding.recyclerView.adapter = PairedDevicesAdapter(ArrayList())

        viewBinding.btnStart.setOnClickListener {
            if (bluetoothIsEnabled()) {
                startBluetooth();
            }
        }
        btn_paired.setOnClickListener {
            getPairedDevices()
        }
        viewBinding.btnScan.setOnClickListener {
            if (bluetoothIsEnabled()) {

            }
        }

        // discover device BroadcastReceiver
        registerDiscoveryReceiver()
    }


    // check if device supports bluetooth
    private fun bluetoothIsEnabled(): Boolean {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "The device does not support Bluetooth!", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    // open bluetooth
    private fun startBluetooth() {
        if (bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Bluetooth is on!", Toast.LENGTH_LONG).show()
            return
        }
        startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT)
    }

    // get paired devices
    private fun getPairedDevices() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter!!.bondedDevices
        pairedDevices?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.i("paired device", "name:$deviceName,MAC address:$deviceHardwareAddress")
        }
        pairedDevices?.toList()?.let {
            (viewBinding.recyclerView.adapter as PairedDevicesAdapter).setData(
                it
            )
        }
    }

    // discover device BroadcastReceiver
    private fun registerDiscoveryReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)

        registerReceiver(discoveryReceiver, filter)
    }
    // TODO 没有收到任何消息
    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent1: Intent) {
            when (intent1.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.i("发现新设备","$device")
                    (viewBinding.recyclerView.adapter as PairedDevicesAdapter).addData(device)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(discoveryReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth is on!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Open Bluetooth canceled!", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

class PairedDevicesAdapter(private var myDataSet: List<BluetoothDevice>) :
    RecyclerView.Adapter<PairedDevicesAdapter.ViewHolder>() {

    class ViewHolder(private val itemBinding: ItemPairedDevicesBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(device: BluetoothDevice) {
            itemBinding.apply {
                name.text = device.name
                macAddress.text = device.address
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemPairedDevicesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(myDataSet[position])
    }

    fun setData(data: List<BluetoothDevice>) {
        myDataSet = data
        notifyDataSetChanged()
    }
    fun addData(device:BluetoothDevice){
        val newData = ArrayList(myDataSet)
        newData.add(device)
        setData(newData)
    }

    override fun getItemCount() = myDataSet.size
}

