#include <stdio.h>
#include <stdlib.h>
#include <math.h>
extern long long dyn_instr_count;
int xx,yy;
//long double dyn_instr_count;

void test()
{
	int *x;
	int num;
	xx=10, yy=20;
	printf("\nHello! In func Test Enter a num");
	//scanf("%d",&num);
	num = 100;
	if(num>5)
		x = &xx;
	else
		x = &yy;	
        int p = *x;
	printf("\nvalue of x is %d",x);
	printf("\nvalue of p is %d", p);
}
void _fVeeeeeeeeeeeeeeerrrrrrrrrrrLLongFunc(int n,int n1) 
{
	double d[100];
	int * p;
	n = 1234;
	p = (int *)malloc(sizeof(int) * n);
	printf("\nsomething something %x, %x\n", p, d);
	printf("\nf called\n");
}

void g()
{
	int i;
	int sum = 34;
	int a[256] = {0};
	int b[256] = {0};
	int c[256] = {0};
	int *p;
	int *pd;
	int *pt;
	printf("\nIn g");
	int n,*k[100];
	//scanf("%d",&n);
	n = 2001;
	pd = (int *)malloc(sizeof(int) * n);
	p = (int *)malloc(sizeof(int) * n);
	for(i=0;i<256;i++)
	{

		a[i] = i; b[i] = i; c[i] = i;
	}
	for(i=0; i<250; i++)
	{
		a[i] = b[i] + c[i];
	}
	_fVeeeeeeeeeeeeeeerrrrrrrrrrrLLongFunc(n,n);
	printf ("\nStill in g, a = %x, p = %x\n", a, p);
	free(p);
	free(pd);
}


int main() 
{
	int t[100],input;
	int *s;
	s= &input;
	int i = sin(100);
	//printf("hello world, %x, %x, %x, %x\n", main, g, f, t);
	g();
	g();
	printf("\nEnter a num");
	//scanf("%d",&input);
	input = 100;
	if(input>1000)
	{
		if(input < 1000)
			input = 10;
	}
	if(input>1000)
	{
		test();
	}
	i =0;
	return 0;
}
