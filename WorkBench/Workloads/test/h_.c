#include <stdio.h>
#include <xmmintrin.h>
     
    /*
     *
     * Compiling:
     * 
     * gcc -O3 -march=pentium3 -mfpmath=sse -funroll-loops 
     *        -fomit-frame-pointer -o gcc_macros  gcc_macros.c
     *
     */
     
float inp1[4] = { 1.2, 3.5, 1.7, 2.8 };
float inp2[4] = { -0.7, 2.6, 3.3, -4.0 };
float out[4];


// __m128 : this is 128 bits long. It allows the storage of 4 floating point numbers

__m128 a, b, c;

// _mm_load_ps and _mm_store_ps: The address must be 16-byte aligned


float inp_sse1[4] __attribute__((aligned(16))) = { 1.2, 3.5, 1.7, 2.8 };
float inp_sse2[4] __attribute__((aligned(16))) = { -0.7, 2.6, 3.3, -4.0 };
float out_sse[4] __attribute__((aligned(16)));

int main(void){
  int i;
  printf("Ordinal calculation...\n");
  for(i = 0; i < 4; i++) {
    out[i] = inp1[i] + inp2[i];
    printf("Result %d: %.2f\n",i,out[i]);
  }

  printf("...with SSE instructions...\n");
  
  a = _mm_load_ps(inp_sse1);
  b = _mm_load_ps(inp_sse2);
  c = _mm_add_ps(a, b);
  _mm_store_ps(out_sse,c);


  for(i = 0; i < 4; i++) {
    printf("Result %d: %.2f\n",i,out_sse[i]);
  }

  return 0;
}

