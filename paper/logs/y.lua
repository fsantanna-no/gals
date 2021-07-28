--[[
function int (n)
    return math.floor(n)
end
function dec (n)
    return math.floor(n*100)/100
end
]]

local f = assert(io.open(...))
local l = f:read'*l'
print('RATE-N-FPS', 'REPS', 'FRAMES', 'EVT', 'RTT', 'LCY', 'DRIFT', 'FREEZE', 'LATE')
l = f:read'*l'
t = {}
while l do
    local lbl,frames,evt,rtt,lcy,drift,freeze,late =
        string.match(l, "([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)")

    frames = tonumber(frames)
    evt    = tonumber(evt)
    rtt    = tonumber(rtt)
    lcy    = tonumber(lcy)
    drift  = tonumber(drift)
    freeze = tonumber(freeze)
    late   = tonumber(late)

    t[lbl] = t[lbl] or { n=0, lbl=lbl, frames=0, evt=0, rtt=0, lcy=0, drift=0, freeze=0, late=0 }
    local T = t[lbl]

    local ok = true

    T.n      = T.n + 1
    T.frames = T.frames + frames ; ok = ok and (1.1>=frames and frames>=0.9)
    T.evt    = T.evt    + evt    ; ok = ok and (1.1>=evt    and evt   >=0.9)
    T.rtt    = T.rtt    + rtt    --; assert(a3*1.5>rtt    and rtt   *1.5>=a3)
    T.lcy    = T.lcy    + lcy    --; assert(a4*1.2>=lcy    and lcy   *1.2>=a4)
    T.drift  = T.drift  + drift  --; assert(a5*1.2>=drift  and drift *1.2>=a5)
    T.freeze = T.freeze + freeze --; assert(a6*1.2>=freeze and freeze*1.2>=a6)
    T.late   = T.late   + late   --; assert(a7*1.2>=late   and late  *1.2>=a7)

    if not ok then
        io.stderr:write(l..'\n')
    end

    l = f:read'*l'
end

local x = {}
for k in pairs(t) do
    --print(k)
    x[#x+1] = k
end
table.sort(x)

for _,k in ipairs(x) do
    local T = t[k]
    print(T.lbl, T.n, T.frames/T.n, T.evt/T.n, T.rtt/T.n, T.lcy/T.n, T.drift/T.n, T.freeze/T.n, T.late/T.n)
end
