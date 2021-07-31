-- $ grep XXX rem.log > rem.xxx
-- $ lua5.4 x.lua rem.xxx > rem.x
-- $ lua5.4 y.lua rem.x > rem.y

--[[
function int (n)
    return math.floor(n)
end
function dec (n)
    return int(n*100)/100
end
]]

local f = assert(io.open(...))
local l = f:read'*l'
print('FPS-N-RATE', 'FRAMES', 'EVT', 'RTT', 'LCY', 'DRIFT', 'FREEZE', 'LATE')
while l do
    local dir,n,fps,evt,time,frames,evts,rtt,lcy,_,drift,_,freeze,_,late = string.match(l, "XXX ; ([^ ]*) ; 50.110. ; 300 ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; %s*([^ ]*) ; %s*([^ ]*) ; (%d+);([^ ]*) ; (%d+);([^ ]*) ; (%d+);([^ ]*)")
    --print(l)
    --print(n,fps,evt,frames,time,evts,rtt,lcy,drift,freeze,late)
    --lcy = lcy*(1000/fps)
    local s1 = string.format('%03d', fps)
    local s2 = string.format('%03d', n)
    local s3 = string.format('%03d', 60000/(evt/n))
    if n ~= '100' then
        print(s1..'-'..s2..'-'..s3,
              frames/(time/(1000/fps)), evts/(time/evt),
              rtt, lcy, drift, freeze, late)
    end
    l = f:read'*l'
end
