package com.bella.testapp.service

import android.content.Context
import org.fourthline.cling.UpnpServiceImpl
import org.fourthline.cling.binding.LocalServiceBinder
import org.fourthline.cling.model.meta.DeviceDetails
import org.fourthline.cling.model.meta.DeviceIdentity
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.ManufacturerDetails
import org.fourthline.cling.model.meta.ModelDetails
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN

class DlnaService(context: Context) {

    private val upnpService = UpnpServiceImpl()

    init {
//        val localDevice = createDevice(context)
//        upnpService.registry.addDevice(localDevice)
    }


}