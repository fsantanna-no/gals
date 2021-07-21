-- $ grep XXX rem.log > rem.xxx
-- $ lua5.4 x.lua rem.xxx > rem.x
-- $ lua5.4 y.lua rem.x > rem.y


function dec (n)
    return math.floor(n*100)/100
end

local f = assert(io.open(...))
local l = f:read'*l'
print('  N-FPS-RATE', 'FRAMES', 'EVT', 'RTT', 'LAT', 'DRIFT', 'FREEZE', 'LATE')
while l do
    local dir,n,fps,evt,time,frames,evts,rtt,lat,_,drift,_,freeze,_,late = string.match(l, "XXX ; ([^ ]*) ; 50.110. ; 600 ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; (%d+) ; %s*([^ ]*) ; %s*([^ ]*) ; (%d+);([^ ]*) ; (%d+);([^ ]*) ; (%d+);([^ ]*)")
    --print(l)
    print(string.format('%03d',n)..'-'..string.format('%03d',fps)..'-'..string.format('%05d',math.floor(evt/n)),
          dec(frames/(time/(1000/fps))),
          dec(evts/(time/evt)),
          rtt, lat, drift, freeze, late)
    l = f:read'*l'
end
