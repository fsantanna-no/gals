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

static TCPsocket s = NULL;

int tcp_recv_u64 () {
    uint64_t v_;
    int i = 0;
    int N = sizeof(v_);
    while (i < N) {
        i += SDLNet_TCP_Recv(s, &((char*)&v_)[i], N-i);
    }
    return be64toh(v_);
}

int tcp_recv_s32 () {
    uint32_t v_;
    int i = 0;
    int N = sizeof(v_);
    while (i < N) {
        i += SDLNet_TCP_Recv(s, &((char*)&v_)[i], N-i);
    }
    return be32toh(v_);
}

void tcp_send_s32 (int v) {
    uint32_t _v = htobe32(v);
    assert(SDLNet_TCP_Send(s, &_v, sizeof(_v)) == sizeof(_v));
}

int gals_connect (int port, int fps) {
	IPaddress ip;
	assert(SDLNet_ResolveHost(&ip, "localhost", port) == 0);
	s = SDLNet_TCP_Open(&ip);
	assert(s != NULL);

    tcp_send_s32(fps);
    return tcp_recv_s32();
}

void gals_wait (uint64_t* now, int* evt) {
    *now = tcp_recv_u64();
    *evt = tcp_recv_s32();
}

int main (int argc, char** argv) {
    assert(argc == 2);
	assert(SDL_Init(SDL_INIT_VIDEO) == 0);
	assert(SDLNet_Init() == 0);

    SDL_Window*   win = SDL_CreateWindow("SDL", 0,0, 400,400, SDL_WINDOW_SHOWN);
    SDL_Renderer* ren = SDL_CreateRenderer(win, -1, SDL_RENDERER_ACCELERATED);

    int self = gals_connect(atoi(argv[1]), 20);
    printf(">>> %d\n", self);

    int x = 10;
    int y = 10;
    int xdir = 0;
    int ydir = 0;
    uint64_t prv = 0;

	while (1) {
        uint64_t now;
        int evt;
        gals_wait(&now, &evt);
        //printf("now=%ld evt=%d\n", now, evt);

        SDL_SetRenderDrawColor(ren, 0xFF,0xFF,0xFF,0xFF);
        SDL_RenderClear(ren);

        switch (evt) {
            case 1: { xdir=-1; ydir=0; break; }
            case 2: { xdir= 1; ydir=0; break; }
            case 3: { ydir=-1; xdir=0; break; }
            case 4: { ydir= 1; xdir=0; break; }
            case 5: { ydir= 0; xdir=0; printf("PAUSE: t=%ld, xy=(%d,%d)\n",now,x,y); break; }
        }

        SDL_Rect r = { x, y, 10, 10 };
        SDL_SetRenderDrawColor(ren, 0xFF,0x00,0x00,0xFF);
        SDL_RenderFillRect(ren, &r);

        if (now!=prv && evt==0) {
            x += 5 * xdir;
            y += 5 * ydir;
        }
        prv = now;


        {
            SDL_Event inp;
            while (SDL_PollEvent(&inp)) {
                uint32_t n = 0;
                if (inp.type == SDL_QUIT) {
                    exit(0);
                }
                if (inp.type == SDL_KEYDOWN) {
                    switch (inp.key.keysym.sym) {
                        case SDLK_LEFT:  { n=1; break; }
                        case SDLK_RIGHT: { n=2; break; }
                        case SDLK_UP:    { n=3; break; }
                        case SDLK_DOWN:  { n=4; break; }
                        case SDLK_SPACE: { n=5; break; }
                    }
                }
                if (n != 0) {
                    SDL_Rect r = { 190, 190, 20, 20 };
                    SDL_SetRenderDrawColor(ren, 0x77,0x77,0x77,0x77);
                    SDL_RenderFillRect(ren, &r);
                    n = htobe32(n);
                    assert(SDLNet_TCP_Send(s, &n, sizeof(n)) == sizeof(n));
                }
            }
        }

        SDL_RenderPresent(ren);
	}

	SDLNet_TCP_Close(s);
	SDLNet_Quit();
	return 0;
}

