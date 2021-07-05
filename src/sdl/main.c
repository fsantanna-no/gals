#if 0
#!/bin/sh
gcc -Wall `sdl2-config --cflags` main.c -o xmain `sdl2-config --libs` -lSDL2_net
exit
#endif

#include <assert.h>
#include <endian.h>
#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>

void _assert (int x) {}

int main (int argc, char** argv) {
    assert(argc == 2);
	assert(SDL_Init(SDL_INIT_VIDEO) == 0);
	assert(SDLNet_Init() == 0);

    SDL_Window*   win = SDL_CreateWindow("SDL", 0,0, 400,400, SDL_WINDOW_SHOWN);
    SDL_Renderer* ren = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);

	IPaddress ip;
	assert(SDLNet_ResolveHost(&ip, "localhost", atoi(argv[1])) == 0);
	TCPsocket s = SDLNet_TCP_Open(&ip);
	assert(s != NULL);

    int x = 10;
    int y = 10;
    int xdir = 1;
    int ydir = 0;

	while (1) {
        char buf[sizeof(uint64_t) + sizeof(uint32_t)];
        int n = 0;
        while (n < sizeof(buf)) {
            int x = SDLNet_TCP_Recv(s, &buf[n], sizeof(buf)-n);
            assert(x > 0);
            n += x;
        }
        uint64_t now = be64toh(*(uint64_t*)&buf[0]);
        uint32_t evt = be32toh(*(uint32_t*)&buf[sizeof(uint64_t)]);
        printf("now=%ld evt=%d\n", now, evt);

        SDL_SetRenderDrawColor(ren, 0xFF,0xFF,0xFF,0xFF);
        SDL_RenderClear(ren);

        switch (evt) {
            case 1: { xdir=-1; ydir=0; break; }
            case 2: { xdir= 1; ydir=0; break; }
            case 3: { ydir=-1; xdir=0; break; }
            case 4: { ydir= 1; xdir=0; break; }
        }            

        SDL_Rect r = { x, y, 10, 10 };
        SDL_SetRenderDrawColor(ren, 0xFF,0x00,0x00,0xFF);        
        SDL_RenderFillRect(ren, &r);
        SDL_RenderPresent(ren);

        x += 5 * xdir;
        y += 5 * ydir;


        {
            SDL_Event inp;
            while (SDL_PollEvent(&inp)) {
                uint32_t n = 0;
                if (inp.type == SDL_KEYDOWN) {
                    switch (inp.key.keysym.sym) {
                        case SDLK_LEFT:  { n=1; break; }
                        case SDLK_RIGHT: { n=2; break; }
                        case SDLK_UP:    { n=3; break; }
                        case SDLK_DOWN:  { n=4; break; }
                    }
                }
                if (n != 0) {
                    n = htobe32(n);
                    assert(SDLNet_TCP_Send(s, &n, sizeof(n)) == sizeof(n));
                }
            }
        }
	}

	SDLNet_TCP_Close(s);
	SDLNet_Quit();
	return 0;
}

