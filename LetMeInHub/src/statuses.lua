statuses = {
    lock_status     = { path = '/root/devices/device[@id="6"]/states/state[@service="urn:micasaverde-com:serviceId:DoorLock1" and @variable="Status"]/@value', coerce = function(v) return tonumber(v) ~= 0 end },
    latch_status    = { path = '/root/devices/device[@id="7"]/states/state[@service="urn:upnp-org:serviceId:SwitchPower1" and @variable="Status"]/@value', coerce = function(v) return tonumber(v) ~= 0 end },
    lock_battery    = { path = '/root/devices/device[@id="6"]/states/state[@service="urn:micasaverde-com:serviceId:HaDevice1" and @variable="BatteryLevel"]/@value', coerce = function(v) return tonumber(v) end },
    visible_devices = { path = '/root/@visible_devices', coerce = function(v) return tonumber(v) end }
}
