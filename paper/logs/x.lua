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
print('N-RATE-FPS', 'FRAMES', 'EVT', 'RTT', 'LAT', 'DRIFT', 'FREEZE', 'LATE')
while l do
    local dir,n,fps,evt,time,frames,evts,rtt,lat,_,drift,_,freeze,_,late = string.match(l, "XXX ; ([^ ]*) ; 50.110. ; 300 ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; %s*([^ ]*) ; %s*([^ ]*) ; (%d+);([^ ]*) ; (%d+);([^ ]*) ; (%d+);([^ ]*)")
    --print(l)
    --print(n,fps,evt,frames,time,evts,rtt,lat,drift,freeze,late)
    print(string.format('%03d',n)..'-'..string.format('%03d',60000/(evt/n))..'-'..string.format('%03d',fps),
          frames/(time/(1000/fps)), evts/(time/evt),
          rtt, lat, drift, freeze, late)
    l = f:read'*l'
end
