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
--print('FPS-N-RATE', 'REPS', 'FRAMES', 'EVT', 'RTT', 'LCY', 'DRIFT', 'FREEZE', 'LATE')
l = f:read'*l'
t = {}
while l do
    local fps,n,rate,frames,evt,rtt,lcy,drift,freeze,late =
        string.match(l, "([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)%s([^%s]*)")

    fps    = tonumber(fps)
    n      = tonumber(n)
    rate   = tonumber(rate)
    frames = tonumber(frames)
    evt    = tonumber(evt)
    rtt    = tonumber(rtt)
    lcy    = tonumber(lcy)
    drift  = tonumber(drift)
    freeze = tonumber(freeze)
    late   = tonumber(late)

    local s1 = string.format('%03d', fps)
    local s2 = string.format('%03d', n)
    local s3 = string.format('%03d', 60000/(rate/n))
    local lbl = s1..'-'..s2..'-'..s3

    t[lbl] = t[lbl] or { x=0, lbl=lbl, fps=fps, n=n, rate=rate/n/1000, frames=0, evt=0, rtt=0, lcy=0, drift=0, freeze=0, late=0 }
    local T = t[lbl]

    local ok = true

    T.x      = T.x + 1
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

local LBLS = {}
for lbl in pairs(t) do
    --print(k)
    LBLS[#LBLS+1] = lbl
end
table.sort(LBLS)

local TIT = { fps='FPS', n='N', rate='Rate' }

for _,prop in ipairs{'fps','n','rate'} do
    local x = {}
    for _,lbl in ipairs(LBLS) do
        local T = t[lbl]
        x[#x+1] = T[prop]
    end
    print(TIT[prop], table.concat(x,'\t'))
end

local TIT = { lcy='Latency', drift='Drift', freeze='Freeze' }

for _,prop in ipairs{'lcy','drift','freeze'} do
    local x = {}
    for _,lbl in ipairs(LBLS) do
        local T = t[lbl]
        x[#x+1] = T[prop]/T.x
    end
    print(TIT[prop], table.concat(x,'\t'))
end
