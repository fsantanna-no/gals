int gals_connect (int port, int fps);
void gals_disconnect (void);
void gals_wait (uint64_t* now, int* evt, int* pay1, int* pay2, int* pay3);
void gals_emit (int evt, int pay1, int pay2, int pay3);
