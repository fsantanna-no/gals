local f = assert(io.open(...))
local l = f:read'*l'
print(l)
l = f:read'*l'
while l do
    local LBL,FRAMES,EVT,RTT,LAT,DRIFT,FREEZE,LATE =
        string.match(l, "([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)")
    for i=1, 4 do
        l = f:read'*l'
        local lbl,frames,evt,rtt,lat,drift,freeze,late =
            string.match(l, "([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)")
        --print(lbl,frames,evt,rtt,lat,drift,freeze,late)
        assert(LBL == lbl)
        FRAMES = FRAMES + frames
        EVT    = EVT    + evt
        RTT    = RTT    + rtt
        LAT    = LAT    + lat
        DRIFT  = DRIFT  + drift
        FREEZE = FREEZE + freeze
        LATE   = LATE   + late
    end
    l = f:read'*l'
    print(LBL, FRAMES/5, EVT/5, RTT/5, LAT/5, DRIFT/5, FREEZE/5, LATE/5)
end
