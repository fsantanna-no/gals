#if 0
#!/bin/sh
gcc -Wall -I .. `sdl2-config --cflags` gals.c main.c -o xmain `sdl2-config --libs` -lSDL2_net
exit
#endif

#include <assert.h>
#include <SDL2/SDL.h>
#include "gals.h"

enum {
    EVT_TIME, EVT_KEY
};

void _assert (int x) {}

int main (int argc, char** argv) {
    assert(argc == 2);
	assert(SDL_Init(SDL_INIT_VIDEO) == 0);

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
        int pay;
        gals_wait(&now, &evt, &pay, NULL);
        //printf("now=%ld evt=%d\n", now, evt);

        SDL_SetRenderDrawColor(ren, 0xFF,0xFF,0xFF,0xFF);
        SDL_RenderClear(ren);

        if (evt == EVT_KEY) {
            switch (pay) {
                case SDLK_LEFT:  { xdir=-1; ydir=0; break; }
                case SDLK_RIGHT: { xdir= 1; ydir=0; break; }
                case SDLK_UP:    { ydir=-1; xdir=0; break; }
                case SDLK_DOWN:  { ydir= 1; xdir=0; break; }
                case SDLK_SPACE: { ydir= 0; xdir=0; printf("PAUSE: t=%ld, xy=(%d,%d)\n",now,x,y); break; }
            }
        }

        SDL_Rect r = { x, y, 10, 10 };
        SDL_SetRenderDrawColor(ren, 0xFF,0x00,0x00,0xFF);
        SDL_RenderFillRect(ren, &r);

        if (now!=prv && evt==EVT_TIME) {
            x += 5 * xdir;
            y += 5 * ydir;
        }
        prv = now;


        {
            SDL_Event inp;
            while (SDL_PollEvent(&inp)) {
                if (inp.type == SDL_QUIT) {
                    exit(0);
                }
                if (inp.type == SDL_KEYDOWN) {
                    int key = inp.key.keysym.sym;
                    if (key==SDLK_LEFT || key==SDLK_RIGHT ||
                        key==SDLK_UP   || key==SDLK_DOWN  ||
                        key==SDLK_SPACE
                    ) {
                        SDL_Rect r = { 190, 190, 20, 20 };
                        SDL_SetRenderDrawColor(ren, 0x77,0x77,0x77,0x77);
                        SDL_RenderFillRect(ren, &r);
                        gals_emit(EVT_KEY, key, 0);
                    }
                }
            }
        }

        SDL_RenderPresent(ren);
	}

	gals_disconnect();
	return 0;
}

