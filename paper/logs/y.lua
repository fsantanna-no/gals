local f = assert(io.open(...))
local l = f:read'*l'
print(l)
l = f:read'*l'
while l do
    local LBL,FRAMES,EVT,RTT,LAT,DRIFT,FREEZE,LATE =
        string.match(l, "([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)")
    local a1,a2,a3,a4,a5,a6,a7 = tonumber(FRAMES),tonumber(EVT),tonumber(RTT),tonumber(LAT),tonumber(DRIFT),tonumber(FREEZE),tonumber(LATE)
    for i=1, 4 do
        l = f:read'*l'
        local lbl,frames,evt,rtt,lat,drift,freeze,late =
            string.match(l, "([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)")
        --print(lbl,frames,evt,rtt,lat,drift,freeze,late)
        assert(LBL == lbl)
        local frames = tonumber(frames)
        local evt    = tonumber(evt)
        local rtt    = tonumber(rtt)
        local lat    = tonumber(lat)
        local drift  = tonumber(drift)
        local freeze = tonumber(freeze)
        local late   = tonumber(late)
        print(l)
        FRAMES = FRAMES + frames --; assert(1.1>=frames and frames>=0.9)
        --print(a2,evt)
        EVT    = EVT    + evt    --; assert(a2*1.1>=evt    and evt   *1.1>=a2)
        --print(a3,rtt)
        RTT    = RTT    + rtt    --; assert(a3*1.5>rtt    and rtt   *1.5>=a3)
        --print(a4,lat)
        LAT    = LAT    + lat    --; assert(a4*1.2>=lat    and lat   *1.2>=a4)
        --print(a5,drift)
        DRIFT  = DRIFT  + drift  --; assert(a5*1.2>=drift  and drift *1.2>=a5)
        --print(a6,freeze)
        FREEZE = FREEZE + freeze --; assert(a6*1.2>=freeze and freeze*1.2>=a6)
        --print(a7,late)
        LATE   = LATE   + late   --; assert(a7*1.2>=late   and late  *1.2>=a7)
    end
    l = f:read'*l'
    print(LBL, FRAMES/5, EVT/5, RTT/5, LAT/5, DRIFT/5, FREEZE/5, LATE/5)
end
